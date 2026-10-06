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
@CloudstreamPlugin class PuhuTVPlugin : Plugin() { override fun load(context: Context) { registerMainAPI(PuhuTVProvider()) } }
class PuhuTVProvider : MainAPI() {
 override var mainUrl="https://puhutv.com"
 override var name="PuhuTV"
 override var lang="tr"
 override val supportedTypes=setOf(TvType.Movie,TvType.TvSeries)
 override val hasMainPage=true
 override val mainPage=mainPageOf("Ana Sayfa" to "/","Diziler" to "/dizi","Yerli Diziler" to "/yerli-diziler","PuhuTV Orijinal" to "/puhutv-orijinal")
 override suspend fun search(query:String):List<SearchResponse>{ val s=query.slug(); if(s.isBlank())return emptyList(); val d=runCatching{app.get("$mainUrl/api/slug/$s-detay").text.json()?.optJSONObject("data")}.getOrNull()?:return emptyList(); val asset=d.optJSONArray("assets")?.optJSONObject(0)?.optString("slug")?.removeSuffix("-izle")?:s; val title=d.optString("name").ifBlank{s.titleTr()}; return listOf(newMovieSearchResponse(title,"$mainUrl/$asset-izle",TvType.Movie){posterUrl=d.optJSONObject("content")?.image()}) }
 override suspend fun getMainPage(page:Int,request:MainPageRequest):HomePageResponse { val doc=app.get(mainUrl+request.data.ifBlank{"/"}).document; val results=doc.select("a[href*=-izle]").mapNotNull{it.toResponse()}.distinctBy{it.url}.take(40); return newHomePageResponse(request.name,results,hasNext=false) }
 override suspend fun load(url:String):LoadResponse? { val s=url.substringAfter(mainUrl).substringBefore('?').trim('/').removeSuffix("-izle"); val d=runCatching{app.get("$mainUrl/api/slug/$s-izle").text.json()?.optJSONObject("data")}.getOrNull()?:return null; val title=d.optString("name").ifBlank{s.titleTr()}; val watch="$mainUrl/$s-izle"; return newMovieLoadResponse(title,watch,TvType.Movie,watch){posterUrl=d.optJSONObject("content")?.image(); plot=d.optString("description").takeIf{it.isNotBlank()}} }
 override suspend fun loadLinks(data:String,isCasting:Boolean,subtitleCallback:(SubtitleFile)->Unit,callback:(ExtractorLink)->Unit):Boolean { val s=data.substringAfter(mainUrl).substringBefore('?').trim('/').removeSuffix("-izle"); val info=runCatching{app.get("$mainUrl/api/slug/$s-izle").text.json()?.optJSONObject("data")}.getOrNull()?:return false; val id=info.optString("id").takeIf{it.isNotBlank()}?:return false; val vs=runCatching{app.get("$mainUrl/api/assets/$id/videos").text.json()?.optJSONObject("data")?.optJSONArray("videos")}.getOrNull()?:return false; for(i in 0 until vs.length()){val v=vs.optJSONObject(i)?:continue; val u=v.optString("url").takeIf{it.startsWith("https://")}?:continue; val q=v.optInt("quality").takeIf{it>0}?:Qualities.Unknown.value; callback(ExtractorLink(source=name,name=if(q>0)name+" "+q+"p" else name,url=u,referer="$mainUrl/",quality=q,isM3u8=v.optString("video_format")=="hls"||u.contains(".m3u8",true)))}; return true }
 private fun Element.toResponse():SearchResponse? { val href=attr("abs:href").takeIf{it.startsWith("$mainUrl/")}?:return null; val slug=href.substringAfterLast('/').substringBefore('?'); if(!slug.endsWith("-izle"))return null; val title=selectFirst("img")?.attr("alt")?.takeIf{it.isNotBlank()}?:slug.removeSuffix("-izle").titleTr(); val poster=selectFirst("img")?.let{it.attr("abs:src").ifBlank{it.attr("abs:data-src")}}; return newMovieSearchResponse(title,href,TvType.Movie){posterUrl=poster} }
}
private fun String.json()=runCatching{JSONObject(this)}.getOrNull()
private fun JSONObject.image():String? {val o=optJSONObject("images")?:return null; val k=o.keys(); while(k.hasNext()){val v=o.optString(k.next()); if(v.startsWith("https://"))return v; if(v.startsWith("//"))return "https:$v"};return null}
private fun String.slug()=lowercase(Locale.ROOT).replace('ı','i').replace('ğ','g').replace('ü','u').replace('ş','s').replace('ö','o').replace('ç','c').replace(Regex("[^a-z0-9]+"),"-").trim('-')
private fun String.titleTr()=split('-').joinToString(" "){it.replaceFirstChar{c->if(c.isLowerCase())c.titlecase(Locale("tr")) else c.toString()}}
