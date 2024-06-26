package me.him188.ani.app.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class VideoPlayerSettings(
    val driver: WebViewDriver = WebViewDriver.AUTO,

    @Suppress("PropertyName")
    @Transient val _placeholder: Int = 0,
) {
    companion object {
        val Default = VideoPlayerSettings()
    }
}

@Serializable
enum class WebViewDriver {
    CHROME,
    EDGE,
    SAFARI,
    AUTO;
    // Maybe a custom executable file
    // CUSTOM;

    override fun toString(): String {
        return this.name.lowercase()
    }

    companion object {
        val enabledEntries by lazy(LazyThreadSafetyMode.NONE) {
            // Safari driver should also be usable when there is no proxy
            entries.filter { it != SAFARI }.sortedDescending()
        }
    }
}