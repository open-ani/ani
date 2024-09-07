package me.him188.ani.app.data.source.media.source

import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import me.him188.ani.app.data.models.ApiResponse
import me.him188.ani.app.data.models.runApiRequest
import me.him188.ani.app.tools.rss.RssChannel
import me.him188.ani.app.tools.rss.RssItem
import me.him188.ani.app.tools.rss.RssParser
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.source.DownloadSearchQuery
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.topic.FileSize.Companion.Unspecified
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.TopicCriteria
import me.him188.ani.datasources.api.topic.matches
import me.him188.ani.datasources.api.topic.titles.RawTitleParser
import me.him188.ani.datasources.api.topic.titles.parse
import me.him188.ani.datasources.api.topic.toTopicCriteria
import me.him188.ani.utils.ktor.toSource
import me.him188.ani.utils.xml.Document
import me.him188.ani.utils.xml.Xml

// 一个共享接口同时用于 RssMediaSource 和 UI 做测试
abstract class RssMediaSourceEngine {
    data class Result(
        val encodedUrl: Url,
        val query: DownloadSearchQuery,
        val document: Document?,
        val channel: RssChannel?,
        val matchedMediaList: List<Media>?, // null means not found
    )

    suspend fun search(
        searchUrl: String,
        query: DownloadSearchQuery,
        page: Int?,
        mediaSourceId: String,
    ): ApiResponse<Result> {
        val encodedUrl = encodeKeyword(query)

        val finalUrl = Url(
            searchUrl
                .replace("{keyword}", encodedUrl)
                .replace("{page}", page.toString()),
        )

        return searchImpl(finalUrl, query, page, mediaSourceId)
    }

    protected abstract suspend fun searchImpl(
        finalUrl: Url,
        query: DownloadSearchQuery,
        page: Int?,
        mediaSourceId: String,
    ): ApiResponse<Result>

    private fun encodeKeyword(query: DownloadSearchQuery) =
        URLBuilder().appendPathSegments(query.keywords).encodedPathSegments.first()

    protected companion object {
        private fun guessResourceLocation(url: String): ResourceLocation {
            return if (url.startsWith("magnet:")) {
                ResourceLocation.MagnetLink(url)
            } else {
                ResourceLocation.HttpTorrentFile(url)
            }
        }

        fun convertItemToMedia(
            item: RssItem,
            mediaSourceId: String,
            criteria: TopicCriteria,
        ): Media? {
            val enclosure = item.enclosure ?: return null

            val details = RawTitleParser.getDefault().parse(item.title, null)

            return DefaultMedia(
                mediaId = "$mediaSourceId.${item.guid}",
                mediaSourceId = mediaSourceId,
                originalUrl = item.link.takeIf { it.isNotBlank() } ?: item.guid,
                download = guessResourceLocation(enclosure.url),
                originalTitle = item.title,
                publishedTime = item.pubDate?.toInstant(TimeZone.currentSystemDefault())
                    ?.toEpochMilliseconds() ?: 0,
                properties = MediaProperties(
                    subtitleLanguageIds = details.subtitleLanguages.map { it.id },
                    resolution = details.resolution?.toString() ?: Resolution.R1080P.toString(),
                    alliance = item.title.trim().split("]", "】").getOrNull(0).orEmpty().removePrefix("[")
                        .removePrefix("【").trim(),
                    size = if (enclosure.length <= 1L) Unspecified // 有的源会返回 1
                    else enclosure.length.bytes,
                    subtitleKind = details.subtitleKind,
                ),
                episodeRange = details.episodeRange,
                kind = MediaSourceKind.BitTorrent,
                location = MediaSourceLocation.Online,
            ).takeIf { criteria.matches(it, allowEpMatch = false) }
        }
    }
}

class DefaultRssMediaSourceEngine(
    private val client: Flow<HttpClient>,
) : RssMediaSourceEngine() {
    override suspend fun searchImpl(
        finalUrl: Url,
        query: DownloadSearchQuery,
        page: Int?,
        mediaSourceId: String
    ): ApiResponse<Result> = runApiRequest {
        val document = try {
            client.first().get(finalUrl).let { resp ->
                Xml.parse(resp.bodyAsChannel().toSource())
            }
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                // 404 Not Found
                return@runApiRequest Result(
                    finalUrl,
                    query,
                    document = null,
                    channel = null,
                    matchedMediaList = null,
                )
            }
            throw e
        }

        val channel = RssParser.parse(document)

        Result(
            finalUrl,
            query,
            document,
            channel,
            channel.items.mapNotNull {
                convertItemToMedia(
                    it, mediaSourceId,
                    query.toTopicCriteria(),
                )
            },
        )
    }
}
