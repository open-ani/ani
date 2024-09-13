package me.him188.ani.app.data.repository

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable
import me.him188.ani.app.data.models.ApiResponse
import me.him188.ani.app.data.models.runApiRequest
import me.him188.ani.utils.ktor.createDefaultHttpClient

interface EpisodeScreenshotRepository : Repository {
    suspend fun getScreenshots(magnetUri: String): ApiResponse<List<String>>
}

// https://whatslink.info/
// 这玩意虽然能跑但是限制阈值有点太低了, 估计实际使用的时候会很容易被限调用速度, 得考虑别的方案
class WhatslinkEpisodeScreenshotRepository : EpisodeScreenshotRepository {
    private val client = createDefaultHttpClient {
        followRedirects = true
        expectSuccess = true
    }

    @Serializable
    private data class WhatslinkResponse(
        val screenshots: List<WhatslinkScreenshot>
    )

    @Serializable
    private data class WhatslinkScreenshot(
        val time: String,
        val screenshot: String,
    )

    override suspend fun getScreenshots(magnetUri: String): ApiResponse<List<String>> {
        return runApiRequest {
            client.get("https://whatslink.info/api/v1/link") {
                parameter("url", magnetUri)
            }.body<WhatslinkResponse>()
                .screenshots.map { it.screenshot }
        }
    }
}
