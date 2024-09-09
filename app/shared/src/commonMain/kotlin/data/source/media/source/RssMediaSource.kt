package me.him188.ani.app.data.source.media.source

import io.ktor.client.request.get
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.Serializable
import me.him188.ani.app.data.models.ApiFailure
import me.him188.ani.app.data.models.fold
import me.him188.ani.app.data.models.runApiRequest
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.paging.map
import me.him188.ani.datasources.api.paging.merge
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.source.DownloadSearchQuery
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
import me.him188.ani.datasources.api.source.deserializeArgumentsOrNull
import me.him188.ani.datasources.api.source.useHttpClient
import me.him188.ani.datasources.api.topic.TopicCategory

@Serializable
data class RssMediaSourceArguments(
    val name: String,
    val description: String,
    val iconUrl: String,
    val searchUrl: String,
) {
    companion object {
        const val DEFAULT_ICON_URL = "https://rss.com/blog/wp-content/uploads/2019/10/social_style_3_rss-512-1.png"

        val Default = RssMediaSourceArguments(
            name = "RSS",
            description = "",
            iconUrl = DEFAULT_ICON_URL,
            searchUrl = "",
        )
    }
}

class RssMediaSource(
    override val mediaSourceId: String,
    config: MediaSourceConfig,
    override val kind: MediaSourceKind = MediaSourceKind.BitTorrent,
) : HttpMediaSource() {
    companion object {
        val FactoryId = FactoryId("rss")
    }

    private val arguments =
        config.deserializeArgumentsOrNull(RssMediaSourceArguments.serializer()) ?: RssMediaSourceArguments.Default

    private val usePaging = arguments.searchUrl.contains("{page}")

    private val client by lazy { useHttpClient(config) }
    private val engine by lazy { DefaultRssMediaSourceEngine(flowOf(client)) }

    override val location: MediaSourceLocation get() = MediaSourceLocation.Online

    class Factory : MediaSourceFactory {
        override val factoryId: FactoryId get() = FactoryId

        override val info: MediaSourceInfo = MediaSourceInfo(
            displayName = "RSS",
            description = "通用 RSS BT 数据源",
            // https://rss.com/blog/free-rss-icon/
            iconUrl = "https://rss.com/blog/wp-content/uploads/2019/10/social_style_3_rss-512-1.png",
        )

        override val allowMultipleInstances: Boolean get() = true
        override fun create(mediaSourceId: String, config: MediaSourceConfig): MediaSource =
            RssMediaSource(mediaSourceId, config)
    }

    override suspend fun checkConnection(): ConnectionStatus {
        return kotlin.runCatching {
            runApiRequest {
                client.get(arguments.searchUrl) // 提交一个请求, 只要它不是因为网络错误就行
            }.fold(
                onSuccess = { ConnectionStatus.SUCCESS },
                onKnownFailure = {
                    when (it) {
                        ApiFailure.NetworkError -> ConnectionStatus.FAILED
                        ApiFailure.ServiceUnavailable -> ConnectionStatus.FAILED
                        ApiFailure.Unauthorized -> ConnectionStatus.SUCCESS
                    }
                },
            )
        }.recover {
            // 只要不是网络错误就行
            ConnectionStatus.SUCCESS
        }.getOrThrow()
    }

    override val info: MediaSourceInfo = MediaSourceInfo(
        displayName = arguments.name,
        description = arguments.description,
        websiteUrl = arguments.searchUrl,
        iconUrl = arguments.iconUrl,
    )

    // https://garden.breadio.wiki/feed.xml?filter=[{"search":["樱trick"]}]
    // https://acg.rip/page/2.xml?term=%E9%AD%94%E6%B3%95%E5%B0%91%E5%A5%B3
    private fun startSearch(query: DownloadSearchQuery): SizedSource<Media> {
        return PageBasedPagedSource { page ->
            if (!usePaging && page != 0) return@PageBasedPagedSource null

            val result = engine.search(arguments.searchUrl, query, page, mediaSourceId)
                .getOrThrow()

            // 404 Not Found
            val channel = result.channel ?: return@PageBasedPagedSource null
            val topics = result.matchedMediaList ?: return@PageBasedPagedSource null

            Paged(
                null,
                hasMore = channel.items.isNotEmpty(),
                page = topics,
            )
        }
    }

    override suspend fun fetch(query: MediaFetchRequest): SizedSource<MediaMatch> {
        return query.subjectNames
            .map { name ->
                startSearch(
                    DownloadSearchQuery(
                        keywords = name,
                        category = TopicCategory.ANIME,
                        episodeSort = query.episodeSort,
                        episodeEp = query.episodeEp,
                    ),
                ).map {
                    MediaMatch(it, MatchKind.FUZZY)
                }
            }.merge()
    }
}
