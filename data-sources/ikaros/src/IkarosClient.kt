package me.him188.ani.datasources.ikaros

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.source.MatchKind
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseSimplified
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseTraditional
import me.him188.ani.datasources.ikaros.models.IkarosSubjectDetails
import org.apache.http.HttpHeaders
import org.apache.http.HttpStatus
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.Collections

class IkarosClient(private val baseUrl: String, private val username: String, private val password: String) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(IkarosClient::class.java)
    }
    
    private val client: HttpClient = HttpClients.createDefault()
    private var authStr = "Basic "

    init {
        authStr +=
            Base64.getEncoder().encodeToString("$username:$password".toByteArray(StandardCharsets.UTF_8))
    }

    fun checkConnection(): Int {
        val get = HttpGet(baseUrl)
        try {
            return client.execute(get).statusLine.statusCode
        } catch (e: IOException) {
            logger.error("Check connection failed", e)
            return HttpStatus.SC_SERVICE_UNAVAILABLE
        }
    }


    
    fun postSubjectSyncBgmTv(bgmTvSubjectId:String): IkarosSubjectDetails? {
        if (bgmTvSubjectId.isEmpty()) {
            return null
        }
        
        val post = HttpPost(baseUrl
        + "/api/v1alpha1/subject/sync/platform?platform=BGM_TV&platformId=" + bgmTvSubjectId)
        post.addHeader(HttpHeaders.AUTHORIZATION, authStr)

        val response = client.execute(post);
        if (response.statusLine.statusCode != HttpStatus.SC_OK) {
            logger.error(
                "Post Ikaros Subject Sync By BgmTv failed for http status code: {} and message: {}",
                response.statusLine.statusCode,
                response.statusLine.reasonPhrase
            )
            return null
        }
        val readAllBytes = response.entity.content.readAllBytes()
        val json = String(readAllBytes, StandardCharsets.UTF_8)
        
        val subjectDetails:IkarosSubjectDetails = Json{ignoreUnknownKeys = true}.decodeFromString(json)
        return subjectDetails
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
                        originalUrl = baseUrl + epRes.url,
                        download = ResourceLocation.HttpStreamingFile(
                            uri = baseUrl + epRes.url
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
