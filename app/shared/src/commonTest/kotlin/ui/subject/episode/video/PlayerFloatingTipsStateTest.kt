package ui.subject.episode.video

import me.him188.ani.app.ui.subject.episode.video.PlayerFloatingTipsState
import me.him188.ani.app.videoplayer.ui.state.Chapter
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerFloatingTipsStateTest {
    private val _24minute = 24 * 60 * 1000L
    private val _12minute = 12 * 60 * 1000L

    private val chapters = listOf(
        Chapter("op", 90_000L, 10_000L),
        Chapter("chapter1", 5000L, 115_000L),
        Chapter("op 60s", 60_000L, 10_000L),
    )

    @Test
    fun `12 minutes - pos before op 10s`() {
        val state = PlayerFloatingTipsState()
        state.calculateTargetTime(chapters, 0, _12minute)
        assertEquals(false, state.showSkipOpEd)
        assertEquals(true, state.enableSkipOpEd)
    }

    @Test
    fun `12 minutes - pos before op 5s`() {
        val state = PlayerFloatingTipsState()
        state.calculateTargetTime(chapters, 5000L, _12minute)
        assertEquals(true, state.showSkipOpEd)
        assertEquals(true, state.enableSkipOpEd)
    }

    @Test
    fun `12 minutes - before op 5s and cancel skip`() {
        val state = PlayerFloatingTipsState()
        state.calculateTargetTime(chapters, 5000L, _12minute)
        assertEquals(true, state.showSkipOpEd)
        assertEquals(true, state.enableSkipOpEd)

        state.cancelSkipOpEd()

        assertEquals(false, state.showSkipOpEd)
        assertEquals(false, state.enableSkipOpEd)
    }

    @Test
    fun `12 minutes - on op and skip to 70s`() {
        val state = PlayerFloatingTipsState()
        val time = state.calculateTargetTime(chapters, 10_000L, _12minute) 
        assertEquals(70_000L, time)
    }

    @Test
    fun `12 minutes - on op and cancel skip`() {
        val state = PlayerFloatingTipsState()
        state.cancelSkipOpEd()
        val time = state.calculateTargetTime(chapters, 10_000L, _12minute)
        assertEquals(null, time)
    }


    @Test
    fun `24 minutes - pos before op 10s`() {
        val state = PlayerFloatingTipsState()
        state.calculateTargetTime(chapters, 0, _24minute)
        assertEquals(false, state.showSkipOpEd)
        assertEquals(true, state.enableSkipOpEd)
    }

    @Test
    fun `24 minutes - pos before op 5s`() {
        val state = PlayerFloatingTipsState()
        state.calculateTargetTime(chapters, 5000L, _24minute)
        assertEquals(true, state.showSkipOpEd)
        assertEquals(true, state.enableSkipOpEd)
    }

    @Test
    fun `24 minutes - before op 5s and cancel skip`() {
        val state = PlayerFloatingTipsState()
        state.calculateTargetTime(chapters, 5000L, _24minute)
        assertEquals(true, state.showSkipOpEd)
        assertEquals(true, state.enableSkipOpEd)

        state.cancelSkipOpEd()

        assertEquals(false, state.showSkipOpEd)
        assertEquals(false, state.enableSkipOpEd)
    }

    @Test
    fun `24 minutes - on op and skip to 100s`() {
        val state = PlayerFloatingTipsState()
        val time = state.calculateTargetTime(chapters, 10_000L, _24minute) 
        assertEquals(100_000L, time)
    }

    @Test
    fun `24 minutes - on op and cancel skip`() {
        val state = PlayerFloatingTipsState()
        state.cancelSkipOpEd()
        val time = state.calculateTargetTime(chapters, 10_000L, _24minute)
        assertEquals(null, time)
    }

    @Test
    fun `before chapter1 5s`() {
        val state = PlayerFloatingTipsState()
        state.calculateTargetTime(chapters, 110_000L, _24minute)
        assertEquals(false, state.showSkipOpEd)
        assertEquals(true, state.enableSkipOpEd)
    }

    @Test
    fun `on chapter1`() {
        val state = PlayerFloatingTipsState()
        val time = state.calculateTargetTime(chapters, 115_000L, _24minute)
        assertEquals(null, time)
    }

    @Test
    fun `do not show tips before op 5s when already cancel skip`() {
        val state = PlayerFloatingTipsState()
        state.cancelSkipOpEd()
        state.calculateTargetTime(chapters, 5_000L, _24minute)
        assertEquals(false, state.showSkipOpEd)
    }
}