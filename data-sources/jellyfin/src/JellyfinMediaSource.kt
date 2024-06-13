package me.him188.ani.datasources.jellyfin

import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.asFlow
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.DefaultMedia
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
import me.him188.ani.datasources.api.source.MediaSourceParameters
import me.him188.ani.datasources.api.source.MediaSourceParametersBuilder
import me.him188.ani.datasources.api.source.get
import me.him188.ani.datasources.api.source.useHttpClient
import me.him188.ani.datasources.api.topic.ResourceLocation

class JellyfinMediaSource(config: MediaSourceConfig) : HttpMediaSource() {
    companion object {
        const val ID = "jellyfin"
    }

    object Parameters : MediaSourceParametersBuilder() {
        val baseUrl = string("baseUrl", description = "API base URL")
        val apikey = string("apikey", description = "API Key")
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

    private val client = useHttpClient(config) {
        defaultRequest {
            header(
                HttpHeaders.Authorization,
                "Bearer ${config[Parameters.apikey]}"
            )
        }
    }

    override suspend fun checkConnection(): ConnectionStatus {
        try {
            client.get(baseUrl).let { response ->
                return if (response.status == HttpStatusCode.OK) ConnectionStatus.SUCCESS
                else ConnectionStatus.FAILED
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return ConnectionStatus.FAILED
        }
    }

    override suspend fun fetch(query: MediaFetchRequest): SizedSource<MediaMatch> {
        // search by subject and episode number
        SinglePagePagedSource {
            query.subjectNames
                .flatMap { subjectName ->
                    val resp = client.get("$baseUrl/Items") {
                        parameter("enableImages", false)
                        parameter("recursive", true)
                        parameter("searchTerm", subjectName)
                        parameter("mediaTypes", "Video")
                    }.body<SearchResponse>()
                    resp.Items
                }.distinctBy { it.Id }
                .mapNotNull {
                    val download = client.get("$baseUrl/Items/${it.Id}/Download").body()
                        ?: return@mapNotNull null

                    MediaMatch(
                        media = DefaultMedia(
                            mediaId = it.Id,
                            mediaSourceId = mediaSourceId,
                            originalUrl = "$baseUrl/Items/${it.Id}",
                            download = ResourceLocation.HttpStreamingFile(
                                uri = download,
                            ),
                            originalTitle = it.OriginalTitle,
                            publishedTime = 0,
                            properties = MediaProperties(
                                subtitleLanguageIds = emptyList("CHS"),
                                resolution = "1080P",
                                alliance = mediaSourceId,
                                size =
                            )
                        ),
                        kind = MatchKind.FUZZY
                    )
                }
                .asFlow()
        }
    }
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
    val OriginalTitle: String,
    val IndexNumber: Int? = null,
    val ParentIndexNumber: Int? = null,
)
