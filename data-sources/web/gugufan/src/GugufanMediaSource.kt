package me.him188.ani.datasources.ntdm

import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import me.him188.ani.datasources.api.matcher.WebVideo
import me.him188.ani.datasources.api.matcher.WebVideoMatcher
import me.him188.ani.datasources.api.matcher.WebVideoMatcherContext
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.api.source.MediaSourceInfo
import me.him188.ani.datasources.api.source.ThreeStepWebMediaSource
import me.him188.ani.datasources.api.source.bodyAsDocument
import me.him188.ani.datasources.api.source.useHttpClient
import org.jsoup.nodes.Document

class GugufanWebVideoMatcher : WebVideoMatcher {
    override fun match(url: String, context: WebVideoMatcherContext): WebVideo? {
        if (context.media.mediaSourceId != GugufanMediaSource.ID) return null
        // https://fuckjapan.cindiwhite.com/videos/202305/05/64557f4f852ee3050d99fc8c/e8210b/index.m3u8?counts=1&timestamp=1721999625000&key=2a2094d5753ae1d26e1332fac72b9db9
        if (url.startsWith("https://fuckjapan.cindiwhite.com") && url.contains("index.m3u8")) {
            return WebVideo(
                url,
                mapOf(
                    "User-Agent" to """Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3""",
                    "Sec-Ch-Ua-Mobile" to "?0",
                    "Sec-Ch-Ua-Platform" to "macOS",
                    "Sec-Fetch-Dest" to "video",
                    "Sec-Fetch-Mode" to "no-cors",
                    "Sec-Fetch-Site" to "cross-site",
                    "Origin" to "https://a79.yizhoushi.com",
                ),
            )
        }
        return null
    }
}

class GugufanMediaSource(config: MediaSourceConfig) : ThreeStepWebMediaSource() {
    companion object {
        const val ID = "gugufan"
        const val BASE_URL = "https://www.gugufan.com"
        val INFO = MediaSourceInfo(
            "咕咕番",
            "咕咕番",
            imageUrl = "$BASE_URL/upload/site/20230512-1/8d3bab2eb1440259baad5079c0a28071.png",
            imageResourceId = "gugufan.png",
        )
    }

    class Factory : MediaSourceFactory {
        override val mediaSourceId: String get() = ID
        override val info: MediaSourceInfo get() = INFO

        override fun create(config: MediaSourceConfig): MediaSource = GugufanMediaSource(config)
    }

    override val baseUrl: String get() = BASE_URL

    override fun parseBangumiSearch(document: Document): List<Bangumi> {
        return document.getElementsByClass("public-list-box")
            .flatMap { element ->
                sequence {
                    for (ele in element.getElementsByClass("flex-auto")) {
                        val a = ele.getElementsByClass("thumb-menu").firstOrNull()
                            ?.getElementsByTag("a")?.firstOrNull() ?: continue
                        val url = a.attr("href")
                        yield(
                            Bangumi(
                                // /video/4621.html
                                internalId = url.substringAfterLast("/").substringBeforeLast("."),
                                name = ele.getElementsByClass("thumb-txt").firstOrNull()?.text() ?: continue,
                                url = baseUrl + url,
                            ),
                        )
                    }
                }
            }
    }

    override suspend fun search(name: String, query: MediaFetchRequest): List<Bangumi> {
        // https://www.gugufan.com/index.php/vod/search.html?wd=%E5%88%AB%E5%BD%93%E6%AC%A7%E5%B0%BC%E9%85%B1%E4%BA%86
        val document = client.get("$baseUrl/index.php/vod/search.html") {
            parameter("wd", name)
        }.bodyAsDocument()
        return parseBangumiSearch(document)
    }


    override fun parseEpisodeList(document: Document): List<Ep> {
        return document.select("body > div.box-width.cor5 > div.anthology.wow.fadeInUp.animated")
            .flatMap { resourceList ->
                val episodes = mutableListOf<Ep>()

                val channelElements =
                    resourceList.select("div.anthology-tab.nav-swiper.b-b.br div.swiper-wrapper a.swiper-slide")
                val channelNames = channelElements.map { e ->
                    e.text().trim().dropLastWhile { it.isDigit() }
                }

                val episodeElements = resourceList.getElementsByTag("a")
                    .filterNot { e -> e.text().dropLastWhile { it.isDigit() } in channelNames }

                for ((index, element) in episodeElements.withIndex()) {
                    val name = element.text()
                    val relativeUrl = element.attr("href")
                    val url = if (relativeUrl.startsWith("http")) relativeUrl else baseUrl + relativeUrl
                    val channel =
                        if (channelNames.isNotEmpty()) channelNames.getOrNull(index / episodeElements.size * channelNames.size) else null
                    val ep = Ep(name, url, channel)
                    episodes.add(ep)
                }

                episodes
            }
    }

    override val client by lazy {
        useHttpClient(config) {
            BrowserUserAgent()
            followRedirects = true
            install(HttpRedirect) {
                checkHttpMethod = false
            }
            expectSuccess = false
        }
    }

    override val mediaSourceId: String get() = ID
    override val info: MediaSourceInfo get() = INFO
}
