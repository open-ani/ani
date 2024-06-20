package me.him188.ani.app.data.media.cache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.media.fetch.MediaFetcher
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.source.MatchKind
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.absoluteValue

/**
 * 表示一个媒体缓存的存储空间, 例如一个本地目录.
 *
 * ## Identity
 *
 * [MediaCacheStorage] and [MediaSource] use the same ID system and,
 * there can be a [MediaSource] with the same ID as this [MediaCacheStorage].
 *
 * By having a [MediaSource] with the same ID,
 * a [MediaCacheStorage] can participate in the [MediaFetcher.newSession] process (and usually it should).
 */
interface MediaCacheStorage : AutoCloseable {
    /**
     * ID of this media source.
     */
    val mediaSourceId: String

    val isEnabled: Flow<Boolean>

    /**
     * 此空间的 [MediaSource]. 调用 [MediaSource.fetch] 则可从此空间中查询缓存, 作为 [Media].
     */
    val cacheMediaSource: MediaSource

    /**
     * Number of caches in this storage.
     */
    val count: Flow<Int>

    /**
     * Total size of the cache.
     */
    val totalSize: Flow<FileSize>

    /**
     * 此存储的总体统计
     */
    val stats: MediaStats

    /**
     * A flow that subscribes on all the caches in the storage.
     *
     * Note that to retrieve [Media] (more specifically, [CachedMedia]) from the cache storage, you might want to use [cacheMediaSource].
     */
    val listFlow: Flow<List<MediaCache>>

    /**
     * Finds the existing cache for the media or adds the media to the cache (queue).
     *
     * When this function returns, A new [MediaSource] can then be listed by [listFlow].
     *
     * Caching is made asynchronously. This function might only adds a job to the queue and does not guarantee when the cache will be done.
     *
     * This function returns only if the cache configuration is persisted.
     *
     * @param metadata The request to fetch the media.
     */
    suspend fun cache(media: Media, metadata: MediaCacheMetadata, resume: Boolean = true): MediaCache

    /**
     * Delete the cache if it exists.
     * @return `true` if a cache was deleted, `false` if there wasn't such a cache.
     */
    suspend fun delete(cache: MediaCache): Boolean
}

suspend inline fun MediaCacheStorage.contains(cache: MediaCache): Boolean = listFlow.first().any { it === cache }

val MediaCacheStorage.anyCaching: Flow<Boolean>
    get() = listFlow.flatMapLatest { caches ->
        combine(caches.map { it.progress }) { array -> array.any { it < 1f } }
    }

/**
 * 表示一个进行中的资源缓存.
 *
 * [MediaCache] 有状态,
 *
 * 可能有用的属性:
 * - 该媒体的实际存储位置: [origin] 的 [Media.download]
 */
interface MediaCache {
    /**
     * 唯一缓存 id
     */
    val cacheId: String
        get() {
            val hash = (origin.mediaId.hashCode() * 31
                    + metadata.subjectId.hashCode() * 31
                    + metadata.episodeId.hashCode()).absoluteValue.toString()
            val subjectName = metadata.subjectNames.firstOrNull() ?: metadata.subjectId
            if (subjectName != null) {
                fun removeSpecials(value: String): String {
                    return value.replace(Regex("""[-\\|/.,;'\[\]{}()=_ ~!@#$%^&*]"""), "")
                }
                return "${removeSpecials(subjectName).take(8)}-$hash"
            }
            return hash
        }

    /**
     * Original media that is being cached.
     */
    val origin: Media

    /**
     * 缓存元数据.
     */
    val metadata: MediaCacheMetadata

    /**
     * Returns the [CachedMedia] instance for this cache.
     * The instance is cached so this function will immediately return the cached instance after the first successful call.
     */
    suspend fun getCachedMedia(): CachedMedia

    fun isValid(): Boolean

    /**
     * 下载速度, 每秒. 对于不支持下载的缓存, 该值为 [FileSize.Zero].
     *
     * - 若 emit [FileSize.Unspecified], 表示上传速度未知. 这只会在该缓存正在上传, 但无法知道具体速度时出现.
     * - 若 emit [FileSize.Zero], 表示上传速度真的是零.
     */
    val downloadSpeed: Flow<FileSize>

    /**
     * 上传速度, 每秒. 对于不支持上传的缓存, 该值为 [FileSize.Zero].
     *
     * - 若 emit [FileSize.Unspecified], 表示上传速度未知. 这只会在该缓存正在上传, 但无法知道具体速度时出现.
     * - 若 emit [FileSize.Zero], 表示上传速度真的是零.
     */
    val uploadSpeed: Flow<FileSize>

    /**
     * 下载进度, 范围为 `0.0f..1.0f`.
     */
    val progress: Flow<Float>

    /**
     * 下载是否已经完成.
     *
     * 在刚创建时, [MediaCache] 可能需要时间扫描已经下载的文件状态.
     * 在扫描完成前, 即使文件已经下载成功, 该值也为 `false`.
     */
    val finished: Flow<Boolean>

    /**
     * 下载的总大小.
     *
     * 在刚创建时, [MediaCache] 可能需要时间扫描已经下载的文件状态.
     * 在扫描完成前, 即使文件已经下载成功, 该值为 [FileSize.Zero].
     *
     * 不会返回 [FileSize.Unspecified].
     */
    val totalSize: Flow<FileSize>

    /**
     * 请求暂停下载.
     *
     * 若当前状态不支持暂停, 例如已经被删除, 则忽略本次请求, 不会抛出异常.
     */
    suspend fun pause()

    /**
     * 请求恢复下载.
     *
     * 若当前状态不支持恢复, 例如已经被删除, 则忽略本次请求, 不会抛出异常.
     */
    suspend fun resume()

    /**
     * 该缓存的文件是否已经被删除. 删除后不可恢复.
     *
     * 删除后 [MediaCacheStorage] 应当移除该 [MediaCache], 但 [MediaCache] 可能仍按被其他对象引用, 解释
     */
    val isDeleted: StateFlow<Boolean>

    /**
     * 尝试删除此 [MediaCache] 所涉及的文件.
     *
     * 此函数必须关闭所有使用的资源, 清理潜在的缓存文件, 且不得抛出异常 (除非是 [CancellationException]).
     */
    suspend fun deleteFiles()
}

/**
 * 尝试匹配
 */
infix fun MediaFetchRequest.matches(cache: MediaCacheMetadata): MatchKind? {
    if (episodeId != null && cache.episodeId != null) {
        // Both query and cache have episodeId, perform exact match.
        if (cache.episodeId == episodeId) {
            return MatchKind.EXACT
        }

        // Don't go for fuzzy match otherwise we'll always get false positives.
        return null
    }

    // Exact match is not possible, do a fuzzy match.

    // Success if the episode name exactly matches
    if (episodeName.isNotEmpty() && cache.episodeName == episodeName) return MatchKind.FUZZY

    if (subjectNames.any { cache.subjectNames.contains(it) }) {
        // Any subject name matches

        return if (episodeSort == cache.episodeSort || episodeEp == cache.episodeSort) {
            // Episode sort matches
            MatchKind.FUZZY
        } else {
            null
        }
    }

    return null
}


open class TestMediaCache(
    val media: CachedMedia,
    override val metadata: MediaCacheMetadata,
    override val progress: Flow<Float> = MutableStateFlow(0f),
    override val totalSize: Flow<FileSize> = MutableStateFlow(0.bytes),
) : MediaCache {
    override val origin: Media get() = media.origin
    override suspend fun getCachedMedia(): CachedMedia = media
    override fun isValid(): Boolean = true

    override val downloadSpeed: Flow<FileSize> = MutableStateFlow(1.bytes)
    override val uploadSpeed: Flow<FileSize> = MutableStateFlow(1.bytes)
    override val finished: Flow<Boolean> by lazy { progress.map { it == 1f } }

    val resumeCalled = AtomicInteger(0)

    override suspend fun pause() {
        println("pause")
    }

    override suspend fun resume() {
        resumeCalled.incrementAndGet()
        println("resume")
    }

    override val isDeleted: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun deleteFiles() {
        println("delete called")
        isDeleted.value = true
    }
}
