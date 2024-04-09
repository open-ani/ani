package me.him188.ani.app.data.media

import androidx.compose.runtime.Immutable
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.ResourceLocation

/**
 * Describes a media for a specific episode.
 *
 * Episodes can have different medias from different sources. For example, there are many subtitle alliances.
 */
@Immutable
class Media(
    /**
     * Globally unique id which includes the source, like "dmhy.1"
     */
    val id: String,
    val mediaSourceId: String, // e.g. "dmhy"
    val originalUrl: String,
    val download: ResourceLocation,
    val originalTitle: String,
    val size: FileSize,
    val publishedTime: Long,
    val properties: MediaProperties,
)

/**
 * Properties of [Media] that might be useful for filtering.
 */
@Immutable
class MediaProperties(
    /**
     * Empty list means no subtitles
     */
    val subtitleLanguages: List<String>,
    /**
     * Resolution, e.g. "1080P", "4K"
     */
    val resolution: String,
    /**
     * Subtitle group
     */
    val alliance: String,
)
