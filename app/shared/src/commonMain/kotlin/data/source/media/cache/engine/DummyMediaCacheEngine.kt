package me.him188.ani.app.data.source.media.cache.engine

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.source.media.cache.MediaCache
import me.him188.ani.app.data.source.media.cache.MediaStats
import me.him188.ani.app.data.source.media.cache.emptyMediaStats
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.kiloBytes
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import kotlin.concurrent.Volatile
import kotlin.coroutines.CoroutineContext

/**
 * 不会实际发起下载, 内部维护一个虚拟进度条, 用于测试.
 */
class DummyMediaCacheEngine(
    private val mediaSourceId: String,
    private val location: MediaSourceLocation = MediaSourceLocation.Local,
) : MediaCacheEngine {
    override val stats: MediaStats = emptyMediaStats()
    override fun supports(media: Media): Boolean = true

    override suspend fun restore(
        origin: Media,
        metadata: MediaCacheMetadata,
        parentContext: CoroutineContext
    ): MediaCache = DummyMediaCache(origin, metadata, mediaSourceId, location)

    override suspend fun createCache(
        origin: Media,
        metadata: MediaCacheMetadata,
        parentContext: CoroutineContext
    ): MediaCache = DummyMediaCache(origin, metadata, mediaSourceId, location)

    override suspend fun deleteUnusedCaches(all: List<MediaCache>) {
    }
}

class DummyMediaCache(
    override val origin: Media,
    override val metadata: MediaCacheMetadata,
    val mediaSourceId: String,
    val location: MediaSourceLocation = MediaSourceLocation.Local,
) : MediaCache {
    private val cachedMedia by lazy {
        CachedMedia(origin, mediaSourceId, origin.download, location)
    }

    override suspend fun getCachedMedia(): CachedMedia = cachedMedia

    @Volatile
    private var isValid = true
    override fun isValid(): Boolean = isValid

    override val downloadSpeed: Flow<FileSize> = flowOf(100.kiloBytes)
    override val uploadSpeed: Flow<FileSize> = flowOf(0.kiloBytes)
    override val progress: Flow<Float> = flowOf(0.3f)
    override val finished: Flow<Boolean> = flowOf(false)
    override val totalSize: Flow<FileSize> = flowOf(100.megaBytes)

    override suspend fun pause() {
    }

    override suspend fun close() {
    }

    override suspend fun resume() {
    }

    override val isDeleted: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun closeAndDeleteFiles() {
        isDeleted.value = true
    }
}
