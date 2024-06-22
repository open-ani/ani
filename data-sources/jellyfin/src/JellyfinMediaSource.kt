package me.him188.ani.datasources.jellyfin

import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.paging.SinglePagePagedSource
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.source.HttpMediaSource
import me.him188.ani.datasources.api.source.MatchKind
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.source.MediaSourceParameters
import me.him188.ani.datasources.api.source.MediaSourceParametersBuilder
import me.him188.ani.datasources.api.source.get
import me.him188.ani.datasources.api.source.matches
import me.him188.ani.datasources.api.source.useHttpClient
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.ResourceLocation

class JellyfinMediaSource(config: MediaSourceConfig) : HttpMediaSource() {
    companion object {
        const val ID = "jellyfin"
    }

    object Parameters : MediaSourceParametersBuilder() {
        val baseUrl = string(
            "baseUrl",
            default = "http://localhost:8096",
            description = "服务器地址" +
                    "\n示例: http://localhost:8096",
        )
        val userId = string(
            "userId",
            description = "User ID, 可在 Jellyfin \"控制台 - 用户\" 中选择一个用户, 在浏览器地址栏找到 \"userId=\" 后面的内容" +
                    "\n示例: cc91f58d951648829c90115520f6adec",
        )
        val apikey = string(
            "apikey",
            description = "API Key, 可在 Jellyfin \"控制台 - API 秘钥\" 中添加" +
                    "\n示例: b7292a71d51a6bf3a31036086a6d2e23",
        )
    }

    class Factory : MediaSourceFactory {
        override val mediaSourceId: String get() = ID
        override val parameters: MediaSourceParameters = Parameters.build()
        override val allowMultipleInstances: Boolean get() = true
        override fun create(config: MediaSourceConfig): MediaSource = JellyfinMediaSource(config)
    }

    override val kind: MediaSourceKind get() = MediaSourceKind.WEB
    override val mediaSourceId: String get() = ID
    private val baseUrl = config[Parameters.baseUrl].removeSuffix("/")
    private val userId = config[Parameters.userId]
    private val apiKey = config[Parameters.apikey]

    private val client = useHttpClient(config) {
        defaultRequest {
            header(
                HttpHeaders.Authorization,
                "MediaBrowser Token=\"$apiKey\"",
            )
        }
    }

    override suspend fun checkConnection(): ConnectionStatus {
        try {
            doSearch("AA测试BB")
            return ConnectionStatus.SUCCESS
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return ConnectionStatus.FAILED
        }
    }

    override suspend fun fetch(query: MediaFetchRequest): SizedSource<MediaMatch> {
        // search by subject and episode number
        return SinglePagePagedSource {
            query.subjectNames
                .asFlow()
                .flatMapConcat { subjectName ->
                    val resp = doSearch(subjectName)
                    resp.Items.asFlow()
                }
                .flatMapMerge {
                    when (it.Type) {
                        "Season" -> doSearch(parentId = it.Id).Items.asFlow()
                        "Episode" -> flowOf(it)
                        else -> emptyFlow()
                    }
                }
                .filter { it.Type == "Episode" && it.CanDownload }
                .toList()
                .distinctBy { it.Id }
                .mapNotNull {
                    it.IndexNumber ?: return@mapNotNull null

                    val episodeRange = EpisodeRange.single(EpisodeSort(it.IndexNumber))
                    MediaMatch(
                        media = DefaultMedia(
                            mediaId = it.Id,
                            mediaSourceId = mediaSourceId,
                            originalUrl = "$baseUrl/Items/${it.Id}",
                            download = ResourceLocation.HttpStreamingFile(
                                uri = "$baseUrl/Items/${it.Id}/Download?ApiKey=${apiKey}",
                            ),
                            originalTitle = "${it.IndexNumber} ${it.Name}",
                            publishedTime = 0,
                            properties = MediaProperties(
                                subtitleLanguageIds = listOf("CHS"), // TODO: Support Jellyfin subtitles 
                                resolution = "1080P",
                                alliance = mediaSourceId,
                                size = FileSize.Unspecified,
                            ),
                            episodeRange = episodeRange,
                            location = MediaSourceLocation.Lan, // cost 更低
                            kind = MediaSourceKind.WEB,
                        ),
                        kind = MatchKind.FUZZY,
                    )
                }
                .filter { it.matches(query) != false }
                .asFlow()
        }
    }

    private suspend fun doSearch(
        subjectName: String? = null,
        recursive: Boolean = true,
        parentId: String? = null,
    ) = client.get("$baseUrl/Items") {
        parameter("userId", userId) // required
        parameter("enableImages", false)
        parameter("recursive", recursive)
        parameter("searchTerm", subjectName)
//        parameter("mediaTypes", "Video")
        parameter("fields", "CanDownload")
        parameter("parentId", parentId)
    }.body<SearchResponse>()
}

@Serializable
private class SearchResponse(
    val Items: List<Item> = emptyList(),
)

@Serializable
@Suppress("PropertyName")
private data class Item(
    val Name: String,
    val Id: String,
    val OriginalTitle: String? = null, // 日文
    val IndexNumber: Int? = null,
    val ParentIndexNumber: Int? = null,
    val Type: String, // "Episode", "Series", ...
    val CanDownload: Boolean = false,
)
