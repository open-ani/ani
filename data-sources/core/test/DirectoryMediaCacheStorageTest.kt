package me.him188.ani.datasources.core.cache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.ResourceLocation
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class DirectoryMediaCacheStorageTest {
    companion object {
        private const val CACHE_MEDIA_SOURCE_ID = "local-test"
    }

    @TempDir
    private lateinit var dir: File

    private open class TestMediaCache(
        val media: CachedMedia,
        override val metadata: MediaCacheMetadata,
        override val progress: Flow<Float> = MutableStateFlow(0f),
        override val totalSize: Flow<FileSize> = MutableStateFlow(0.bytes),
    ) : MediaCache {
        override val origin: Media get() = media.origin
        override suspend fun getCachedMedia(): CachedMedia = media
        override val finished: Flow<Boolean> by lazy {
            progress.map { it == 1f }
        }

        val resumeCalled = AtomicInteger(0)

        override suspend fun pause() {
            println("pause")
        }

        override suspend fun resume() {
            resumeCalled.incrementAndGet()
            println("resume")
        }

        override suspend fun delete() {
            println("delete called")
        }
    }

    private val engine = object : MediaCacheEngine {
        override suspend fun restore(
            origin: Media,
            metadata: MediaCacheMetadata,
            parentContext: CoroutineContext
        ): MediaCache? {
            return null
        }

        override suspend fun createCache(
            origin: Media,
            request: MediaFetchRequest,
            parentContext: CoroutineContext
        ): MediaCache {
            return TestMediaCache(
                media = CachedMedia(
                    origin,
                    CACHE_MEDIA_SOURCE_ID,
                    download = origin.download,
                ),
                metadata = MediaCacheMetadata(
                    request,
                    mapOf("testExtra" to "1")
                ),
            )
        }
    }

    private val media = DefaultMedia(
        mediaId = "dmhy.2",
        mediaSourceId = "dmhy",
        originalTitle = "夜晚的水母不会游泳 02 测试剧集",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
        originalUrl = "https://example.com/1",
        size = 233.megaBytes,
        publishedTime = System.currentTimeMillis(),
        episodes = listOf(EpisodeSort(2)),
        properties = MediaProperties(
            subtitleLanguages = listOf("CHT"),
            resolution = "1080P",
            alliance = "北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组",
        ),
    )

    @Test
    fun `create and fine with resume`() = runTest {
        val storage = DirectoryMediaCacheStorage(CACHE_MEDIA_SOURCE_ID, dir.toPath(), engine, this.coroutineContext)

        val cache = storage.cache(
            media,
            MediaFetchRequest(
                episodeId = "1231231",
                subjectNames = emptyList(),
                episodeSort = EpisodeSort("02"),
                episodeName = "测试剧集",
            ),
            resume = true
        ) as TestMediaCache
        assertEquals(1, cache.resumeCalled.get())

        assertSame(
            cache,
            storage.findCache(
                media,
                resume = true,
            )
        )
        assertEquals(2, cache.resumeCalled.get())

        storage.close()
    }

    @Test
    fun `create and find without resume`() = runTest {
        val storage = DirectoryMediaCacheStorage(CACHE_MEDIA_SOURCE_ID, dir.toPath(), engine, this.coroutineContext)

        val cache = storage.cache(
            media,
            MediaFetchRequest(
                episodeId = "1231231",
                subjectNames = emptyList(),
                episodeSort = EpisodeSort("02"),
                episodeName = "测试剧集",
            ),
            resume = false
        ) as TestMediaCache
        assertEquals(0, cache.resumeCalled.get())

        assertSame(
            cache,
            storage.findCache(
                media,
                resume = false,
            )
        )
        assertEquals(0, cache.resumeCalled.get())

        storage.close()
    }

    @Test
    fun `can delete while not using`() = runTest {
        val storage = DirectoryMediaCacheStorage(CACHE_MEDIA_SOURCE_ID, dir.toPath(), engine, this.coroutineContext)

        val cache = storage.cache(
            media,
            MediaFetchRequest(
                episodeId = "1231231",
                subjectNames = emptyList(),
                episodeSort = EpisodeSort("02"),
                episodeName = "测试剧集",
            ),
            resume = false
        ) as TestMediaCache

        assertEquals(0, cache.resumeCalled.get())

        assertEquals(cache, storage.findCache(media, resume = false))
        assertEquals(true, storage.delete(media))
        assertEquals(null, storage.findCache(media, resume = false))

        assertEquals(0, cache.resumeCalled.get())

        storage.close()
    }

    @Test
    fun `cached media id`() = runTest {
        val storage = DirectoryMediaCacheStorage(CACHE_MEDIA_SOURCE_ID, dir.toPath(), engine, this.coroutineContext)

        val cache = storage.cache(
            media,
            MediaFetchRequest(
                episodeId = "1231231",
                subjectNames = emptyList(),
                episodeSort = EpisodeSort("02"),
                episodeName = "测试剧集",
            ),
            resume = false
        ) as TestMediaCache

        assertEquals("$CACHE_MEDIA_SOURCE_ID:${media.mediaId}", cache.media.mediaId)
        assertEquals(CACHE_MEDIA_SOURCE_ID, cache.media.mediaSourceId)
        assertEquals(media, cache.media.origin)

        storage.close()
    }
}
