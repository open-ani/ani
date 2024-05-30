package me.him188.ani.datasources.core.cache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.topic.EpisodeRange
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
        override fun isValid(): Boolean = true

        override val downloadSpeed: Flow<FileSize> = MutableStateFlow(0.bytes)
        override val uploadSpeed: Flow<FileSize> = MutableStateFlow(0.bytes)
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
                    CACHE_MEDIA_SOURCE_ID,
                    download = origin.download,
                ),
                metadata = metadata.withExtra(mapOf("testExtra" to "1")),
            )
        }

        override suspend fun deleteUnusedCaches(all: List<MediaCache>) {
        }
    }

    private val media = DefaultMedia(
        mediaId = "dmhy.2",
        mediaSourceId = "dmhy",
        originalTitle = "夜晚的水母不会游泳 02 测试剧集",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
        originalUrl = "https://example.com/1",
        publishedTime = System.currentTimeMillis(),
        episodeRange = EpisodeRange.single(EpisodeSort(2)),
        properties = MediaProperties(
            subtitleLanguageIds = listOf("CHT"),
            resolution = "1080P",
            alliance = "北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组",
            size = 233.megaBytes,
        ),
        kind = MediaSourceKind.BitTorrent,
        location = MediaSourceLocation.Online,
    )

    @Test
    fun `create and fine with resume`() = runTest {
        val storage = DirectoryMediaCacheStorage(CACHE_MEDIA_SOURCE_ID, dir.toPath(), engine, this.coroutineContext)

        val cache = storage.cache(
            media,
            MediaCacheMetadata(
                episodeId = "1231231",
                subjectNames = emptySet(),
                episodeSort = EpisodeSort("02"),
                episodeEp = EpisodeSort("02"),
                episodeName = "测试剧集",
            ),
            resume = true
        ) as TestMediaCache
        assertEquals(1, cache.resumeCalled.get())

        assertSame(
            cache,
            storage.listFlow.first().single()
        )
        assertEquals(1, cache.resumeCalled.get())

        storage.close()
    }

    @Test
    fun `create and find without resume`() = runTest {
        val storage = DirectoryMediaCacheStorage(CACHE_MEDIA_SOURCE_ID, dir.toPath(), engine, this.coroutineContext)

        val cache = storage.cache(
            media,
            MediaCacheMetadata(
                episodeId = "1231231",
                subjectNames = emptySet(),
                episodeSort = EpisodeSort("02"),
                episodeEp = EpisodeSort("02"),
                episodeName = "测试剧集",
            ),
            resume = false
        ) as TestMediaCache
        assertEquals(0, cache.resumeCalled.get())

        assertSame(
            cache,
            storage.listFlow.first().single()
        )
        assertEquals(0, cache.resumeCalled.get())

        storage.close()
    }

    @Test
    fun `can delete while not using`() = runTest {
        val storage = DirectoryMediaCacheStorage(CACHE_MEDIA_SOURCE_ID, dir.toPath(), engine, this.coroutineContext)

        val cache = storage.cache(
            media,
            MediaCacheMetadata(
                episodeId = "1231231",
                subjectNames = emptySet(),
                episodeSort = EpisodeSort("02"),
                episodeEp = EpisodeSort("02"),
                episodeName = "测试剧集",
            ),
            resume = false
        ) as TestMediaCache

        assertEquals(0, cache.resumeCalled.get())

        assertEquals(cache, storage.listFlow.first().single())
        assertEquals(true, storage.delete(cache))
        assertEquals(null, storage.listFlow.first().firstOrNull())

        assertEquals(0, cache.resumeCalled.get())

        storage.close()
    }

    @Test
    fun `cached media id`() = runTest {
        val storage = DirectoryMediaCacheStorage(CACHE_MEDIA_SOURCE_ID, dir.toPath(), engine, this.coroutineContext)

        val cache = storage.cache(
            media,
            MediaCacheMetadata(
                episodeId = "1231231",
                subjectNames = emptySet(),
                episodeSort = EpisodeSort("02"),
                episodeEp = EpisodeSort("02"),
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
