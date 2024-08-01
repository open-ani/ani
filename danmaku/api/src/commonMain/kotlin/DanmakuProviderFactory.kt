package me.him188.ani.danmaku.api

import io.ktor.client.HttpClientConfig
import kotlinx.coroutines.Dispatchers
import me.him188.ani.utils.ktor.ClientProxyConfig
import me.him188.ani.utils.ktor.proxy
import me.him188.ani.utils.ktor.userAgent
import kotlin.coroutines.CoroutineContext

interface DanmakuProviderFactory { // SPI interface
    /**
     * @see DanmakuProvider.id
     */
    val id: String

    fun create(
        config: DanmakuProviderConfig,
    ): DanmakuProvider
}

class DanmakuProviderConfig(
    val proxy: ClientProxyConfig? = null,
    val userAgent: String? = null,
    val useGlobal: Boolean = false,
    val coroutineContext: CoroutineContext = Dispatchers.Default,
)

fun HttpClientConfig<*>.applyDanmakuProviderConfig(
    config: DanmakuProviderConfig,
) {
    config.proxy?.let { proxy(it) }
    config.userAgent?.let { userAgent(it) }
}
