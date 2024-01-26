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
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import kotlin.time.Duration


@Serializable
class DandanplayDanmaku(
    val cid: Long,
    val p: String,
    val m: String, // content
)

fun DandanplayDanmaku.toDanmakuOrNull(): Danmaku? {
    /*
    p参数格式为出现时间,模式,颜色,用户ID，各个参数之间使用英文逗号分隔

弹幕出现时间：格式为 0.00，单位为秒，精确到小数点后两位，例如12.34、445.6、789.01
弹幕模式：1-普通弹幕，4-底部弹幕，5-顶部弹幕
颜色：32位整数表示的颜色，算法为 Rx256x256+Gx256+B，R/G/B的范围应是0-255
用户ID：字符串形式表示的用户ID，通常为数字，不会包含特殊字符

     */
    val (time, mode, color, userId) = p.split(",").let {
        if (it.size < 4) return null else it
    }

    return Danmaku(
        time = time.toDoubleOrNull() ?: return null,
        senderId = userId,
        location = when (mode.toIntOrNull()) {
            1 -> DanmakuLocation.NORMAL
            4 -> DanmakuLocation.BOTTOM
            5 -> DanmakuLocation.TOP
            else -> return null
        },
        text = m,
        color = color.toIntOrNull() ?: return null
    )
}

@Serializable
class DandanplayDanmakuListResponse(
    val count: Int,
    val comments: List<DandanplayDanmaku>
)

@Serializable
data class DandanplayEpisode(
    val animeId: Long,
    val animeTitle: String,
    val episodeId: Long,
    val episodeTitle: String,
    val shift: Double,
    val type: String,
    val typeDescription: String
)

@Serializable
class DandanplayMatchVideoResponse(
    val isMatched: Boolean,
    val matches: List<DandanplayEpisode>,
    val errorCode: Int,
    val success: Boolean,
    val errorMessage: String,
)

class DandanplayClient(
    httpClientConfiguration: HttpClientConfig<*>.() -> Unit = {},
) {
    private val logger = logger(this::class)
    private val client = HttpClient(CIO) {
        httpClientConfiguration()
        install(HttpRequestRetry) {
            maxRetries = 3
            delayMillis { 2000 }
        }
        install(HttpTimeout)
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

    suspend fun matchVideo(
        filename: String,
        fileHash: String?,
        fileSize: Long,
        videoDuration: Duration
    ): DandanplayMatchVideoResponse {
        val response = client.get("https://api.acplay.net/api/v2/danmaku/match") {
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
        val response = client.get("https://api.acplay.net/api/v2/comment/${episodeId}") {
            accept(ContentType.Application.Json)
        }.body<DandanplayDanmakuListResponse>()
        return response.comments
    }
}