package me.him188.ani.danmaku.dandanplay

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.him188.ani.danmaku.dandanplay.data.DandanplayDanmaku
import me.him188.ani.danmaku.dandanplay.data.DandanplayDanmakuListResponse
import me.him188.ani.danmaku.dandanplay.data.DandanplayMatchVideoResponse
import me.him188.ani.danmaku.dandanplay.data.DandanplaySearchEpisodeResponse
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import java.util.Locale
import kotlin.time.Duration

class DandanplayClient(
    httpClientConfiguration: HttpClientConfig<*>.() -> Unit = {},
) {
    private val logger = logger(this::class)
    private val client = HttpClient(CIO) {
        httpClientConfiguration()
        install(HttpRequestRetry) {
            maxRetries = 1
            delayMillis { 2000 }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 20_000 // 弹弹服务器请求比较慢
            connectTimeoutMillis = 10_000 // 弹弹服务器请求比较慢
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        Logging {
            level = LogLevel.INFO
            logger = object : Logger {
                override fun log(message: String) {
                    this@DandanplayClient.logger.info { "[ktor] $message" }
                }
            }
        }
    }

    suspend fun searchEpisode(
        subjectName: String,
        episodeName: String,
    ): DandanplaySearchEpisodeResponse {
        val response = client.get("https://api.dandanplay.net/api/v2/search/episodes") {
            accept(ContentType.Application.Json)
            parameter("anime", subjectName)
            parameter("episode", episodeName)
        }

        return response.body<DandanplaySearchEpisodeResponse>()
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