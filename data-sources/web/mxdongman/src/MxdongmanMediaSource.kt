package me.him188.ani.datasources.mxdongman

import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import me.him188.ani.datasources.api.matcher.WebVideo
import me.him188.ani.datasources.api.matcher.WebVideoMatcher
import me.him188.ani.datasources.api.matcher.WebVideoMatcherContext
import me.him188.ani.datasources.api.source.FactoryId
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.api.source.MediaSourceInfo
import me.him188.ani.datasources.api.source.ThreeStepWebMediaSource
import me.him188.ani.datasources.api.source.bodyAsDocument
import me.him188.ani.datasources.api.source.useHttpClient
import org.jsoup.nodes.Document

class MxdongmanWebVideoMatcher : WebVideoMatcher {
    override fun match(url: String, context: WebVideoMatcherContext): WebVideo? {
        if (context.media.mediaSourceId != MxdongmanMediaSource.ID) return null
        if (url.contains("https://v16m-default.akamaized.net")) {
            return WebVideo(
                url,
                mapOf(
                    "User-Agent" to """Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3""",
                    "Referer" to "https://www.mxdm4.com/dongmanplay/",
                    "Sec-Ch-Ua-Mobile" to "?0",
                    "Sec-Ch-Ua-Platform" to "macOS",
                    "Sec-Fetch-Dest" to "video",
                    "Sec-Fetch-Mode" to "no-cors",
                    "Sec-Fetch-Site" to "cross-site",
                ),
            )
        }
        return null
    }
}

private const val BASE_URL = "https://www.mxdm.xyz"

class MxdongmanMediaSource(config: MediaSourceConfig) : ThreeStepWebMediaSource() {
    companion object {
        const val ID = "mxdongman"
        val INFO = MediaSourceInfo(
            "MX 动漫",
            websiteUrl = BASE_URL,
            imageUrl = "$BASE_URL/favicon.ico",
        )
    }

    class Factory : MediaSourceFactory {
        override val factoryId: FactoryId get() = me.him188.ani.datasources.api.source.FactoryId(ID)

        override val info: MediaSourceInfo get() = INFO
        override fun create(mediaSourceId: String, config: MediaSourceConfig): MediaSource =
            MxdongmanMediaSource(config)
    }

    override val baseUrl: String get() = BASE_URL

    // https://www.mxdm4.com/search/-------------.html?wd=%E6%A8%B1trick
    override fun parseBangumiSearch(document: Document): List<Bangumi> {
        /*
        <div class="video-info-header"><a class="video-serial"
                                   href="/dongman/1850.html"
                                   title="樱Trick">完结</a>
     <h3><a href="/dongman/1850.html" title="樱Trick">樱Trick</a></h3>
     <div class="video-info-aux">
         */

        return document.getElementsByClass("video-info-header").orEmpty().mapNotNull {
            val a = it.selectFirst("a") ?: return@mapNotNull null
            Bangumi(
                internalId = a.attr("href").removePrefix("/dongman/").removeSuffix(".html"),
                name = a.attr("title"),
                url = baseUrl + a.attr("href"),
            )
        }
    }

    override suspend fun search(name: String, query: MediaFetchRequest): List<Bangumi> {
        val document = client.get("$baseUrl/search/-------------.html") {
            parameter("wd", name)
        }.bodyAsDocument()
        return parseBangumiSearch(document)
    }

    // https://www.mxdm4.com/dongman/1850.html
    override fun parseEpisodeList(document: Document): List<Ep> {
        /*
        <div class="sort-item" id="sort-item-1">
                    
                   <a href="/dongmanplay/1850-1-1.html" title="播放樱Trick第01集"><span>第01集</span></a>
                     
                   <a href="/dongmanplay/1850-1-2.html" title="播放樱Trick第02集"><span>第02集</span></a>
                     
                   <a href="/dongmanplay/1850-1-3.html" title="播放樱Trick第03集"><span>第03集</span></a>
                     
                   <a href="/dongmanplay/1850-1-4.html" title="播放樱Trick第04集"><span>第04集</span></a>
                     
                   <a href="/dongmanplay/1850-1-5.html" title="播放樱Trick第05集"><span>第05集</span></a>
                     
                   <a href="/dongmanplay/1850-1-6.html" title="播放樱Trick第06集"><span>第06集</span></a>
                     
                   <a href="/dongmanplay/1850-1-7.html" title="播放樱Trick第07集"><span>第07集</span></a>
                     
                   <a href="/dongmanplay/1850-1-8.html" title="播放樱Trick第08集"><span>第08集</span></a>
                     
                   <a href="/dongmanplay/1850-1-9.html" title="播放樱Trick第09集"><span>第09集</span></a>
                     
                   <a href="/dongmanplay/1850-1-10.html" title="播放樱Trick第10集"><span>第10集</span></a>
                     
                   <a href="/dongmanplay/1850-1-11.html" title="播放樱Trick第11集"><span>第11集</span></a>
                     
                   <a href="/dongmanplay/1850-1-12.html" title="播放樱Trick第12集"><span>第12集</span></a>
                                      
                    </div>
         */
        return document.select("#sort-item-1").flatMap { element ->
            element.select("a").map {
                Ep(
                    name = it.text(),
                    url = baseUrl + it.attr("href"),
                )
            }
        }
    }

    override val client by lazy {
        useHttpClient(config) {
            BrowserUserAgent()
        }
    }

    override val mediaSourceId: String get() = ID
    override val info: MediaSourceInfo get() = INFO
}
