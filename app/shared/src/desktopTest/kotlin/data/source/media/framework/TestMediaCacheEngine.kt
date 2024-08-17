package me.him188.ani.app.data.source.media.framework

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.source.media.cache.MediaCache
import me.him188.ani.app.data.source.media.cache.MediaStats
import me.him188.ani.app.data.source.media.cache.TestMediaCache
import me.him188.ani.app.data.source.media.cache.emptyMediaStats
import me.him188.ani.app.data.source.media.cache.engine.MediaCacheEngine
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import kotlin.coroutines.CoroutineContext

class TestMediaCacheEngine(
    private val mediaCacheId: String,
) : MediaCacheEngine {
    override val isEnabled: Flow<Boolean> = flowOf(true)
    override val stats: MediaStats = emptyMediaStats()
    override fun supports(media: Media): Boolean = true

    override suspend fun restore(
        origin: Media,
        metadata: MediaCacheMetadata,
        parentContext: CoroutineContext
    ): MediaCache? {
        return null
    }

    override suspend fun createCache(
        origin: Media,
        metadata: MediaCacheMetadata,
        parentContext: CoroutineContext
    ): MediaCache {
        return TestMediaCache(
            media = CachedMedia(
                origin,
                mediaCacheId,
                download = origin.download,
            ),
            metadata = metadata.withExtra(mapOf("testExtra" to "1")),
        )
    }

    override suspend fun deleteUnusedCaches(all: List<MediaCache>) {
    }
}