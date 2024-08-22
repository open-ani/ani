package me.him188.ani.app.data.source.media.cache

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.utils.platform.annotations.TestOnly
import kotlin.math.absoluteValue

/**
 * 表示一个进行中的 [Media] 缓存.
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
            return calculateCacheId(origin.mediaId, metadata)
        }

    /**
     * Original media that is being cached.
     */
    val origin: Media

    /**
     * 缓存元数据.
     */
    val metadata: MediaCacheMetadata

    val state: StateFlow<MediaCacheState>

    /**
     * Returns the [CachedMedia] instance for this cache.
     * The instance is cached so this function will immediately return the cached instance after the first successful call.
     */
    suspend fun getCachedMedia(): CachedMedia

    /**
     * 缓存
     */
    fun isValid(): Boolean

    /**
     * 下载速度, 每秒. 对于不支持下载的缓存, 该值为 [FileSize.Zero].
     *
     * 为单个文件的下载速度.
     *
     * - 若 emit [FileSize.Unspecified], 表示上传速度未知. 这只会在该缓存正在上传, 但无法知道具体速度时出现.
     * - 若 emit [FileSize.Zero], 表示上传速度真的是零.
     */
    val downloadSpeed: Flow<FileSize>

    val sessionDownloadSpeed: Flow<FileSize> get() = downloadSpeed

    /**
     * 上传速度, 每秒. 对于不支持上传的缓存, 该值为 [FileSize.Zero].
     *
     * 注意, 这实际上是整个 media 的下载速度.
     *
     * - 若 emit [FileSize.Unspecified], 表示上传速度未知. 这只会在该缓存正在上传, 但无法知道具体速度时出现.
     * - 若 emit [FileSize.Zero], 表示上传速度真的是零.
     */
    val sessionUploadSpeed: Flow<FileSize> // todo this is shit

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
     * 从引擎中关闭此缓存任务, 关闭后不能再开启.
     *
     * 此函数会等待此资源完全关闭并释放资源后返回.
     *
     * 若当前状态不支持暂停, 例如已经被删除, 则忽略本次请求, 不会抛出异常.
     */
    suspend fun close()

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
    suspend fun closeAndDeleteFiles()

    companion object {
        fun calculateCacheId(originMediaId: String, metadata: MediaCacheMetadata): String {
            val hash = (originMediaId.hashCode() * 31
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
    }
}

val MediaCache.downloadedSize
    get() = this.totalSize.combine(this.progress) { totalSize, progress ->
        if (totalSize == FileSize.Unspecified) {
            FileSize.Unspecified
        } else {
            totalSize * progress
        }
    }

enum class MediaCacheState {
    IN_PROGRESS,
    PAUSED,
}

open class TestMediaCache(
    val media: CachedMedia,
    override val metadata: MediaCacheMetadata,
    override val progress: Flow<Float> = MutableStateFlow(0f),
    override val totalSize: Flow<FileSize> = MutableStateFlow(0.bytes),
) : MediaCache {
    override val origin: Media get() = media.origin
    override val state: MutableStateFlow<MediaCacheState> = MutableStateFlow(MediaCacheState.IN_PROGRESS)

    override suspend fun getCachedMedia(): CachedMedia = media
    override fun isValid(): Boolean = true

    override val downloadSpeed: Flow<FileSize> = MutableStateFlow(1.bytes)
    override val sessionUploadSpeed: Flow<FileSize> = MutableStateFlow(1.bytes)
    override val finished: Flow<Boolean> by lazy { progress.map { it == 1f } }

    private val resumeCalled = atomic(0)

    @TestOnly
    fun getResumeCalled() = resumeCalled.value

    override suspend fun pause() {
        println("pause")
    }

    override suspend fun close() {
        println("close")
    }

    override suspend fun resume() {
        resumeCalled.incrementAndGet()
        println("resume")
    }

    override val isDeleted: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun closeAndDeleteFiles() {
        println("delete called")
        isDeleted.value = true
    }
}
