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
import me.him188.ani.datasources.api.source.ThreeStepWebMediaSource
import me.him188.ani.datasources.api.source.bodyAsDocument
import me.him188.ani.datasources.api.source.useHttpClient
import org.jsoup.nodes.Document

class NtdmWebVideoMatcher : WebVideoMatcher {
    override fun match(url: String, context: WebVideoMatcherContext): WebVideo? {
        if (context.media.mediaSourceId != NtdmMediaSource.ID) return null
        if (url.contains(".akamaized.net")) {
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

class NtdmMediaSource(config: MediaSourceConfig) : ThreeStepWebMediaSource() {
    companion object {
        const val ID = "ntdm"
        const val BASE_URL = "http://ntdm.tv"
    }

    class Factory : MediaSourceFactory {
        override val mediaSourceId: String get() = ID

        override fun create(config: MediaSourceConfig): MediaSource = NtdmMediaSource(config)
    }

    override val baseUrl: String get() = BASE_URL

    override fun parseBangumiSearch(document: Document): List<Bangumi> {
        return document.getElementsByClass("cell_imform").flatMap { element ->
            sequence {
                for (a in element.getElementsByClass("cell_imform_name")) {
                    yield(
                        Bangumi(
                            // /video/4621.html
                            internalId = a.attr("href").substringAfterLast("/").substringBefore(".html"),
                            name = a.text(),
                            url = baseUrl + a.attr("href"),
                        ),
                    )
                }
            }
        }
    }

    override suspend fun search(name: String, query: MediaFetchRequest): List<Bangumi> {
        val document = client.get("$baseUrl/search/-------------.html") {
            parameter("wd", name)
        }.bodyAsDocument()
        return parseBangumiSearch(document)
    }

    override fun parseEpisodeList(document: Document): List<Ep> {
        val channels = document.getElementById("menu0")
            ?.getElementsByTag("li")
            ?.map { it.text() }
            .orEmpty()
        return document.getElementById("main0")?.children()?.flatMapIndexed { index, element ->
            val channel = channels.getOrNull(index) ?: "未知线路"
            element.select("a").map { a ->
                Ep(
                    name = a.text(),
                    url = baseUrl + a.attr("href"),
                    channel = channel,
                )
            }
        }.orEmpty()
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
}
