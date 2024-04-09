package me.him188.ani.datasources.api

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.ResourceLocation

/**
 * Describes the media (video) for a specific episode.
 *
 * Episodes can have different medias from different sources. For example, there are many subtitle alliances.
 */
@Immutable
sealed interface Media {
    /**
     * Globally unique id which includes the source, like "dmhy.1"
     */
    val mediaId: String
    val mediaSourceId: String // e.g. "dmhy"
    val originalUrl: String

    /**
     * Returns the location to download the media.
     */
    val download: ResourceLocation

    val originalTitle: String
    val size: FileSize
    val publishedTime: Long
    val properties: MediaProperties

    /**
     * Location of the media.
     * @see MediaSourceLocation
     */
    val location: MediaSourceLocation
}

/**
 * A media that is on the Internet so that downloading it is considered expensive.
 */
@Immutable
@Serializable
class OnlineMedia(
    override val mediaId: String,
    override val mediaSourceId: String, // e.g. "dmhy"
    override val originalUrl: String,
    override val download: ResourceLocation,
    override val originalTitle: String,
    override val size: FileSize,
    override val publishedTime: Long,
    override val properties: MediaProperties,
) : Media {
    override val location: MediaSourceLocation get() = MediaSourceLocation.ONLINE
}

/**
 * A media that is already cached on a easily accessible location,
 * e.g. on the local storage, or on a file server on the LAN.
 */
@Immutable
@Serializable
class LocalMedia(
    private val original: OnlineMedia,
    private val cacheMetadata: MediaCacheMetadata,
) : Media by original {
    override val mediaId: String = "${cacheMetadata.mediaSourceId}-${original.mediaId}"
    override val mediaSourceId: String get() = cacheMetadata.mediaSourceId
    override val location: MediaSourceLocation get() = MediaSourceLocation.LOCAL
}

/**
 * Properties of [Media] that might be useful for filtering.
 */
@Immutable
@Serializable
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

@Immutable
@Serializable
class MediaCacheMetadata(
    val mediaSourceId: String, // e.g. "localfs" for the local file system
)