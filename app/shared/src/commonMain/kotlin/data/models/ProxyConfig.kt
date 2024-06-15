package me.him188.ani.app.data.models

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    }
}

@Immutable
@Serializable
data class MediaSourceProxySettings(
    val enabled: Boolean = false,
    val config: ProxyConfig = ProxyConfig.Default,
) {
    companion object {
        @Stable
        val Default = MediaSourceProxySettings()
    }
}

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
