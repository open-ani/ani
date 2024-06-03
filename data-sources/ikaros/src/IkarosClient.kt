package me.him188.ani.datasources.ikaros

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.call.receive
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.http.headersOf
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.source.MatchKind
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseSimplified
import me.him188.ani.datasources.ikaros.models.IkarosSubjectDetails
import me.him188.ani.utils.logging.error
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.Base64

class IkarosClient(private val baseUrl: String, private val username: String, private val password: String) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(IkarosClient::class.java)
    }

    private val client: HttpClient;
    private var authStr = "Basic "

    init {
        authStr +=
            Base64.getEncoder().encodeToString("$username:$password".toByteArray(StandardCharsets.UTF_8))
        client = HttpClient(CIO) {
            defaultRequest { 
                header(HttpHeaders.Authorization, authStr)
            }
        }
    }

    suspend fun checkConnection(): HttpStatusCode {
        return try {
            client.get(baseUrl).run {
                check(status.isSuccess()) { "Request failed: $this" }
            }
            HttpStatusCode.OK
        } catch (e: Exception) {
            logger.error(e) { "Failed to connect to $baseUrl" }
            HttpStatusCode.ServiceUnavailable
        }
    }


    suspend fun postSubjectSyncBgmTv(bgmTvSubjectId: String): IkarosSubjectDetails? {
        if (bgmTvSubjectId.isEmpty()) {
            return null
        }
        val url = "$baseUrl/api/v1alpha1/subject/sync/platform?platform=BGM_TV&platformId=$bgmTvSubjectId"
        
        val httpResponse: HttpResponse = client.post(url)
        if (!httpResponse.status.isSuccess()) {
            logger.error(
                "Post Ikaros Subject Sync By BgmTv failed for http status code: {} and message: {}",
                httpResponse.status.value,
                httpResponse.status.description
            )
            return null
        }
        val json = httpResponse.body<String>();
        val subjectDetails: IkarosSubjectDetails = Json { ignoreUnknownKeys = true }.decodeFromString(json)
        return subjectDetails
    }

    private fun getResUrl(url: String): String {
        if (url.isEmpty()) {
            return ""
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url
        }
        return baseUrl + url;
    }

    fun subjectDetails2SizedSource(subjectDetails: IkarosSubjectDetails, seq: Int): SizedSource<MediaMatch> {
        val episodes = subjectDetails.episodes
        val mediaMatchs = mutableListOf<MediaMatch>()
        val episode = episodes.find { ep -> ep.sequence == seq }
        if (episode != null) {
            for (epRes in episode.resources!!) {
                val media = epRes?.let {
                    DefaultMedia(
                        mediaId = epRes.attachmentId.toString(),
                        mediaSourceId = IkarosMediaSource.ID,
                        originalUrl = getResUrl(epRes.url),
                        download = ResourceLocation.HttpStreamingFile(
                            uri = getResUrl(epRes.url)
                        ),
                        originalTitle = epRes.name,
                        publishedTime = 0L,
                        properties = MediaProperties(
                            subtitleLanguageIds = listOf(ChineseSimplified).map { it.id },
                            resolution = "1080p",
                            alliance = IkarosMediaSource.ID,
                            size = FileSize.Zero,
                        ),
                        episodeRange = EpisodeRange.single(seq.toString()),
                        kind = MediaSourceKind.WEB,
                    )
                }
                val mediaMatch = media?.let { MediaMatch(it, MatchKind.FUZZY) };
                if (mediaMatch != null) {
                    mediaMatchs.add(mediaMatch)
                };
            }
        }

        val sizedSource = IkarosSizeSource(
            totalSize = flowOf(mediaMatchs.size),
            finished = flowOf(true),
            results = mediaMatchs.asFlow()
        )

        return sizedSource
    }
}

class IkarosSizeSource(
    override val results: Flow<MediaMatch>,
    override val finished: Flow<Boolean>,
    override val totalSize: Flow<Int?>
) : SizedSource<MediaMatch> {
}

