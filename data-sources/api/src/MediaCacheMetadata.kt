package me.him188.ani.datasources.api

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.datasources.api.source.MediaFetchRequest

/**
 * Additional data stored when creating the cache to help matching caches with future [request][MediaFetchRequest]s.
 */ // See `MediaFetchRequest.matches` or `MediaCacheStorage`
@Immutable
@Serializable
class MediaCacheMetadata
/**
 * This constructor is only for serialization
 */
private constructor(
//    /**
//     * Id of the [MediaSource] that cached this media.
//     */
//    val cacheMediaSourceId: String, // e.g. "localfs" for the local file system
    /**
     * @see MediaFetchRequest.subjectId
     */
    val subjectId: String? = null,
    /**
     * @see MediaFetchRequest.episodeId
     */
    val episodeId: String? = null,
    val subjectNames: Set<String>,
    val episodeSort: EpisodeSort,
    val episodeEp: EpisodeSort? = episodeSort,
    val episodeName: String,
    val extra: Map<String, String> = emptyMap(),
    @Transient @Suppress("unused") private val _primaryConstructorMarker: Byte = 0, // avoid compiler error
) {
    constructor(
//    /**
//     * Id of the [MediaSource] that cached this media.
//     */
//    val cacheMediaSourceId: String, // e.g. "localfs" for the local file system
        /**
         * @see MediaFetchRequest.subjectId
         */
        subjectId: String? = null,
        /**
         * @see MediaFetchRequest.episodeId
         */
        episodeId: String? = null,
        subjectNames: Set<String>,
        episodeSort: EpisodeSort,
        episodeEp: EpisodeSort?,
        episodeName: String,
        extra: Map<String, String> = emptyMap(),
    ) : this(
        subjectId, episodeId, subjectNames, episodeSort, episodeEp, episodeName, extra,
        _primaryConstructorMarker = 0
    )

    /**
     * [other]'s null and empty properties are replaced by this instance's properties.
     */
    @Stable
    fun merge(other: MediaCacheMetadata): MediaCacheMetadata {
        return MediaCacheMetadata(
            subjectId = other.subjectId ?: subjectId,
            episodeId = other.episodeId ?: episodeId,
            subjectNames = other.subjectNames + subjectNames,
            episodeSort = other.episodeSort,
            episodeEp = other.episodeEp ?: episodeEp,
            episodeName = other.episodeName,
            extra = other.extra + extra,
        )
    }

    /**
     * Appends [other] to the existing [extra].
     */
    @Stable
    fun withExtra(other: Map<String, String>): MediaCacheMetadata {
        return MediaCacheMetadata(
            subjectId = subjectId,
            episodeId = episodeId,
            subjectNames = subjectNames,
            episodeSort = episodeSort,
            episodeEp = episodeEp,
            episodeName = episodeName,
            extra = extra + other,
        )
    }

    @Stable
    fun copy(
        subjectId: String? = this.subjectId,
        episodeId: String? = this.episodeId,
        subjectNames: Set<String> = this.subjectNames,
        episodeSort: EpisodeSort = this.episodeSort,
        episodeEp: EpisodeSort? = this.episodeEp,
        episodeName: String = this.episodeName,
        extra: Map<String, String> = this.extra,
    ): MediaCacheMetadata {
        return MediaCacheMetadata(
            subjectId = subjectId,
            episodeId = episodeId,
            subjectNames = subjectNames,
            episodeSort = episodeSort,
            episodeEp = episodeEp,
            episodeName = episodeName,
            extra = extra,
        )
    }
}


fun MediaCacheMetadata(
    request: MediaFetchRequest,
    extra: Map<String, String> = emptyMap(),
): MediaCacheMetadata {
    return MediaCacheMetadata(
        subjectId = request.subjectId,
        episodeId = request.episodeId,
        subjectNames = request.subjectNames,
        episodeSort = request.episodeSort,
        episodeEp = request.episodeEp,
        episodeName = request.episodeName,
        extra = extra,
    )
}
