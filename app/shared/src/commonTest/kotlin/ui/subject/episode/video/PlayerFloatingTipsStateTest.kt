package ui.subject.episode.video

import me.him188.ani.app.ui.subject.episode.video.PlayerFloatingTipsState
import me.him188.ani.app.videoplayer.ui.state.Chapter
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerFloatingTipsStateTest {
    val _24minute = 24 * 60 * 1000L
    val _12minute = 12 * 60 * 1000L

    val chapters = listOf(
        Chapter("op", 90_000L, 10_000L),
        Chapter("chapter1", 5000L, 115_000L),
        Chapter("op 60s", 60_000L, 10_000L),
    )

    @Test
    fun `pos before op 10s when 12 minute`() {
        val state = PlayerFloatingTipsState()
        state.autoSkipOpEd(0, _12minute, chapters) {

        }
        assertEquals(false, state.leftBottomTipsVisible)
        assertEquals(true, state.skipOpEd)
    }

    @Test
    fun `pos before op 5s when 12 minute`() {
        val state = PlayerFloatingTipsState()
        state.autoSkipOpEd(5000L, _12minute, chapters) {

        }
        assertEquals(true, state.leftBottomTipsVisible)
        assertEquals(true, state.skipOpEd)
    }

    @Test
    fun `before op 5s and cancel skip when 12 minute`() {
        val state = PlayerFloatingTipsState()
        state.autoSkipOpEd(5000L, _12minute, chapters) {

        }
        assertEquals(true, state.leftBottomTipsVisible)
        assertEquals(true, state.skipOpEd)

        state.cancelSkipOpEd()

        assertEquals(false, state.leftBottomTipsVisible)
        assertEquals(false, state.skipOpEd)
    }

    @Test
    fun `on op and skip to 70s when 12 minute`() {
        val state = PlayerFloatingTipsState()
        var time = 0L
        state.autoSkipOpEd(10_000L, _12minute, chapters) {
            time = it
        }
        assertEquals(70_000L, time)
    }

    @Test
    fun `on op and cancel skip when 12 minute`() {
        val state = PlayerFloatingTipsState()
        state.cancelSkipOpEd()
        var time = 0L
        state.autoSkipOpEd(10_000L, _12minute, chapters) {
            time = it
        }
        assertEquals(0L, time)
    }


    @Test
    fun `pos before op 10s when 24 minute`() {
        val state = PlayerFloatingTipsState()
        state.autoSkipOpEd(0, _24minute, chapters) {

        }
        assertEquals(false, state.leftBottomTipsVisible)
        assertEquals(true, state.skipOpEd)
    }

    @Test
    fun `pos before op 5s when 24 minute`() {
        val state = PlayerFloatingTipsState()
        state.autoSkipOpEd(5000L, _24minute, chapters) {

        }
        assertEquals(true, state.leftBottomTipsVisible)
        assertEquals(true, state.skipOpEd)
    }

    @Test
    fun `before op 5s and cancel skip when 24 minute`() {
        val state = PlayerFloatingTipsState()
        state.autoSkipOpEd(5000L, _24minute, chapters) {

        }
        assertEquals(true, state.leftBottomTipsVisible)
        assertEquals(true, state.skipOpEd)

        state.cancelSkipOpEd()

        assertEquals(false, state.leftBottomTipsVisible)
        assertEquals(false, state.skipOpEd)
    }

    @Test
    fun `on op and skip to 100s when 24 minute`() {
        val state = PlayerFloatingTipsState()
        var time = 0L
        state.autoSkipOpEd(10_000L, _24minute, chapters) {
            time = it
        }
        assertEquals(100_000L, time)
    }

    @Test
    fun `on op and cancel skip when 24 minute`() {
        val state = PlayerFloatingTipsState()
        state.cancelSkipOpEd()
        var time = 0L
        state.autoSkipOpEd(10_000L, _24minute, chapters) {
            time = it
        }
        assertEquals(0L, time)
    }

    @Test
    fun `before chapter1 5s`() {
        val state = PlayerFloatingTipsState()
        state.autoSkipOpEd(110_000L, _24minute, chapters) {

        }
        assertEquals(false, state.leftBottomTipsVisible)
        assertEquals(true, state.skipOpEd)
    }

    @Test
    fun `on chapter1`() {
        val state = PlayerFloatingTipsState()
        var time = 0L
        state.autoSkipOpEd(115_000L, _24minute, chapters) {
            time = it
        }
        assertEquals(0L, time)
    }
}