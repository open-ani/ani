package me.him188.ani.datasources.ikaros

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.MediaExtraFiles
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.Subtitle
import me.him188.ani.datasources.api.SubtitleKind
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.source.MatchKind
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.titles.RawTitleParser
import me.him188.ani.datasources.api.topic.titles.parse
import me.him188.ani.datasources.ikaros.models.IkarosSubjectDetails
import me.him188.ani.datasources.ikaros.models.IkarosVideoSubtitle
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import models.IkarosAttachment

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
        return client.post(url).body<IkarosSubjectDetails>()
    }

    private suspend fun getAttachmentById(attId: Long): IkarosAttachment? {
        if (attId <= 0) return null;
        val url = baseUrl.plus("/api/v1alpha1/attachment/").plus(attId);
        return client.get(url).body<IkarosAttachment>();
    }

    private suspend fun getAttachmentVideoSubtitlesById(attId: Long): List<IkarosVideoSubtitle>? {
        if (attId <= 0) return null;
        val url = baseUrl.plus("/api/v1alpha1/attachment/relation/videoSubtitle/subtitles/").plus(attId);
        return client.get(url).body<List<IkarosVideoSubtitle>>();
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

    suspend fun subjectDetails2SizedSource(subjectDetails: IkarosSubjectDetails, seq: Int): SizedSource<MediaMatch> {
        val episodes = subjectDetails.episodes
        val mediaMatchs = mutableListOf<MediaMatch>()
        val episode = episodes.find { ep -> ep.sequence == seq && "MAIN" == ep.group }
        if (episode?.resources != null && episode.resources.isNotEmpty()) {
            for (epRes in episode.resources) {
                val media = epRes?.let {
                    val attachment: IkarosAttachment? = getAttachmentById(epRes.attachmentId);
                    val parseResult = RawTitleParser.getDefault().parse(epRes.name);
                    DefaultMedia(
                        mediaId = epRes.attachmentId.toString(),
                        mediaSourceId = IkarosMediaSource.ID,
                        originalUrl = baseUrl.plus("/console/#/subjects/subject/details/").plus(subjectDetails.id),
                        download = ResourceLocation.HttpStreamingFile(
                            uri = getResUrl(epRes.url),
                        ),
                        originalTitle = epRes.name,
                        publishedTime = DateFormater.default.utcDateStr2timeStamp(attachment?.updateTime ?: ""),
                        properties = MediaProperties(
                            subtitleLanguageIds = parseResult.subtitleLanguages.map { it.id },
                            resolution = parseResult.resolution?.displayName ?: "480P",
                            alliance = IkarosMediaSource.ID,
                            size = FileSize(attachment?.size ?: 0),
                            subtitleKind = SubtitleKind.EXTERNAL_PROVIDED,
                        ),
                        episodeRange = parseResult.episodeRange,
                        location = MediaSourceLocation.Online,
                        kind = MediaSourceKind.WEB,
                        extraFiles = fetchVideoAttSubtitles2ExtraFiles(epRes.attachmentId),
                    )
                }
                val mediaMatch = media?.let { MediaMatch(it, MatchKind.FUZZY) }
                if (mediaMatch != null) {
                    mediaMatchs.add(mediaMatch)
                }
            }
        }

        val sizedSource = IkarosSizeSource(
            totalSize = flowOf(mediaMatchs.size), finished = flowOf(true), results = mediaMatchs.asFlow(),
        )

        return sizedSource
    }

    private suspend fun fetchVideoAttSubtitles2ExtraFiles(attachmentId: Long): MediaExtraFiles {
        if (attachmentId <= 0) return MediaExtraFiles();
        val attVideoSubtitleList = getAttachmentVideoSubtitlesById(attachmentId)
        val subtitles: MutableList<Subtitle> = mutableListOf();
        if (!attVideoSubtitleList.isNullOrEmpty()) {
            for (ikVideoSubtitle in attVideoSubtitleList) {
                // convert ikarosVideoSubtitle to ani subtitle
                subtitles.add(
                    Subtitle(
                        uri = getResUrl(ikVideoSubtitle.url),
                        language = AssNameParser.default.parseAssName2Language(ikVideoSubtitle.name),
                        mimeType = AssNameParser.httpMineType,
                    ),
                )
            }
        }
        return MediaExtraFiles(subtitles);
    }
}

class IkarosSizeSource(
    override val results: Flow<MediaMatch>, override val finished: Flow<Boolean>, override val totalSize: Flow<Int?>
) : SizedSource<MediaMatch>

