package me.him188.ani.app.platform

import androidx.compose.runtime.Stable
import io.ktor.client.plugins.UserAgent
import kotlinx.coroutines.flow.Flow
import me.him188.ani.utils.ktor.ClientProxyConfig
import me.him188.ani.utils.ktor.proxy
import me.him188.ani.utils.platform.currentPlatform
import kotlin.coroutines.CoroutineContext


@Stable
interface AniBuildConfig {
    /**
     * `3.0.0-rc04`
     */
    val versionName: String
    val isDebug: Boolean
    val aniAuthServerUrl: String

    companion object {
        @Stable
        fun current(): AniBuildConfig = currentAniBuildConfig
    }
}

/**
 * E.g. `30000` for `3.0.0`, `30102` for `3.1.2`
 */
val AniBuildConfig.versionCode: String
    get() = buildString {
        val split = versionName.substringBefore("-").split(".")
        if (split.size == 3) {
            split[0].toIntOrNull()?.let {
                append(it.toString())
            }
            split[1].toIntOrNull()?.let {
                append(it.toString().padStart(2, '0'))
            }
            split[2].toIntOrNull()?.let {
                append(it.toString().padStart(2, '0'))
            }
        } else {
            for (section in split) {
                section.toIntOrNull()?.let {
                    append(it.toString().padStart(2, '0'))
                }
            }
        }
    }

@Stable
@PublishedApi
internal expect val currentAniBuildConfigImpl: AniBuildConfig

@Stable
inline val currentAniBuildConfig: AniBuildConfig get() = currentAniBuildConfigImpl

/**
 * 满足各个数据源建议格式的 User-Agent, 所有 HTTP 请求都应该带此 UA.
 */
fun getAniUserAgent(
    version: String = currentAniBuildConfig.versionName,
    platform: String = currentPlatform().nameAndArch,
): String = "open-ani/ani/$version ($platform) (https://github.com/open-ani/ani)"
