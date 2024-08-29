package me.him188.ani.datasources.nyafun

import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.isSuccess
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.SubtitleKind
import me.him188.ani.datasources.api.matcher.WebVideo
import me.him188.ani.datasources.api.matcher.WebVideoMatcher
import me.him188.ani.datasources.api.matcher.WebVideoMatcherContext
import me.him188.ani.datasources.api.paging.SinglePagePagedSource
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.source.FactoryId
import me.him188.ani.datasources.api.source.HttpMediaSource
import me.him188.ani.datasources.api.source.MatchKind
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.api.source.MediaSourceInfo
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.source.definitelyMatches
import me.him188.ani.datasources.api.source.toConnectionStatus
import me.him188.ani.datasources.api.source.useHttpClient
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.utils.logging.warn
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class NyafunBangumi(
    val id: String,
    val name: String,
    val url: String,
)

data class NyafunEp(
    val name: String,
    val url: String,
)

class NyafunWebVideoMatcher : WebVideoMatcher {
    override fun match(url: String, context: WebVideoMatcherContext): WebVideo? {
        if (context.media.mediaSourceId != NyafunMediaSource.ID) return null
        // we want https://vod.2bdm.cc/2024/04/gs8h/01.mp4?verify=1716675316-p3ScUWwQbHmMf5%2F63tM6%2FR2Ac8NydzYvECQ1XmTUhbU%3D
        if ((url.contains(".mp4") || url.contains(".mkv") || url.contains(".m3u8"))
            && url.contains("verify=")
        ) {
            return WebVideo(
                url,
                mapOf(
                    "User-Agent" to """Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3""",
                    "Referer" to "https://play.nyafun.net/",
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

class NyafunMediaSource(config: MediaSourceConfig) : HttpMediaSource() {
    companion object {
        const val ID = "nyafun"
        internal const val BASE_URL = "https://www.nyacg.net"
        val INFO = MediaSourceInfo(
            displayName = "Nyafun",
            websiteUrl = BASE_URL,
            imageUrl = "https://files.superbed.cn/proxy/7468686c6f26333378737f75717b2f3278737f6f326d6d327f73713375717d7b79335d7b5d5d5f2a6931484a4c5d71757f28666d4650502b682c28702f4b2b566a6c326c727b",
            imageResourceId = "nyafun.png",
        )

        // https://www.nyafun.net/search.html?wd=girls%20band%20cry
        fun parseBangumiSearch(document: Document): List<NyafunBangumi> =
            document.getElementsByClass("public-list-box").map { element ->
                val a = element.select(".thumb-txt.cor4.hide").select("a")
                val name = a.text()
                val url = a.attr("href")
                val id = url.substringBeforeLast(".html").substringAfterLast("/")
                NyafunBangumi(
                    id = id,
                    name = name,
                    url = BASE_URL + url,
                )
            }

        // https://www.nyafun.net/bangumi/7168.html
        fun parseEpisodeList(document: Document): List<NyafunEp> {
            return document.getElementsByClass("anthology-list-play").flatMap { element ->
                element.select("a").map { a ->
                    val name = a.text() // "第1集"
                    val url = a.attr("href") // "/play/7168-1-1.html"
                    NyafunEp(name, BASE_URL + url)
                }
            }
        }


        private val subtitleLanguages = listOf("CHS")

        fun createMediaMatch(
            bangumi: NyafunBangumi,
            ep: NyafunEp
        ): MediaMatch {
            val sort = EpisodeSort(ep.name.removePrefix("第").removeSuffix("集"))
            return MediaMatch(
                DefaultMedia(
                    mediaId = "$ID.${bangumi.id}-${sort}",
                    mediaSourceId = ID,
                    originalUrl = bangumi.url,
                    download = ResourceLocation.WebVideo(ep.url),
                    originalTitle = """${bangumi.name} ${ep.name}""",
                    publishedTime = 0L,
                    properties = MediaProperties(
                        subtitleLanguageIds = subtitleLanguages,
                        resolution = "1080P",
                        alliance = ID,
                        size = FileSize.Unspecified,
                        subtitleKind = SubtitleKind.EMBEDDED,
                    ),
                    episodeRange = EpisodeRange.single(
                        if (isPossiblyMovie(ep.name) && sort is EpisodeSort.Special) {
                            EpisodeSort(1) // 电影总是 01
                        } else {
                            sort
                        },
                    ),
                    location = MediaSourceLocation.Online,
                    kind = MediaSourceKind.WEB,
                ),
                MatchKind.FUZZY,
            )
        }

        private fun isPossiblyMovie(title: String): Boolean {
            val t = title
            return ("简" in t || "繁" in t) && ("2160P" in t || "1440P" in t || "2K" in t || "4K" in t || "1080P" in t || "720P" in t)
        }
    }

    class Factory : MediaSourceFactory {
        override val factoryId: FactoryId get() = me.him188.ani.datasources.api.source.FactoryId(ID)

        override val info: MediaSourceInfo get() = INFO
        override fun create(mediaSourceId: String, config: MediaSourceConfig): MediaSource = NyafunMediaSource(config)
    }

    private val client by lazy {
        useHttpClient(config) {
            BrowserUserAgent()
        }
    }

    override val kind: MediaSourceKind get() = MediaSourceKind.WEB

    override val mediaSourceId: String get() = ID

    override suspend fun checkConnection(): ConnectionStatus =
        client.get(BASE_URL).status.isSuccess().toConnectionStatus()

    override suspend fun fetch(query: MediaFetchRequest): SizedSource<MediaMatch> = SinglePagePagedSource {
        query.subjectNames.asFlow().flatMapMerge { name ->
            val bangumiList = flow {
                emit(
                    getDocument("$BASE_URL/search.html") {
                        parameter("wd", name)
                    },
                )
            }.map {
                parseBangumiSearch(it)
            }.retry(3) { e ->
                logger.warn(e) { "Failed to search using name '$name'" }
                true
            }.firstOrNull() ?: return@flatMapMerge emptyFlow()

            bangumiList.asFlow()
                .flatMapMerge { bangumi ->
                    val result = flow {
                        emit(getDocument(bangumi.url))
                    }.map {
                        parseEpisodeList(it)
                    }.retry(3) { e ->
                        logger.warn(e) { "Failed to get episodes using name '$name'" }
                        true
                    }.firstOrNull()
                        .orEmpty()
                        .asSequence()
                        .map { ep ->
                            createMediaMatch(bangumi, ep)
                        }
                        .filter {
                            it.definitelyMatches(query) ||
                                    isPossiblyMovie(it.media.originalTitle)
                        }
                        .toList()

                    result.asFlow()
                }
        }
    }

    private suspend inline fun getDocument(
        url: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ) = client.get(url, block).bodyAsChannel().toInputStream().use {
        Jsoup.parse(it, "UTF-8", BASE_URL)
    }

    override val info: MediaSourceInfo get() = INFO
}
