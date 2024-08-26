package me.him188.ani.datasources.api.source

import io.ktor.client.HttpClientConfig
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.source.parameter.MediaSourceParameter
import me.him188.ani.datasources.api.source.parameter.MediaSourceParameters
import me.him188.ani.utils.ktor.ClientProxyConfig
import me.him188.ani.utils.ktor.proxy
import me.him188.ani.utils.ktor.userAgent
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class FactoryId(
    // 强制类型检查, 防止跟 mediaSourceId 弄混 (历史遗留问题, 有太多的 mediaSourceId 了)
    val value: String,
) {
    override fun toString(): String = value
}

/**
 * 表示一个数据源类型, 可以构造多个该类型的实例. 例如 RSS 类型可以构造出适配不同站点的实例.
 *
 * 负责定义 [MediaSource] 的可配置参数列表, 并使用这些参数创建一个示例.
 *
 * @see MediaSource
 */
interface MediaSourceFactory { // SPI service load
    /**
     * 不一定和 [MediaSource.mediaSourceId] 相同, 因为一个数据源类型可能有多个实例.
     */
    val factoryId: FactoryId

    /**
     * 是否允许用户使用此模板创建多个实例. 不同的实例可以用不同的配置.
     *
     * 如果设置为 `true`, 构造的 [MediaSource] 就必须采用 [create] 时传入的 `mediaSourceId` 参数.
     */
    val allowMultipleInstances: Boolean get() = false

    /**
     * 数据源的可配置参数列表. 例如 API key, 用户名密码等.
     */
    val parameters: MediaSourceParameters get() = MediaSourceParameters.Empty

    /**
     * 初始的配置信息, 仅在创建数据源时有用. 之后将使用 [MediaSource.info]
     */
    val info: MediaSourceInfo

    /**
     * 必须返回新实例.
     *
     * @param mediaSourceId 系统为数据源分配的唯一 ID. 注意, 历史遗留的, 不支持多个实例的旧数据源实现会忽略这个参数.
     */
    fun create(
        mediaSourceId: String,
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
    arguments[parameter.name]?.let { parameter.parseFromString(it) } ?: parameter.default()

fun HttpClientConfig<*>.applyMediaSourceConfig(
    config: MediaSourceConfig,
) {
    config.proxy?.let { proxy(it) }
    config.userAgent?.let { userAgent(it) }
}
