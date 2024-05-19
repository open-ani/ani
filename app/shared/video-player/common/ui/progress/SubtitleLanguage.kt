package me.him188.ani.app.videoplayer.ui.progress

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import me.him188.ani.app.videoplayer.ui.state.SubtitleTrack

@Immutable
class SubtitlePresentation(
    val subtitleTrack: SubtitleTrack,
    val displayName: String,
)

@Stable
val SubtitleTrack.subtitleLanguage: String
    get() = labels.firstOrNull()?.value ?: language ?: id.substringAfterLast("-").takeLast(4)

