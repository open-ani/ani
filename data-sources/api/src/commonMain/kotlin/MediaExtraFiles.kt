package me.him188.ani.datasources.api

import kotlinx.serialization.Serializable

/**
 * @see Media.extraFiles
 */
@Serializable
class MediaExtraFiles(
    val subtitles: List<Subtitle> = emptyList(),
) {
    companion object {
        val Empty = MediaExtraFiles()
    }
}

@Serializable
data class Subtitle(
    /**
     * e.g. `https://example.com/1.ass`
     */
    val uri: String,
    val mimeType: String? = null,
    val language: String? = null,
)
