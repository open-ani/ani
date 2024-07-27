package me.him188.ani.app.data.source

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.plugin
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.reflect.typeInfo
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.platform.getAniUserAgent
import me.him188.ani.client.apis.BangumiOAuthAniApi
import me.him188.ani.client.models.AniBangumiUserToken
import me.him188.ani.utils.ktor.registerLogging
import me.him188.ani.utils.logging.logger

class AniAuthClient : Closeable {
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
    )

    suspend fun getResultOrNull(requestId: String): AniBangumiUserToken? {
        return try {
            client.v1LoginBangumiOauthTokenGet(requestId)
                .typedBody(typeInfo<AniBangumiUserToken>())
        } catch (e: ResponseException) {
            null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    override fun close() {
        httpClient.close()
    }
}
