package me.him188.ani.app.videoplayer.ui.progress

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import me.him188.ani.app.videoplayer.ui.state.AudioTrack

@Immutable
class AudioPresentation(
    val audioTrack: AudioTrack,
    val displayName: String,
)

@Stable
val AudioTrack.videoName: String
    get() = name ?: labels.firstOrNull()?.value ?: internalId

