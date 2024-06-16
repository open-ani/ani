package me.him188.ani.danmaku.ani.client

import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuProviderConfig
import me.him188.ani.danmaku.api.applyDanmakuProviderConfig
import me.him188.ani.danmaku.protocol.AniUser
import me.him188.ani.danmaku.protocol.BangumiLoginRequest
import me.him188.ani.danmaku.protocol.BangumiLoginResponse
import me.him188.ani.danmaku.protocol.DanmakuInfo
import me.him188.ani.danmaku.protocol.DanmakuPostRequest
import me.him188.ani.utils.coroutines.runUntilSuccess
import me.him188.ani.utils.ktor.createDefaultHttpClient
import me.him188.ani.utils.ktor.registerLogging
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.random.Random

interface AniDanmakuSender : AutoCloseable {
    val selfId: Flow<String?>

    @Throws(SendDanmakuException::class)
    suspend fun send(
        episodeId: Int,
        info: DanmakuInfo
    ): Danmaku
}

sealed class SendDanmakuException : Exception()
class AuthorizationFailureException(override val cause: Throwable?) : SendDanmakuException()
class RequestFailedException(
    override val message: String?,
    override val cause: Throwable? = null
) : SendDanmakuException()

class NetworkErrorException(override val cause: Throwable?) : SendDanmakuException()

class AniDanmakuSenderImpl(
    private val config: DanmakuProviderConfig,
    private val bangumiToken: Flow<String?>,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : AniDanmakuSender, HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    private fun getBaseUrl() = AniBangumiSeverBaseUrls.getBaseUrl(config.useGlobal)

    companion object {
        private val logger = logger(this::class)
    }

    private val client = createDefaultHttpClient {
        applyDanmakuProviderConfig(config)
        followRedirects = true
        expectSuccess = false
        install(HttpTimeout) {
            connectTimeoutMillis = 20_000
            requestTimeoutMillis = 30_000
        }
    }.apply {
        registerLogging(logger)
    }

    private suspend fun getUserInfo(token: String): AniUser {
        return invokeRequest {
            client.get("${getBaseUrl()}/v1/me") {
                bearerAuth(token)
            }
        }.body<AniUser>()
    }

    private inline fun invokeRequest(
        block: () -> HttpResponse
    ): HttpResponse {
        val resp = try {
            block()
        } catch (e: Throwable) {
            throw NetworkErrorException(e)
        }
        if (resp.status.value == 401) {
            throw AuthorizationFailureException(null)
        }
        if (!resp.status.isSuccess()) {
            throw RequestFailedException(resp.toString())
        }
        return resp
    }

    private suspend fun authByBangumiToken(
        bangumiToken: String
    ): String {
        return invokeRequest {
            client.post("${getBaseUrl()}/v1/login/bangumi") {
                contentType(ContentType.Application.Json)
                setBody(
                    BangumiLoginRequest(
                        bangumiToken,
                        clientVersion = currentAniBuildConfig.versionName,
                        clientOS = Platform.currentPlatform.name,
                        clientArch = Platform.currentPlatform.arch.displayName,
                    )
                )
            }
        }.body<BangumiLoginResponse>().token
    }

    private suspend fun sendDanmaku(
        episodeId: Int,
        info: DanmakuInfo,
    ) {
        val token = requireSession().token
        invokeRequest {
            client.post("${getBaseUrl()}/v1/danmaku/$episodeId") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(DanmakuPostRequest(info))
            }
        }
    }

    private class Session(
        val token: String,
        val selfInfo: AniUser,
    )

    private val session = MutableStateFlow<Session?>(null)

    private suspend inline fun requireSession() =
        session.value ?: kotlin.run {
            login()
        }

    private val loginLock = Mutex()
    private suspend fun login(): Session = loginLock.withLock {
        session.value?.let { return it }
        val bangumiToken = bangumiToken.first()
            ?: throw AuthorizationFailureException(null)

        return runUntilSuccess(maxAttempts = 3) {
            val token = authByBangumiToken(bangumiToken)
            val selfInfo = getUserInfo(token)
            val session = Session(token, selfInfo)
            this.session.value = session
            session
        }
    }

    init {
        launchInBackground {
            kotlin.runCatching { login() }.onFailure {
                logger.error(it) { "Failed to login to danmaku sever (on startup). Will try later when sending danmaku." }
            }
        }
    }

    override val selfId = session.map { it?.selfInfo?.id }

    private val sendLock = Mutex()
    override suspend fun send(episodeId: Int, info: DanmakuInfo): Danmaku = sendLock.withLock {
        val selfId = requireSession().selfInfo.id

        sendDanmaku(episodeId, info)

        Danmaku(
            id = "self" + Random.nextInt(),
            providerId = AniDanmakuProvider.ID,
            playTimeMillis = info.playTime,
            senderId = selfId,
            location = info.location.toApi(),
            text = info.text,
            color = info.color,
        )
    }

    override fun close() {
        client.close()
    }
}