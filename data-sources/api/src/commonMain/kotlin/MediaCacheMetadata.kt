package me.him188.ani.datasources.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.datasources.api.source.MediaFetchRequest

/**
 * 一个 `MediaCache` 的元数据, 包含来源的条目和剧集信息, 以及 `MediaCacheEngine` 自行添加的额外信息 [extra].
 *
 * [MediaCacheMetadata] 通常在创建缓存时, 根据 [MediaFetchRequest] 中的条目和剧集信息创建.
 *
 * [MediaCacheMetadata] 可被持久化, 用于下次启动时恢复缓存任务. 恢复过程详见 `MediaCacheStorage`.
 *
 * 在播放时查询数据源时, [MediaCacheMetadata] 也被用于与 [MediaFetchRequest] 匹配缓存. 查询过程详见 `MediaCacheEngine`.
 */
@Serializable
data class MediaCacheMetadata
/**
 * This constructor is only for serialization
 */
@Deprecated("This constructor is only for serialization. Use the other one instead", level = DeprecationLevel.ERROR)
constructor(
    /**
     * @see MediaFetchRequest.subjectId
     */
    val subjectId: String? = null,
    /**
     * @see MediaFetchRequest.episodeId
     */
    val episodeId: String? = null,
    /**
     * @see MediaFetchRequest.subjectNames
     */
    val subjectNames: Set<String>,
    /**
     * @see MediaFetchRequest.episodeSort
     */
    val episodeSort: EpisodeSort,
    /**
     * @see MediaFetchRequest.episodeEp
     */
    val episodeEp: EpisodeSort? = episodeSort,
    /**
     * @see MediaFetchRequest.episodeName
     */
    val episodeName: String,
    /**
     * `MediaCacheEngine` 自行添加的额外信息. 例如来源 BT 磁力链.
     */
    val extra: Map<String, String> = emptyMap(),
    @Transient @Suppress("unused") private val _primaryConstructorMarker: Byte = 0, // avoid compiler error
) {
    @Suppress("DEPRECATION_ERROR")
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
        _primaryConstructorMarker = 0,
    )

    /**
     * Appends [other] to the existing [extra].
     */
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
