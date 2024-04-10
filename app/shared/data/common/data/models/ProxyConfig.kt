package me.him188.ani.app.data.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.source.MediaSource

/**
 * All proxy preferences
 */
@Immutable
@Serializable
data class ProxyPreferences(
    /**
     * Default preferences to use if [MediaSourceProxyPreferences] is not set for a media source.
     */
    val default: MediaSourceProxyPreferences = MediaSourceProxyPreferences.Default,
    /**
     * Per data source [MediaSource.mediaSourceId]
     */
    val perSource: Map<String, MediaSourceProxyPreferences> = emptyMap(),
) {
    fun get(sourceId: String): MediaSourceProxyPreferences {
        return perSource[sourceId] ?: default
    }

    companion object {
        val Default = ProxyPreferences()
    }
}

@Immutable
@Serializable
data class MediaSourceProxyPreferences(
    val enabled: Boolean = false,
    val config: ProxyConfig = ProxyConfig.Default,
) {
    companion object {
        val Default = MediaSourceProxyPreferences()
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
