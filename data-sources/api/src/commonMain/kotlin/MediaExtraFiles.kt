package me.him188.ani.datasources.api

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

/**
 * @see Media.extraFiles
 */
@Serializable
@Immutable
class MediaExtraFiles(
    val subtitles: List<Subtitle> = emptyList(),
) {
    companion object {
        @Stable
        val Empty = MediaExtraFiles()
    }
}

@Serializable
@Immutable
data class Subtitle(
    /**
     * e.g. `https://example.com/1.ass`
     */
    val uri: String,
    val mimeType: String? = null,
    val language: String? = null,
)
