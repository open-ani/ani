/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.session

import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.plugin
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.reflect.typeInfo
import kotlinx.serialization.json.Json
import me.him188.ani.app.data.models.runApiRequest
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.platform.getAniUserAgent
import me.him188.ani.client.apis.BangumiOAuthAniApi
import me.him188.ani.client.apis.TrendsAniApi
import me.him188.ani.client.models.AniBangumiUserToken
import me.him188.ani.client.models.AniRefreshBangumiTokenRequest
import me.him188.ani.utils.ktor.registerLogging
import me.him188.ani.utils.logging.logger

class AniAuthClient : AutoCloseable {
    private val logger = logger<AniAuthClient>()
    private val httpClient = HttpClient {
        install(UserAgent) {
            agent = getAniUserAgent(currentAniBuildConfig.versionName)
        }
        install(HttpRequestRetry) {
            maxRetries = 3
            delayMillis { 2000 }
        }
        install(HttpTimeout)
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                },
            )
        }
        expectSuccess = true
    }.apply {
        registerLogging(logger)
        plugin(HttpSend).intercept { request ->
            val originalCall = execute(request)
            if (originalCall.response.status.value !in 100..399) {
                execute(request)
            } else {
                originalCall
            }
        }
    }

    val trendsApi = TrendsAniApi(
        baseUrl = currentAniBuildConfig.aniAuthServerUrl,
        httpClient,
    )

    private val oauthApi = BangumiOAuthAniApi(
        baseUrl = currentAniBuildConfig.aniAuthServerUrl,
        httpClient,
    )

    suspend fun getResult(requestId: String) = runApiRequest {
        try {
            oauthApi.getBangumiToken(requestId)
                .typedBody<AniBangumiUserToken>(typeInfo<AniBangumiUserToken>())
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                return@runApiRequest null
            }
            throw e
        }
    }

    suspend fun refreshAccessToken(refreshToken: String) = runApiRequest {
        oauthApi.refreshBangumiToken(AniRefreshBangumiTokenRequest(refreshToken)).body()
    }

    override fun close() {
        httpClient.close()
    }

}
