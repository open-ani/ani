package me.him188.ani.app.data.persistent.preference

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Immutable
data class VideoResolverSettings(
    val driver: WebViewDriver = WebViewDriver.AUTO,

    @Suppress("PropertyName")
    @Transient val _placeholder: Int = 0,
) {
    companion object {
        val Default = VideoResolverSettings()
    }
}

@Serializable
enum class WebViewDriver {
    CHROME,
    EDGE,
    AUTO;
    // Maybe a custom executable file
    // CUSTOM;

    override fun toString(): String {
        return this.name.lowercase()
    }

    companion object {
        val enabledEntries by lazy(LazyThreadSafetyMode.NONE) {
            entries.sortedDescending()
        }
    }
}