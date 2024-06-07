package me.him188.ani.datasources.ikaros

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.source.MatchKind
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseSimplified
import me.him188.ani.datasources.ikaros.models.IkarosSubjectDetails
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger

class IkarosClient(
    private val baseUrl: String,
    private val client: HttpClient,
) {
    companion object {
        private val logger = logger<IkarosClient>()
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
        if (bgmTvSubjectId.isBlank() || bgmTvSubjectId.toInt() <= 0) {
            return null
        }
        val url = "$baseUrl/api/v1alpha1/subject/sync/platform?platform=BGM_TV&platformId=$bgmTvSubjectId"

        val httpResponse: HttpResponse = client.post(url)
        if (!httpResponse.status.isSuccess()) {
            logger.error {
                "Post Ikaros Subject Sync By BgmTv failed for http status code: ${httpResponse.status.value} and message: ${httpResponse.status.description}"
            }
            return null
        }
        return httpResponse.body<IkarosSubjectDetails>()
    }

    private fun getResUrl(url: String): String {
        if (url.isEmpty()) {
            return ""
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url
        }
        return baseUrl + url
    }

    fun subjectDetails2SizedSource(subjectDetails: IkarosSubjectDetails, seq: Int): SizedSource<MediaMatch> {
        val episodes = subjectDetails.episodes
        val mediaMatchs = mutableListOf<MediaMatch>()
        val episode = episodes.find { ep -> ep.sequence == seq && "MAIN" == ep.group }
        if (episode?.resources != null && episode.resources.isNotEmpty()) {
            for (epRes in episode.resources) {
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
                        location = MediaSourceLocation.Online,
                        kind = MediaSourceKind.WEB,
                    )
                }
                val mediaMatch = media?.let { MediaMatch(it, MatchKind.FUZZY) }
                if (mediaMatch != null) {
                    mediaMatchs.add(mediaMatch)
                }
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
) : SizedSource<MediaMatch>

