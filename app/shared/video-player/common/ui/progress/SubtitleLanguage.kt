package me.him188.ani.app.videoplayer.ui.progress

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import me.him188.ani.app.videoplayer.ui.state.SubtitleTrack
import me.him188.ani.datasources.api.topic.SubtitleLanguage

@Immutable
class SubtitlePresentation(
    val subtitleTrack: SubtitleTrack,
    val displayName: String,
)

@Stable
val SubtitleTrack.subtitleLanguage: SubtitleLanguage
    get() {
        for (label in labels) {
            SubtitleLanguage.tryParse(label.value)?.let { return it }
        }
        return SubtitleLanguage.Other(labels.firstOrNull()?.value ?: language ?: "Unknown")
    }

