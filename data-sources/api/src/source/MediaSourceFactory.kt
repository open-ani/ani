package me.him188.ani.datasources.api.source

import io.ktor.client.HttpClientConfig
import me.him188.ani.utils.ktor.ClientProxyConfig
import me.him188.ani.utils.ktor.proxy
import me.him188.ani.utils.ktor.userAgent

/**
 * @see MediaSource
 */
interface MediaSourceFactory {
    /**
     * @see MediaSource.mediaSourceId
     */
    val mediaSourceId: String

    fun create(
        config: MediaSourceConfig,
    ): MediaSource
}

class MediaSourceConfig(
    val proxy: ClientProxyConfig? = null,
    val userAgent: String? = null
    // TODO: 增加每个数据源专有的配置, 并支持序列化
)


fun HttpClientConfig<*>.applyMediaSourceConfig(
    config: MediaSourceConfig,
) {
    config.proxy?.let { proxy(it) }
    config.userAgent?.let { userAgent(it) }
}
