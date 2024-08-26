package me.him188.ani.datasources.api.source

import io.ktor.client.HttpClientConfig
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.source.parameter.MediaSourceParameter
import me.him188.ani.datasources.api.source.parameter.MediaSourceParameters
import me.him188.ani.utils.ktor.ClientProxyConfig
import me.him188.ani.utils.ktor.proxy
import me.him188.ani.utils.ktor.userAgent

/**
 * 负责定义 [MediaSource] 的可配置参数列表, 并使用这些参数创建一个示例.
 *
 * @see MediaSource
 */
interface MediaSourceFactory { // SPI service load
    /**
     * @see MediaSource.mediaSourceId
     */
    val mediaSourceId: String

    /**
     * 是否允许用户使用此模板创建多个实例. 不同的实例可以用不同的配置.
     */
    val allowMultipleInstances: Boolean get() = false

    /**
     * 数据源的可配置参数列表. 例如 API key, 用户名密码等.
     * @see MediaSourceParametersBuilder
     */
    val parameters: MediaSourceParameters get() = MediaSourceParameters.Empty

    val info: MediaSourceInfo

    /**
     * 必须返回新实例.
     */
    fun create(
        config: MediaSourceConfig,
    ): MediaSource
}

@Serializable
data class MediaSourceConfig(
    val proxy: ClientProxyConfig? = null,
    val userAgent: String? = null,
    /**
     * 用户为数据源配置的参数列表. 一定包含 [MediaSourceFactory.parameters] 中的所有参数, 但如果数据源更新了更多参数, 则可能不会包含.
     */
    val arguments: Map<String, String?> = mapOf(),
) {
    companion object {
        val Default = MediaSourceConfig()
    }
}

operator fun <T> MediaSourceConfig.get(parameter: MediaSourceParameter<T>): T =
    arguments[parameter.name]?.let { parameter.parseFromString(it) } ?: parameter.default

fun HttpClientConfig<*>.applyMediaSourceConfig(
    config: MediaSourceConfig,
) {
    config.proxy?.let { proxy(it) }
    config.userAgent?.let { userAgent(it) }
}
