package me.him188.ani.app.data.media

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.openapitools.client.models.Episode
import org.openapitools.client.models.EpisodeCollectionType
import org.openapitools.client.models.UserEpisodeCollection
import java.math.BigDecimal
import kotlin.test.assertEquals

class MediaAutoCacheServiceTest {
    private var id = 0
    private fun ep(
        id: Int = this.id++,
    ) = Episode(
        id = id,
        type = 5956,
        name = "Diana Houston",
        nameCn = "Nita O'Donnell",
        sort = BigDecimal.ONE,
        airdate = "phasellus",
        comment = 5931,
        duration = "",
        desc = "gubergren",
        disc = 2272,
        ep = BigDecimal.ONE,
        durationSeconds = null
    )

    @Test
    fun `no cache for watched`() = runTest {
        val res = DefaultMediaAutoCacheService.firstEpisodeToCache(
            flowOf(
                UserEpisodeCollection(
                    ep(),
                    type = EpisodeCollectionType.WATCHED,
                ),
            ),
            hasAlreadyCached = {
                false
            }
        ).toList()

        assertEquals(0, res.size)
    }

    @Test
    fun `no cache for already cached`() = runTest {
        val res = DefaultMediaAutoCacheService.firstEpisodeToCache(
            flowOf(
                UserEpisodeCollection(
                    ep(),
                    type = EpisodeCollectionType.WATCHLIST,
                ),
            ),
            hasAlreadyCached = {
                true
            }
        ).toList()

        assertEquals(0, res.size)
    }

    @Test
    fun `cache unwatched`() = runTest {
        val res = DefaultMediaAutoCacheService.firstEpisodeToCache(
            flowOf(
                UserEpisodeCollection(
                    ep(),
                    type = EpisodeCollectionType.WATCHLIST,
                ),
            ),
            hasAlreadyCached = {
                false
            }
        ).toList()

        assertEquals(1, res.size)
    }

    @Test
    fun `cache unwatched multiple`() = runTest {
        val res = DefaultMediaAutoCacheService.firstEpisodeToCache(
            flowOf(
                UserEpisodeCollection(ep(id = 0), type = EpisodeCollectionType.WATCHED),
                UserEpisodeCollection(ep(id = 1), type = EpisodeCollectionType.WATCHLIST),
                UserEpisodeCollection(ep(id = 2), type = EpisodeCollectionType.WATCHLIST),
                UserEpisodeCollection(ep(id = 3), type = EpisodeCollectionType.WATCHLIST),
            ),
            hasAlreadyCached = {
                false
            }
        ).toList()

        assertEquals(3, res.size)
        assertEquals(1, res.first().episode.id)
    }

    @Test
    fun `no cache when already cached many`() = runTest {
        val res = DefaultMediaAutoCacheService.firstEpisodeToCache(
            flowOf(
                UserEpisodeCollection(ep(id = 0), type = EpisodeCollectionType.WATCHLIST),
                UserEpisodeCollection(ep(id = 1), type = EpisodeCollectionType.WATCHLIST),
                UserEpisodeCollection(ep(id = 2), type = EpisodeCollectionType.WATCHLIST),
                UserEpisodeCollection(ep(id = 3), type = EpisodeCollectionType.WATCHLIST),
            ),
            hasAlreadyCached = {
                it.episode.id <= 0
            },
            maxCount = 1
        ).toList()

        assertEquals(0, res.size)
    }

    @Test
    fun `cache until max count`() = runTest {
        val res = DefaultMediaAutoCacheService.firstEpisodeToCache(
            flowOf(
                UserEpisodeCollection(ep(id = 0), type = EpisodeCollectionType.WATCHLIST),
                UserEpisodeCollection(ep(id = 1), type = EpisodeCollectionType.WATCHLIST),
                UserEpisodeCollection(ep(id = 2), type = EpisodeCollectionType.WATCHLIST),
                UserEpisodeCollection(ep(id = 3), type = EpisodeCollectionType.WATCHLIST),
            ),
            hasAlreadyCached = {
                it.episode.id <= 1
            },
            maxCount = 3
        ).toList()

        assertEquals(listOf(2), res.map { it.episode.id })
    }

    @Test
    fun `cache until max count when nothing cached`() = runTest {
        val res = DefaultMediaAutoCacheService.firstEpisodeToCache(
            flowOf(
                UserEpisodeCollection(ep(id = 0), type = EpisodeCollectionType.WATCHLIST),
                UserEpisodeCollection(ep(id = 1), type = EpisodeCollectionType.WATCHLIST),
                UserEpisodeCollection(ep(id = 2), type = EpisodeCollectionType.WATCHLIST),
                UserEpisodeCollection(ep(id = 3), type = EpisodeCollectionType.WATCHLIST),
            ),
            hasAlreadyCached = {
                false
            },
            maxCount = 1
        ).toList()

        assertEquals(listOf(0), res.map { it.episode.id })
    }
}
