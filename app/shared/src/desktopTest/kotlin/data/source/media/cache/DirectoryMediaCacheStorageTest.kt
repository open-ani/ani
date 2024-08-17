package me.him188.ani.app.data.source.media.cache

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.him188.ani.app.data.source.media.framework.TestMediaCacheEngine
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.utils.io.inSystem
import me.him188.ani.utils.io.toKtPath
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class DirectoryMediaCacheStorageTest {
    companion object {
        private const val CACHE_MEDIA_SOURCE_ID = "local-test"
    }

    @TempDir
    private lateinit var dir: File

    private val engine = TestMediaCacheEngine(CACHE_MEDIA_SOURCE_ID)

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
            subtitleKind = null,
        ),
        kind = MediaSourceKind.BitTorrent,
        location = MediaSourceLocation.Online,
    )

    @Test
    fun `create and find with resume`() = runTest {
        val storage =
            DirectoryMediaCacheStorage(CACHE_MEDIA_SOURCE_ID, dir.toKtPath().inSystem, engine, this.coroutineContext)

        val cache = storage.cache(
            media,
            MediaCacheMetadata(
                episodeId = "1231231",
                subjectNames = emptySet(),
                episodeSort = EpisodeSort("02"),
                episodeEp = EpisodeSort("02"),
                episodeName = "测试剧集",
            ),
            resume = true,
        ) as TestMediaCache
        assertEquals(1, cache.getResumeCalled())

        assertSame(
            cache,
            storage.listFlow.first().single(),
        )
        assertEquals(1, cache.getResumeCalled())

        storage.close()
    }

    @Test
    fun `create and find without resume`() = runTest {
        val storage =
            DirectoryMediaCacheStorage(CACHE_MEDIA_SOURCE_ID, dir.toKtPath().inSystem, engine, this.coroutineContext)

        val cache = storage.cache(
            media,
            MediaCacheMetadata(
                episodeId = "1231231",
                subjectNames = emptySet(),
                episodeSort = EpisodeSort("02"),
                episodeEp = EpisodeSort("02"),
                episodeName = "测试剧集",
            ),
            resume = false,
        ) as TestMediaCache
        assertEquals(0, cache.getResumeCalled())

        assertSame(
            cache,
            storage.listFlow.first().single(),
        )
        assertEquals(0, cache.getResumeCalled())

        storage.close()
    }

    @Test
    fun `can delete while not using`() = runTest {
        val storage =
            DirectoryMediaCacheStorage(CACHE_MEDIA_SOURCE_ID, dir.toKtPath().inSystem, engine, this.coroutineContext)

        val cache = storage.cache(
            media,
            MediaCacheMetadata(
                episodeId = "1231231",
                subjectNames = emptySet(),
                episodeSort = EpisodeSort("02"),
                episodeEp = EpisodeSort("02"),
                episodeName = "测试剧集",
            ),
            resume = false,
        ) as TestMediaCache

        assertEquals(0, cache.getResumeCalled())

        assertEquals(cache, storage.listFlow.first().single())
        assertEquals(true, storage.delete(cache))
        assertEquals(null, storage.listFlow.first().firstOrNull())

        assertEquals(0, cache.getResumeCalled())

        storage.close()
    }

    @Test
    fun `cached media id`() = runTest {
        val storage =
            DirectoryMediaCacheStorage(CACHE_MEDIA_SOURCE_ID, dir.toKtPath().inSystem, engine, this.coroutineContext)

        val cache = storage.cache(
            media,
            MediaCacheMetadata(
                episodeId = "1231231",
                subjectNames = emptySet(),
                episodeSort = EpisodeSort("02"),
                episodeEp = EpisodeSort("02"),
                episodeName = "测试剧集",
            ),
            resume = false,
        ) as TestMediaCache

        assertEquals("$CACHE_MEDIA_SOURCE_ID:${media.mediaId}", cache.media.mediaId)
        assertEquals(CACHE_MEDIA_SOURCE_ID, cache.media.mediaSourceId)
        assertEquals(media, cache.media.origin)

        storage.close()
    }
}
