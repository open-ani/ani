package me.him188.ani.app.ui.cache.components

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.framework.runComposeStateTest
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.Unspecified
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import kotlin.test.Test
import kotlin.test.assertEquals

class CacheEpisodeStateTest {
    class CalculateSizeTextTest {
        @Test
        fun `all unavailable`() {
            check(null, Unspecified, null)
        }

        @Test
        fun `progress unavailable - total size available`() =
            check("200 MB", 200.megaBytes, null)

        @Test
        fun `progress available - total size unavailable`() =
            check(null, Unspecified, 0.5f)

        @Test
        fun `all available`() =
            check("100 MB / 200 MB", 200.megaBytes, 0.5f)

        private fun check(expected: String?, total: FileSize, progress: Float?) {
            assertEquals(
                expected,
                CacheEpisodeState.calculateSizeText(
                    totalSize = total,
                    progress = progress,
                ),
            )
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Suppress("SameParameterValue")
    private fun cacheEpisode(
        sort: Int = 1,
        displayName: String = "翻转孤独",
        subjectId: Int = 1,
        episodeId: Int = 1,
        initialPaused: CacheEpisodePaused = when (sort % 2) {
            0 -> CacheEpisodePaused.PAUSED
            else -> CacheEpisodePaused.IN_PROGRESS
        },
        downloadSpeed: FileSize = 100.megaBytes,
        progress: Float? = 0.9f,
        totalSize: FileSize = 200.megaBytes,
    ): CacheEpisodeState {
        val state = mutableStateOf(initialPaused)
        return CacheEpisodeState(
            subjectId = subjectId,
            episodeId = episodeId,
            cacheId = "1",
            sort = EpisodeSort(sort),
            displayName = displayName,
            screenShots = stateOf(emptyList()),
            stats = stateOf(
                CacheEpisodeState.Stats(
                    downloadSpeed = downloadSpeed,
                    progress = progress,
                    totalSize = totalSize,
                ),
            ),
            state = state,
            onPause = { state.value = CacheEpisodePaused.PAUSED },
            onResume = { state.value = CacheEpisodePaused.IN_PROGRESS },
            onDelete = {},
            onPlay = {},
            backgroundScope = GlobalScope,
        )
    }

    @Test
    fun `progress not available`() = runComposeStateTest {
        cacheEpisode(
            initialPaused = CacheEpisodePaused.IN_PROGRESS,
            downloadSpeed = 100.megaBytes,
            progress = null,
        ).run {
            assertEquals(false, isPaused)
            assertEquals(false, isFinished)
            assertEquals("200 MB", sizeText)
            assertEquals("99%", progressText)
            assertEquals(0f, progress)
            assertEquals(true, isProgressUnspecified)
        }
    }

    @Test
    fun `in progress and not finished`() = runComposeStateTest {
        cacheEpisode(
            initialPaused = CacheEpisodePaused.IN_PROGRESS,
            downloadSpeed = 100.megaBytes,
            progress = 0.1f,
        ).run {
            assertEquals(false, isPaused)
            assertEquals(false, isFinished)
            assertEquals(null, sizeText)
            assertEquals("100 MB/s", progressText)
            assertEquals(0.1f, progress)
            assertEquals(false, isProgressUnspecified)
        }
    }

    @Test
    fun `in progress and finished`() = runComposeStateTest {
        cacheEpisode(
            initialPaused = CacheEpisodePaused.IN_PROGRESS,
            downloadSpeed = 100.megaBytes,
            progress = 1f,
        ).run {
            assertEquals(false, isPaused)
            assertEquals(true, isFinished)
            assertEquals(null, sizeText)
            assertEquals(null, progressText)
            assertEquals(1f, progress)
            assertEquals(false, isProgressUnspecified)
        }
    }

    @Test
    fun `show speed if not finished`() = runComposeStateTest {
        cacheEpisode(
            initialPaused = CacheEpisodePaused.IN_PROGRESS,
            downloadSpeed = 100.megaBytes,
            progress = 0.1f,
        ).run {
            assertEquals(null, sizeText)
            assertEquals("100 MB/s", progressText)
        }
        cacheEpisode(
            initialPaused = CacheEpisodePaused.PAUSED,
            downloadSpeed = 100.megaBytes,
            progress = 0.1f,
        ).run {
            assertEquals(null, sizeText)
            assertEquals("100 MB/s", progressText)
        }
    }

    @Test
    fun `do not show speed if finished`() = runComposeStateTest {
        cacheEpisode(
            initialPaused = CacheEpisodePaused.IN_PROGRESS,
            downloadSpeed = 100.megaBytes,
            progress = 1f,
        ).run {
            assertEquals(null, sizeText)
            assertEquals(null, progressText)
        }
        cacheEpisode(
            initialPaused = CacheEpisodePaused.PAUSED,
            downloadSpeed = 100.megaBytes,
            progress = 1f,
        ).run {
            assertEquals(null, sizeText)
            assertEquals(null, progressText)
        }
        cacheEpisode(
            initialPaused = CacheEpisodePaused.PAUSED,
            downloadSpeed = 100.megaBytes,
            progress = 2f,
        ).run {
            assertEquals(null, sizeText)
            assertEquals(null, progressText)
        }
    }
}
