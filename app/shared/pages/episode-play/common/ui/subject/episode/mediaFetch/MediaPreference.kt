package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class MediaPreference(
    val alliance: String? = null,
    val resolution: String? = null,
    val subtitleLanguage: String? = null,
    val mediaSourceId: String? = null,
) {
    companion object {
        val Empty = MediaPreference()
    }
}