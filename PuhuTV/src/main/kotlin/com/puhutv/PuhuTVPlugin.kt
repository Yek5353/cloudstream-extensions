package com.puhutv
import android.content.Context
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.newExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import org.json.JSONObject
import org.jsoup.nodes.Element
import java.util.Locale

@CloudstreamPlugin
class PuhuTVPlugin : Plugin() { override fun load(context: Context) { registerMainAPI(PuhuTVProvider()) } }

class PuhuTVProvider : MainAPI() {
 override var mainUrl = "https://puhutv.com"
 override var name = "PuhuTV"
 override var lang = "tr"
 override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
 override val hasMainPage = true
 override val mainPage = mainPageOf("/" to "Ana Sayfa", "/dizi" to "Diziler", "/yerli-diziler" to "Yerli Diziler", "/puhutv-orijinal" to "PuhuTV Orijinal")

 override suspend fun search(query: String): List<SearchResponse> {
  val slug: String = query.slug()
  if (slug.isBlank()) return emptyList()
  val data: JSONObject = try { JSONObject(app.get("$mainUrl/api/slug/$slug-detay").text).getJSONObject("data") } catch (_: Exception) { return emptyList() }
  val title: String = data.optString("name").ifBlank { slug.titleTr() }
  val seasons = data.optJSONArray("seasons")
  val content: JSONObject? = data.optJSONObject("content")
  val poster: String? = if (content == null) null else content.image()
  return if (seasons != null && seasons.length() > 0) {
   listOf(newTvSeriesSearchResponse(title, "$mainUrl/$slug-detay") { posterUrl = poster })
  } else {
   val assets = data.optJSONArray("assets")
   val firstAsset: JSONObject? = if (assets == null || assets.length() == 0) null else assets.optJSONObject(0)
   val videoSlug: String = if (firstAsset == null) slug else firstAsset.optString("slug").removeSuffix("-izle")
   listOf(newMovieSearchResponse(title, "$mainUrl/$videoSlug-izle", TvType.Movie) { posterUrl = poster })
  }
 }

 override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
  val path: String = request.data.ifBlank { "/" }
  val document = app.get(mainUrl + path).document
  val results: List<SearchResponse> = document.select("a[href*=-detay], a[href*=-izle]").mapNotNull { element -> element.toResponse() }.distinctBy { it.url }.take(40)
  return newHomePageResponse(request.name, results, hasNext = false)
 }

 override suspend fun load(url: String): LoadResponse? {
  val requestedPath: String = url.substringAfter(mainUrl).substringBefore("?").trim('/')
  val isDetail: Boolean = requestedPath.endsWith("-detay")
  val slug: String = requestedPath.removeSuffix("-detay").removeSuffix("-izle")
  if (slug.isBlank()) return null
  val suffix: String = if (isDetail) "-detay" else "-izle"
  val data: JSONObject = try { JSONObject(app.get("$mainUrl/api/slug/$slug$suffix").text).getJSONObject("data") } catch (_: Exception) { return null }
  val title: String = data.optString("name").ifBlank { slug.titleTr() }
  val content: JSONObject? = data.optJSONObject("content")
  val poster: String? = if (content == null) null else content.image()
  val seasons = data.optJSONArray("seasons")
  if (isDetail && seasons != null && seasons.length() > 0) {
   val episodes = mutableListOf<Episode>()
   for (seasonIndex in 0 until seasons.length()) {
    val season: JSONObject = seasons.optJSONObject(seasonIndex) ?: continue
    val seasonId: String = season.optString("id")
    if (seasonId.isBlank()) continue
    val seasonData: JSONObject = try { JSONObject(app.get("https://galadriel.puhutv.com/seasons/$seasonId?page=1&per=40").text) } catch (_: Exception) { continue }
    val seasonNumber: Int = season.optInt("number", seasonIndex + 1)
    val seasonEpisodes = seasonData.optJSONArray("episodes") ?: continue
    for (episodeIndex in 0 until seasonEpisodes.length()) {
     val episodeJson: JSONObject = seasonEpisodes.optJSONObject(episodeIndex) ?: continue
     val episodePath: String = episodeJson.optString("slugPath")
     if (episodePath.isBlank()) continue
     episodes.add(newEpisode("$mainUrl/" + episodePath.trimStart('/')) {
      name = episodeJson.optString("name").ifBlank { episodeJson.optString("eventLabel") }
      season = seasonNumber
      episode = episodeJson.optInt("number", episodeIndex + 1)
     })
    }
   }
   return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) { posterUrl = poster; plot = data.optString("description").takeIf { it.isNotBlank() } }
  }
  val assets = data.optJSONArray("assets")
  val firstAsset: JSONObject? = if (assets == null || assets.length() == 0) null else assets.optJSONObject(0)
  val videoSlug: String = if (firstAsset == null) slug else firstAsset.optString("slug").removeSuffix("-izle")
  val watchUrl: String = "$mainUrl/$videoSlug-izle"
  return newMovieLoadResponse(title, watchUrl, TvType.Movie, watchUrl) { posterUrl = poster; plot = data.optString("description").takeIf { it.isNotBlank() } }
 }

 override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
  val slug: String = data.substringAfter(mainUrl).substringBefore("?").trim('/').removeSuffix("-izle")
  val info: JSONObject = try { JSONObject(app.get("$mainUrl/api/slug/$slug-izle").text).getJSONObject("data") } catch (_: Exception) { return false }
  val id: String = info.getString("id")
  if (id.isBlank()) return false
  val videos = try { JSONObject(app.get("$mainUrl/api/assets/$id/videos").text).getJSONObject("data").getJSONArray("videos") } catch (_: Exception) { return false }
  for (index in 0 until videos.length()) {
   val video: JSONObject = videos.getJSONObject(index)
   val streamUrl: String = video.optString("url")
   if (!streamUrl.startsWith("https://")) continue
   val quality: Int = video.optInt("quality", Qualities.Unknown.value)
   val format: String = video.optString("video_format")
   callback(newExtractorLink(source = name, name = name, url = streamUrl, type = if (format == "hls" || streamUrl.contains(".m3u8", true)) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO) { this.quality = quality; referer = "$mainUrl/" })
  }
  return true
 }

 private fun Element.toResponse(): SearchResponse? {
  val href: String = attr("abs:href")
  if (!href.startsWith("$mainUrl/")) return null
  val slug: String = href.substringAfterLast("/").substringBefore("?")
  val isDetail: Boolean = slug.endsWith("-detay")
  if (!isDetail && !slug.endsWith("-izle")) return null
  val image: Element? = selectFirst("img")
  val alt: String = if (image == null) "" else image.attr("alt")
  val title: String = if (alt.isNotBlank()) alt else slug.removeSuffix("-detay").removeSuffix("-izle").titleTr()
  val poster: String? = if (image == null) null else { val src: String = image.attr("abs:src"); if (src.isNotBlank()) src else image.attr("abs:data-src") }
  return if (isDetail) newTvSeriesSearchResponse(title, href) { posterUrl = poster } else newMovieSearchResponse(title, href, TvType.Movie) { posterUrl = poster }
 }
}

private fun JSONObject.image(): String? {
 val images: JSONObject = optJSONObject("images") ?: return null
 val keys = images.keys()
 while (keys.hasNext()) { val value: String = images.optString(keys.next()); if (value.startsWith("https://")) return value; if (value.startsWith("//")) return "https:$value" }
 return null
}
private fun String.slug(): String = lowercase(Locale.ROOT).replace('ı', 'i').replace('ğ', 'g').replace('ü', 'u').replace('ş', 's').replace('ö', 'o').replace('ç', 'c').replace(Regex("[^a-z0-9]+"), "-").trim('-')
private fun String.titleTr(): String = split('-').joinToString(" ") { word -> word.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale("tr")) else c.toString() } }
