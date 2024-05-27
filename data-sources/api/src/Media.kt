package me.him188.ani.datasources.api

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.ResourceLocation

/**
 * 表示从数据源获取到的一个资源, 即一个字幕组发布的资源.
 *
 * 一个资源可能对应一个剧集 [episodeRange], 新番资源资源大部分都是这样.
 * 一个资源也可能对应多个剧集 [episodeRange], 尤其是老番的季度全集资源.
 *
 * [Media] 的使用方可自行
 */
@Serializable
@Immutable
sealed interface Media {
    /**
     * 该资源的全局唯一 id, 通常需要包含 [mediaSourceId], 例如 "dmhy.1", 以防多数据源查询到同一个资源会容易 crash UI.
     */
    val mediaId: String

    /**
     * 查询出这个资源的数据源的全局唯一 id.
     *
     * @see MediaSource.mediaSourceId
     */
    val mediaSourceId: String // e.g. "dmhy"

    /**
     * 在数据源上的原始链接, 一般是 HTTP URL
     */
    val originalUrl: String

    /**
     * 描述如何下载这个资源
     */
    val download: ResourceLocation // 有关具体下载过程, 参考 app `VideoSourceResolver`, 以及 `MediaCacheEngine`

    /**
     * 该资源包含的剧集列表.
     *
     * - 如果是单集资源, 该列表可能包含 1 个元素.
     * - 如果是季度全集资源, 该列表可能包含多个元素, 典型值为 12 个.
     *
     * 当解析剧集列表失败时为 `null`
     */
    val episodeRange: EpisodeRange?

    /**
     * 字幕组发布的原标题
     */
    val originalTitle: String

    /**
     * 该资源发布时间, 毫秒时间戳
     */
    val publishedTime: Long

    val properties: MediaProperties

    /**
     * 查看 [MediaSourceLocation.Local] 和 [MediaSourceLocation.Online].
     */
    val location: MediaSourceLocation

    /**
     * @see MediaSource.kind
     */
    val kind: MediaSourceKind
}

/**
 * The default implementation of [Media].
 *
 * [location] can be either [MediaSourceLocation.Online] or [MediaSourceLocation.Local].
 * A local [MediaSource] may support caching of other [MediaSourceLocation.Online] media sources, however,
 * it may also produce [MediaSourceLocation.Local] [DefaultMedia]s directly when you first search for that media.
 */
@Immutable
@Serializable
data class DefaultMedia(
    override val mediaId: String,
    override val mediaSourceId: String, // e.g. "dmhy"
    override val originalUrl: String,
    override val download: ResourceLocation,
    override val originalTitle: String,
    override val publishedTime: Long,
    override val properties: MediaProperties,
    override val episodeRange: EpisodeRange? = null,
    override val location: MediaSourceLocation = MediaSourceLocation.Online,
    override val kind: MediaSourceKind = MediaSourceKind.BitTorrent,
) : Media

/**
 * A media that is already cached into an easily accessible location, i.e. [MediaSourceLocation.Local].
 */
@Immutable
class CachedMedia(
    val origin: Media,
    cacheMediaSourceId: String,
    override val download: ResourceLocation,
    override val location: MediaSourceLocation = MediaSourceLocation.Local,
    override val kind: MediaSourceKind = MediaSourceKind.LocalCache,
) : Media by origin {
    override val mediaId: String = "${cacheMediaSourceId}:${origin.mediaId}"
    override val mediaSourceId: String = cacheMediaSourceId
}

/**
 * Properties of [Media] that might be useful for filtering.
 */
@Immutable
@Serializable
class MediaProperties private constructor(
    /**
     * Empty list means no subtitles
     */
    val subtitleLanguageIds: List<String>,
    /**
     * Resolution, e.g. "1080P", "4K"
     */
    val resolution: String,
    /**
     * Subtitle group
     */
    val alliance: String,
    /**
     * Size of the media file. Can be [FileSize.Zero] if not available.
     */
    val size: FileSize = 0.bytes, // note: only for compatibility
    @Suppress("unused")
    @Transient private val _primaryConstructorMarker: Unit = Unit,
) {
    constructor(
        // so that caller still need to provide all properties despite we have default values for compatibility
        subtitleLanguageIds: List<String>,
        resolution: String,
        alliance: String,
        size: FileSize,
    ) : this(
        subtitleLanguageIds, resolution, alliance, size,
        _primaryConstructorMarker = Unit
    )

    override fun toString(): String {
        return "MediaProperties(subtitleLanguageIds=$subtitleLanguageIds, resolution='$resolution', alliance='$alliance', size=$size)"
    }
}

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