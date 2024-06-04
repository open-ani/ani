package me.him188.ani.datasources.api.source

import io.ktor.client.HttpClientConfig
import kotlinx.serialization.Serializable
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

    /**
     * 数据源的可配置参数列表. 例如 API key, 用户名密码等.
     * @see MediaSourceParametersBuilder
     */
    val parameters: MediaSourceParameters get() = MediaSourceParameters.Empty

    fun create(
        config: MediaSourceConfig,
    ): MediaSource
}

@Serializable
class MediaSourceConfig(
    val proxy: ClientProxyConfig? = null,
    val userAgent: String? = null,
    /**
     * 用户为数据源配置的参数列表. 一定包含 [MediaSourceFactory.parameters] 中的所有参数.
     */
    val arguments: Map<String, String> = mapOf(),
)


fun HttpClientConfig<*>.applyMediaSourceConfig(
    config: MediaSourceConfig,
) {
    config.proxy?.let { proxy(it) }
    config.userAgent?.let { userAgent(it) }
}
