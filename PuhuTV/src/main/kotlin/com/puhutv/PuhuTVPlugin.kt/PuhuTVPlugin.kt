package com.puhutv

import android.content.Context
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import org.json.JSONObject
import org.jsoup.nodes.Element
import java.util.Locale

@CloudstreamPlugin
class PuhuTVPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(PuhuTVProvider())
    }
}

class PuhuTVProvider : MainAPI() {
    override var mainUrl = "https://puhutv.com"
    override var name = "PuhuTV"
    override var lang = "tr"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override val hasMainPage = true
    override val mainPage = mainPageOf(
        "Ana Sayfa" to "/",
        "Diziler" to "/dizi",
        "Yerli Diziler" to "/yerli-diziler",
        "PuhuTV Orijinal" to "/puhutv-orijinal",
        "Yaşam" to "/yasam",
    )

    override suspend fun search(query: String): List<SearchResponse> {
        val slug = query.toPuhuSlug()
        if (slug.isBlank()) return emptyList()
        val result = runCatching { app.get("$mainUrl/api/slug/$slug-detay").text }.getOrNull()
            ?: return emptyList()
        val data = result.toJsonObject()?.optJSONObject("data") ?: return emptyList()
        val assetSlug = data.optJSONArray("assets")?.optJSONObject(0)?.optString("slug")
            ?.takeIf { it.isNotBlank() }?.removeSuffix("-izle") ?: slug
        val videoUrl = "$mainUrl/$assetSlug-izle"
        val title = data.optString("name").takeIf { it.isNotBlank() }
            ?: data.optJSONObject("title")?.optString("name")?.takeIf { it.isNotBlank() }
            ?: slug.toPuhuTitle()
        val poster = data.optJSONObject("content")?.imageUrl()
        return listOf(newMovieSearchResponse(title, videoUrl, TvType.Movie) {
            this.posterUrl = poster
        })
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val path = request.data.ifBlank { "/" }
        val document = app.get(mainUrl + path).document
        val results = document.select("a[href*=-izle]")
            .mapNotNull { it.toPuhuSearchResponse() }.distinctBy { it.url }.take(40)
        return newHomePageResponse(request.name, results, hasNext = false)
    }

    override suspend fun load(url: String): LoadResponse? {
        val slug = url.substringAfter(mainUrl).substringBefore('?').trim('/')
            .removeSuffix("-izle").removeSuffix("-detay")
        if (slug.isBlank()) return null
        val data = runCatching {
            app.get("$mainUrl/api/slug/$slug-izle").text.toJsonObject()?.optJSONObject("data")
        }.getOrNull() ?: return null
        val title = data.optString("name").takeIf { it.isNotBlank() }
            ?: data.optJSONObject("title")?.optString("name")?.takeIf { it.isNotBlank() }
            ?: slug.toPuhuTitle()
        val description = data.optString("description").takeIf { it.isNotBlank() }
            ?: data.optJSONObject("title")?.optString("description")?.takeIf { it.isNotBlank() }
        val poster = data.optJSONObject("content")?.imageUrl()
        val year = data.optJSONObject("title")?.optInt("released_at")?.takeIf { it > 0 }
        val watchUrl = "$mainUrl/$slug-izle"
        return newMovieLoadResponse(title, watchUrl, TvType.Movie, watchUrl) {
            this.posterUrl = poster
            this.plot = description
            this.year = year
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val slug = data.substringAfter(mainUrl).substringBefore('?').trim('/').removeSuffix("-izle")
        if (slug.isBlank()) return false
        val info = runCatching {
            app.get("$mainUrl/api/slug/$slug-izle").text.toJsonObject()?.optJSONObject("data")
        }.getOrNull() ?: return false
        val id = info.optString("id").takeIf { it.isNotBlank() } ?: return false
        val videos = runCatching {
            app.get("$mainUrl/api/assets/$id/videos").text.toJsonObject()
                ?.optJSONObject("data")?.optJSONArray("videos")
        }.getOrNull() ?: return false
        for (index in 0 until videos.length()) {
            val video = videos.optJSONObject(index) ?: continue
            val streamUrl = video.optString("url").takeIf { it.startsWith("https://") } ?: continue
            val quality = video.optInt("quality").takeIf { it > 0 } ?: Qualities.Unknown.value
            val isHls = video.optString("video_format") == "hls" || streamUrl.contains(".m3u8", true)
            callback(ExtractorLink(
                source = name,
                name = if (quality == Qualities.Unknown.value) name else "$name " + quality + "p",
                url = streamUrl,
                referer = "$mainUrl/",
                quality = quality,
                isM3u8 = isHls,
            ))
        }
        val subtitles = info.optJSONObject("content")?.optJSONArray("subtitles")
        for (index in 0 until (subtitles?.length() ?: 0)) {
            val subtitle = subtitles?.optJSONObject(index) ?: continue
            val subtitleUrl = subtitle.optString("url").takeIf { it.startsWith("https://") } ?: continue
            subtitleCallback(SubtitleFile(subtitle.optString("language").ifBlank { "tr" }, subtitleUrl))
        }
        return true
    }

    private fun Element.toPuhuSearchResponse(): SearchResponse? {
        val href = attr("abs:href").takeIf { it.startsWith("$mainUrl/") } ?: return null
        val slug = href.substringAfterLast('/').substringBefore('?')
        if (!slug.endsWith("-izle")) return null
        val card = parents().firstOrNull { it.selectFirst("h1, h2, h3, h4") != null } ?: this
        val title = card.selectFirst("h1, h2, h3, h4")?.text()?.trim()?.takeIf { it.isNotBlank() }
            ?: selectFirst("img")?.attr("alt")?.trim()?.takeIf { it.isNotBlank() }
            ?: slug.removeSuffix("-izle").toPuhuTitle()
        val poster = card.selectFirst("img")?.let { it.attr("abs:src").ifBlank { it.attr("abs:data-src") } }
        return newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = poster }
    }
}

private fun String.toJsonObject(): JSONObject? = runCatching { JSONObject(this) }.getOrNull()

private fun JSONObject.imageUrl(): String? {
    val images = optJSONObject("images") ?: return null
    val keys = images.keys()
    while (keys.hasNext()) {
        val value = images.optString(keys.next()).trim()
        if (value.startsWith("https://")) return value
        if (value.startsWith("//")) return "https:$value"
    }
    return null
}

private fun String.toPuhuSlug(): String = lowercase(Locale.ROOT)
    .replace('ı', 'i').replace('ğ', 'g').replace('ü', 'u')
    .replace('ş', 's').replace('ö', 'o').replace('ç', 'c')
    .replace(Regex("[^a-z0-9]+"), "-").trim('-')

private fun String.toPuhuTitle(): String = split('-').joinToString(" ") { word ->
    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("tr")) else it.toString() }
}
