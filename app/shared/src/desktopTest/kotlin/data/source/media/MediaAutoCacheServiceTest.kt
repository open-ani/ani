package me.him188.ani.app.data.source.media

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import me.him188.ani.app.data.models.episode.EpisodeCollection
import me.him188.ani.app.data.models.episode.EpisodeInfo
import me.him188.ani.app.data.models.episode.episode
import me.him188.ani.app.data.source.media.cache.DefaultMediaAutoCacheService
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.PackedDate
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MediaAutoCacheServiceTest {
    private var id = 0
    private fun ep(
        id: Int = this.id++,
        airdate: PackedDate = PackedDate(0, 1, 1)
    ) = EpisodeInfo(
        id = id,
        name = "Diana Houston",
        nameCn = "Nita O'Donnell",
        sort = EpisodeSort(1),
        comment = 5931,
        duration = "",
        desc = "gubergren",
        airDate = airdate, // 一定已经开播
        disc = 2272,
        ep = EpisodeSort(1),
    )

    @Test
    fun `no cache for watched`() = runTest {
        val res = DefaultMediaAutoCacheService.firstEpisodeToCache(
            listOf(
                EpisodeCollection(
                    ep(),
                    collectionType = UnifiedCollectionType.DONE,
                ),
            ),
            hasAlreadyCached = {
                false
            },
        ).toList()

        assertEquals(0, res.size)
    }

    @Test
    fun `no cache for not broadcast`() = runTest {
        val res = DefaultMediaAutoCacheService.firstEpisodeToCache(
            listOf(
                EpisodeCollection(
                    ep(airdate = PackedDate(9999, 1, 1)),
                    collectionType = UnifiedCollectionType.WISH,
                ),
            ),
            hasAlreadyCached = {
                false
            },
        ).toList()

        assertEquals(0, res.size)
    }

    @Test
    fun `no cache for already cached`() = runTest {
        val res = DefaultMediaAutoCacheService.firstEpisodeToCache(
            listOf(
                EpisodeCollection(
                    ep(),
                    collectionType = UnifiedCollectionType.WISH,
                ),
            ),
            hasAlreadyCached = {
                true
            },
        ).toList()

        assertEquals(0, res.size)
    }

    @Test
    fun `cache unwatched`() = runTest {
        val res = DefaultMediaAutoCacheService.firstEpisodeToCache(
            listOf(
                EpisodeCollection(
                    ep(),
                    collectionType = UnifiedCollectionType.WISH,
                ),
            ),
            hasAlreadyCached = {
                false
            },
        ).toList()

        assertEquals(1, res.size)
    }

    @Test
    fun `cache unwatched multiple`() = runTest {
        val res = DefaultMediaAutoCacheService.firstEpisodeToCache(
            listOf(
                EpisodeCollection(ep(id = 0), collectionType = UnifiedCollectionType.DONE),
                EpisodeCollection(ep(id = 1), collectionType = UnifiedCollectionType.WISH),
                EpisodeCollection(ep(id = 2), collectionType = UnifiedCollectionType.WISH),
                EpisodeCollection(ep(id = 3), collectionType = UnifiedCollectionType.WISH),
            ),
            hasAlreadyCached = {
                false
            },
        ).toList()

        assertEquals(3, res.size)
        assertEquals(1, res.first().episode.id)
    }

    @Test
    fun `no cache when already cached many`() = runTest {
        val res = DefaultMediaAutoCacheService.firstEpisodeToCache(
            listOf(
                EpisodeCollection(ep(id = 0), collectionType = UnifiedCollectionType.WISH),
                EpisodeCollection(ep(id = 1), collectionType = UnifiedCollectionType.WISH),
                EpisodeCollection(ep(id = 2), collectionType = UnifiedCollectionType.WISH),
                EpisodeCollection(ep(id = 3), collectionType = UnifiedCollectionType.WISH),
            ),
            hasAlreadyCached = {
                it.episode.id <= 0
            },
            maxCount = 1,
        ).toList()

        assertEquals(0, res.size)
    }

    @Test
    fun `cache until max count`() = runTest {
        val res = DefaultMediaAutoCacheService.firstEpisodeToCache(
            listOf(
                EpisodeCollection(ep(id = 0), collectionType = UnifiedCollectionType.WISH),
                EpisodeCollection(ep(id = 1), collectionType = UnifiedCollectionType.WISH),
                EpisodeCollection(ep(id = 2), collectionType = UnifiedCollectionType.WISH),
                EpisodeCollection(ep(id = 3), collectionType = UnifiedCollectionType.WISH),
            ),
            hasAlreadyCached = {
                it.episode.id <= 1
            },
            maxCount = 3,
        ).toList()

        assertEquals(listOf(2), res.map { it.episode.id })
    }

    @Test
    fun `cache until max count when nothing cached`() = runTest {
        val res = DefaultMediaAutoCacheService.firstEpisodeToCache(
            listOf(
                EpisodeCollection(ep(id = 0), collectionType = UnifiedCollectionType.WISH),
                EpisodeCollection(ep(id = 1), collectionType = UnifiedCollectionType.WISH),
                EpisodeCollection(ep(id = 2), collectionType = UnifiedCollectionType.WISH),
                EpisodeCollection(ep(id = 3), collectionType = UnifiedCollectionType.WISH),
            ),
            hasAlreadyCached = {
                false
            },
            maxCount = 1,
        ).toList()

        assertEquals(listOf(0), res.map { it.episode.id })
    }
}
