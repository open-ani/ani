package me.him188.ani.app.data.models.preference

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Immutable
data class VideoPlayerSelectionSettings(
    val core: VideoPlayerCore = VideoPlayerCore.VLC_ANDROID,

    @Suppress("PropertyName")
    @Transient val _placeholder: Int = 0,
) {
    companion object {
        val Default = VideoPlayerSelectionSettings()
    }
}

@Serializable
enum class VideoPlayerCore {
    EXO,
    VLC_ANDROID;

    override fun toString(): String {
        return this.name.lowercase()
    }

    companion object {
        val enabledEntries by lazy(LazyThreadSafetyMode.NONE) {
            entries.sortedDescending()
        }
    }
}