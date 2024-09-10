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
import kotlinx.serialization.Serializable
import me.him188.ani.app.data.models.ApiResponse
import me.him188.ani.app.data.models.runApiRequest
import me.him188.ani.app.tools.rss.RssChannel
import me.him188.ani.app.tools.rss.RssItem
import me.him188.ani.app.tools.rss.RssParser
import me.him188.ani.app.tools.rss.guessResourceLocation
import me.him188.ani.app.ui.settings.tabs.media.source.rss.test.RssItemPresentation
import me.him188.ani.app.ui.settings.tabs.media.source.rss.test.RssTestPane
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.topic.FileSize.Companion.Unspecified
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.titles.RawTitleParser
import me.him188.ani.datasources.api.topic.titles.parse
import me.him188.ani.utils.ktor.toSource
import me.him188.ani.utils.xml.Document
import me.him188.ani.utils.xml.Xml

/**
 * 一个共享接口同时用于 [RssMediaSource] 编辑时的测试.
 *
 * 默认实现为 [DefaultRssMediaSourceEngine].
 * 在用户编辑数据源时的测试功能中, 也是使用 [DefaultRssMediaSourceEngine] (因为也要执行网络请求).
 *
 * @see DefaultRssMediaSourceEngine
 */
abstract class RssMediaSourceEngine {
    data class Result(
        val encodedUrl: Url,
        val query: RssSearchQuery,
        val document: Document?,
        val channel: RssChannel?,
        val matchedMediaList: List<Media>?, // null means not found
    )

    /**
     * 搜索并使用 [searchConfig] 过滤.
     */
    suspend fun search(
        searchConfig: RssSearchConfig,
        query: RssSearchQuery,
        page: Int?,
        mediaSourceId: String,
    ): ApiResponse<Result> {
        val encodedUrl = encodeKeyword(query)

        val finalUrl = Url(
            searchConfig.searchUrl
                .replace("{keyword}", encodedUrl)
                .replace("{page}", page.toString()),
        )

        return searchImpl(finalUrl, searchConfig, query, page, mediaSourceId)
    }

    protected abstract suspend fun searchImpl(
        finalUrl: Url,
        config: RssSearchConfig,
        query: RssSearchQuery,
        page: Int?,
        mediaSourceId: String,
    ): ApiResponse<Result>

    protected companion object {
        fun encodeKeyword(query: RssSearchQuery) =
            URLBuilder().appendPathSegments(query.subjectName).encodedPathSegments.first()

        fun convertItemToMedia(
            item: RssItem,
            mediaSourceId: String,
        ): Media? {
            val details = RawTitleParser.getDefault().parse(item.title, null)

            return DefaultMedia(
                mediaId = "$mediaSourceId.${item.guid}",
                mediaSourceId = mediaSourceId,
                originalUrl = item.link.takeIf { it.isNotBlank() } ?: item.guid,
                download = item.guessResourceLocation() ?: return null,
                originalTitle = item.title,
                publishedTime = item.pubDate?.toInstant(TimeZone.currentSystemDefault())
                    ?.toEpochMilliseconds() ?: 0,
                properties = MediaProperties(
                    subtitleLanguageIds = details.subtitleLanguages.map { it.id },
                    resolution = details.resolution?.toString() ?: Resolution.R1080P.toString(),
                    alliance = item.title.trim().split("]", "】").getOrNull(0).orEmpty().removePrefix("[")
                        .removePrefix("【").trim(),
                    size = if (item.enclosure == null || item.enclosure.length <= 1L) Unspecified // 有的源会返回 1
                    else item.enclosure.length.bytes,
                    subtitleKind = details.subtitleKind,
                ),
                episodeRange = details.episodeRange,
                kind = MediaSourceKind.BitTorrent,
                location = MediaSourceLocation.Online,
            )
        }
    }
}

/**
 * 决定 [RssMediaSourceEngine.search] 的行为的配置.
 *
 * 添加新配置需要考虑兼容旧版本, 需要修改:
 * - [createFilters]
 * - 编辑数据源时的测试功能 ([RssTestPane]).
 *
 * 如果新加功能会影响产出结果 (例如新加一个过滤功能), 还需要修改 UI 的 tags 来在测试页面能 debug 到这个新过滤功能: [RssItemPresentation.computeTags]
 *
 * @since 3.9
 * @see createFilters
 */
@Serializable
data class RssSearchConfig(
    val searchUrl: String = "", // required
    val filterByEpisodeSort: Boolean = true,
    val filterBySubjectName: Boolean = true,
) {
    companion object {
        val Empty = RssSearchConfig()
    }
}

/**
 * 根据配置信息创建应当用于过滤搜索到的 [Media] 列表的过滤器.
 *
 * 注意, 这不会影响 UI 里每个 RSS 卡片显示的信息. 如需修改, 查看 [RssItemPresentation.computeTags]
 *
 * @see MediaListFilter
 */
fun RssSearchConfig.createFilters() = buildList {
    if (filterBySubjectName) add(MediaListFilters.ContainsSubjectName)
    if (filterByEpisodeSort) add(MediaListFilters.ContainsEpisodeSort)
}

/**
 * @see RssMediaSourceEngine
 */
class DefaultRssMediaSourceEngine(
    /**
     * Engine 自己不会 cache 实例, 每次都调用 `.first()`.
     */
    private val client: Flow<HttpClient>,
    private val parser: RssParser = RssParser(includeOrigin = false),
) : RssMediaSourceEngine() {
    override suspend fun searchImpl(
        finalUrl: Url,
        config: RssSearchConfig,
        query: RssSearchQuery,
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

        val channel = parser.parse(document)

        val filters = config.createFilters()

        val items = with(query.toFilterContext()) {
            channel.items.mapNotNull { rssItem ->
                convertItemToMedia(rssItem, mediaSourceId)
                    ?.takeIf { media ->
                        filters.applyOn(media.asCandidate())
                    }
            }
        }

        Result(
            finalUrl,
            query,
            document,
            channel,
            items,
        )
    }
}
