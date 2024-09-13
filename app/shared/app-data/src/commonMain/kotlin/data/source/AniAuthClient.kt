package me.him188.ani.app.data.source

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

    private val client = BangumiOAuthAniApi(
        baseUrl = currentAniBuildConfig.aniAuthServerUrl,
        httpClient,
    )

    suspend fun getResult(requestId: String) = runApiRequest {
        try {
            client.getBangumiToken(requestId)
                .typedBody<AniBangumiUserToken>(typeInfo<AniBangumiUserToken>())
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                return@runApiRequest null
            }
            throw e
        }
    }

    suspend fun refreshAccessToken(refreshToken: String) = runApiRequest {
        client.refreshBangumiToken(AniRefreshBangumiTokenRequest(refreshToken)).body()
    }

    override fun close() {
        httpClient.close()
    }

}
