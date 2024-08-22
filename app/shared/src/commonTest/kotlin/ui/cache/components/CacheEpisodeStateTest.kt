package me.him188.ani.app.ui.cache.components

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.framework.runComposeStateTest
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import kotlin.test.Test
import kotlin.test.assertEquals

class CacheEpisodeStateTest {
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
    ): CacheEpisodeState {
        val state = mutableStateOf(initialPaused)
        return CacheEpisodeState(
            subjectId = subjectId,
            episodeId = episodeId,
            sort = EpisodeSort(sort),
            displayName = displayName,
            screenShots = stateOf(emptyList()),
            downloadSpeed = stateOf(downloadSpeed),
            progress = stateOf(progress),
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
            assertEquals("100 MB/s", downloadSpeedText)
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
            assertEquals("100 MB/s", downloadSpeedText)
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
            assertEquals(null, downloadSpeedText)
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
            assertEquals("100 MB/s", downloadSpeedText)
        }
        cacheEpisode(
            initialPaused = CacheEpisodePaused.PAUSED,
            downloadSpeed = 100.megaBytes,
            progress = 0.1f,
        ).run {
            assertEquals("100 MB/s", downloadSpeedText)
        }
    }

    @Test
    fun `do not show speed if finished`() = runComposeStateTest {
        cacheEpisode(
            initialPaused = CacheEpisodePaused.IN_PROGRESS,
            downloadSpeed = 100.megaBytes,
            progress = 1f,
        ).run {
            assertEquals(null, downloadSpeedText)
        }
        cacheEpisode(
            initialPaused = CacheEpisodePaused.PAUSED,
            downloadSpeed = 100.megaBytes,
            progress = 1f,
        ).run {
            assertEquals(null, downloadSpeedText)
        }
        cacheEpisode(
            initialPaused = CacheEpisodePaused.PAUSED,
            downloadSpeed = 100.megaBytes,
            progress = 2f,
        ).run {
            assertEquals(null, downloadSpeedText)
        }
    }
}
