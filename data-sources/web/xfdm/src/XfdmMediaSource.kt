package me.him188.ani.datasources.ntdm

import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.HttpRedirect
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

class XfdmWebVideoMatcher : WebVideoMatcher {
    override fun match(url: String, context: WebVideoMatcherContext): WebVideo? {
        if (context.media.mediaSourceId != XfdmMediaSource.ID) return null

        if (url.indexOf("https://", startIndex = 1) != -1) {
            // 有多个 https
            return null
        }
        if (url.startsWith("pan.wo.cn") && url.contains("download") || url.contains(".mp4")) {
            return WebVideo(
                url,
                mapOf(
                    "User-Agent" to """Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3""",
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

// 注意: xfdm 和 gugufan 的解析是一模一样的, 只有 http routing 和 XfdmWebVideoMatcher 与 gugufan 不一样
class XfdmMediaSource(
    config: MediaSourceConfig,
) : ThreeStepWebMediaSource() {
    companion object {
        const val ID = "xfdm"
        const val BASE_URL = "https://dm1.xfdm.pro"
        val INFO = MediaSourceInfo(
            displayName = "稀饭动漫",
            websiteUrl = BASE_URL,
            imageUrl = "$BASE_URL/upload/site/20240308-1/813e41f81d6f85bfd7a44bf8a813f9e5.png",
            imageResourceId = "xfdm.png",
        )
    }

    override val baseUrl: String get() = BASE_URL

    class Factory : MediaSourceFactory {
        override val factoryId: FactoryId get() = me.him188.ani.datasources.api.source.FactoryId(ID)


        override fun create(mediaSourceId: String, config: MediaSourceConfig): MediaSource =
            XfdmMediaSource(config)

        override val info: MediaSourceInfo = INFO
    }


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
        // https://www.xfdm.com/index.php/vod/search.html?wd=%E5%88%AB%E5%BD%93%E6%AC%A7%E5%B0%BC%E9%85%B1%E4%BA%86
        val document = client.get("$baseUrl/search.html") {
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
    override val info: MediaSourceInfo = INFO
}
