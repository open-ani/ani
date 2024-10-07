/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.models.preference

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.app.platform.SystemProxyDetector

/**
 * All proxy preferences
 */
@Immutable
@Serializable
data class ProxySettings(
    /**
     * Default settings to use if [MediaSourceProxySettings] is not set for a media source.
     */
    val default: MediaSourceProxySettings = MediaSourceProxySettings.Default,
    @Suppress("PropertyName") @Transient val _placeHolder: Int = 0,
) {
    companion object {
        @Stable
        val Default = ProxySettings()

        @Stable
        val Disabled = ProxySettings(default = MediaSourceProxySettings(enabled = false))
    }
}

@RequiresOptIn
@Target(AnnotationTarget.PROPERTY)
annotation class ProxyConfigRegardlessOfEnabled

@Immutable
@Serializable
data class MediaSourceProxySettings(
    val enabled: Boolean = false,
    /**
     * Use [configIfEnabledOrNull]
     */
    @property:ProxyConfigRegardlessOfEnabled val config: ProxyConfig = ProxyConfig.Default,
) {
    companion object {
        @Stable
        val Default by lazy {
            val defaultProxy = SystemProxyDetector.instance.detect()
            // 如果检测到了则启用检测到的代理, 否则使用默认的代理 (但保持禁用)
            if (defaultProxy == null) {
                MediaSourceProxySettings()
            } else {
                MediaSourceProxySettings(
                    enabled = true,
                    config = ProxyConfig(url = defaultProxy.url.toString()),
                )
            }
        }
    }
}

@OptIn(ProxyConfigRegardlessOfEnabled::class)
val MediaSourceProxySettings.configIfEnabledOrNull get() = if (enabled) config else null

@Immutable
@Serializable
data class ProxyConfig(
    val url: String = "http://127.0.0.1:7890",
    val authorization: ProxyAuthorization? = null,
) {
    companion object {
        val Default = ProxyConfig()
    }
}

@Immutable
@Serializable
data class ProxyAuthorization(
    val username: String,
    val password: String,
)
