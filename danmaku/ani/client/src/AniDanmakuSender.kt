package me.him188.ani.danmaku.ani.client

import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.him188.ani.danmaku.api.DanmakuProviderConfig
import me.him188.ani.danmaku.api.applyDanmakuProviderConfig
import me.him188.ani.danmaku.protocol.DanmakuInfo
import me.him188.ani.utils.ktor.createDefaultHttpClient
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger

interface AniDanmakuSender {
    @Throws(SendDanmakuException::class)
    suspend fun send(info: DanmakuInfo)
}

sealed class SendDanmakuException : Exception()
class AuthorizationFailureException(override val cause: Throwable?) : SendDanmakuException()
class RequestFailedException(
    override val message: String?,
    override val cause: Throwable? = null
) : SendDanmakuException()

class NetworkErrorException(override val cause: Throwable?) : SendDanmakuException()

class AniDanmakuSenderImpl(
    config: DanmakuProviderConfig,
    getBangumiToken: suspend () -> String?,
) : AniDanmakuSender {
    companion object {
        private val logger = logger(this::class)
    }

    private val client = createDefaultHttpClient {
        applyDanmakuProviderConfig(config)
        Logging {
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    Companion.logger.info { message }
                }
            }
            level = LogLevel.INFO
        }
    }

    private var token: MutableStateFlow<String?> = MutableStateFlow(null)

    private suspend fun authByBangumiToken(): String {
        try {
            TODO()
        } catch (e: Throwable) {
            throw AuthorizationFailureException(e)
        }
    }

    private suspend fun sendDanmaku(info: DanmakuInfo) {
        val resp = try {
            TODO()
        } catch (e: Throwable) {
            throw NetworkErrorException(e)
        }

    }


    private val sendLock = Mutex()
    override suspend fun send(info: DanmakuInfo) = sendLock.withLock {
        if (token.value == null) {
            token.value = authByBangumiToken()
        }
        sendDanmaku(info)
        TODO()
    }
}