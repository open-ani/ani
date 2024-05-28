package me.him188.ani.danmaku.dandanplay

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.him188.ani.danmaku.dandanplay.data.DandanplayDanmaku
import me.him188.ani.danmaku.dandanplay.data.DandanplayDanmakuListResponse
import me.him188.ani.danmaku.dandanplay.data.DandanplayGetBangumiResponse
import me.him188.ani.danmaku.dandanplay.data.DandanplayMatchVideoResponse
import me.him188.ani.danmaku.dandanplay.data.DandanplaySearchEpisodeResponse
import me.him188.ani.danmaku.dandanplay.data.DandanplaySeasonSearchResponse
import java.util.Locale
import kotlin.time.Duration

internal class DandanplayClient(
    private val client: HttpClient,
) {
    suspend fun getSeasonAnimeList(
        year: Int,
        month: Int,
    ): DandanplaySeasonSearchResponse {
        // https://api.dandanplay.net/api/v2/bangumi/season/anime/2024/04
        val response = client.get("https://api.dandanplay.net/api/v2/bangumi/season/anime/$year/$month") {
            accept(ContentType.Application.Json)
        }

        return response.body<DandanplaySeasonSearchResponse>()
    }

    suspend fun searchSubject(
        subjectName: String,
    ): DandanplaySearchEpisodeResponse {
        val response = client.get("https://api.dandanplay.net/api/v2/search/subject") {
            accept(ContentType.Application.Json)
            parameter("keyword", subjectName)
        }

        if (response.status == HttpStatusCode.NotFound) {
            return DandanplaySearchEpisodeResponse()
        }

        return response.body<DandanplaySearchEpisodeResponse>()
    }

    suspend fun searchEpisode(
        subjectName: String,
        episodeName: String?,
    ): DandanplaySearchEpisodeResponse {
        val response = client.get("https://api.dandanplay.net/api/v2/search/episodes") {
            accept(ContentType.Application.Json)
            parameter("anime", subjectName)
            parameter("episode", episodeName)
        }

        return response.body<DandanplaySearchEpisodeResponse>()
    }

    suspend fun getBangumiEpisodes(
        bangumiId: Int, // 注意, 这是 dandanplay 的 id, 不是 Bangumi.tv 的 id
    ): DandanplayGetBangumiResponse {
        val response = client.get("https://api.dandanplay.net/api/v2/bangumi/$bangumiId") {
            accept(ContentType.Application.Json)
        }

        return response.body<DandanplayGetBangumiResponse>()
    }

    suspend fun matchVideo(
        filename: String,
        fileHash: String?,
        fileSize: Long,
        videoDuration: Duration
    ): DandanplayMatchVideoResponse {
        val response = client.post("https://api.dandanplay.net/api/v2/match") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("fileName", filename)
                put("fileHash", fileHash)
                put("fileSize", fileSize)
                put("videoDuration", videoDuration.inWholeSeconds)
                put("matchMode", "hashAndFileName")
            })
        }

        return response.body<DandanplayMatchVideoResponse>()
    }

    suspend fun getDanmakuList(
        episodeId: Long,
    ): List<DandanplayDanmaku> {
        // See #122
//        val chConvert = when (getSystemChineseVariant()) {
//            ChineseVariant.SIMPLIFIED -> 1
//            ChineseVariant.TRADITIONAL -> 2
//            null -> 0
//        }
        val chConvert = 0
        val response =
            client.get("https://api.dandanplay.net/api/v2/comment/${episodeId}?chConvert=$chConvert&withRelated=true") {
                accept(ContentType.Application.Json)
            }.body<DandanplayDanmakuListResponse>()
        return response.comments
    }

    enum class ChineseVariant {
        SIMPLIFIED,
        TRADITIONAL
    }

    private fun getSystemChineseVariant(): ChineseVariant? {
        val locale = Locale.getDefault()
        if (locale.language != "zh") return null
        return when (locale.country) {
            "CN" -> ChineseVariant.SIMPLIFIED
            "TW", "HK", "MO" -> ChineseVariant.TRADITIONAL
            else -> null
        }
    }
}