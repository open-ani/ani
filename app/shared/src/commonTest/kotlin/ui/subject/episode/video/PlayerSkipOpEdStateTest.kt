package me.him188.ani.app.ui.subject.episode.video

import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.videoplayer.ui.state.Chapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class PlayerSkipOpEdStateTest {
    class `OP chapter on start` {
        private val opChapterOnStart = listOf(
            Chapter("chapter1 op", 90_000L, 0),
            Chapter("chapter2", 10_000L, 100_000L),
            Chapter("chapter3", 10_000L, 110_000L),
        )

        private val videoLength = 24.minutes

        private fun createState_opChapterOnStart_24minutes(onSkip: (targetMillis: Long) -> Unit = {}): PlayerSkipOpEdState {
            return PlayerSkipOpEdState(
                stateOf(opChapterOnStart),
                onSkip = onSkip,
                stateOf(videoLength),
            )
        }

        @Test
        fun `on op`() {
            val state = createState_opChapterOnStart_24minutes()
            state.update(0)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        fun `after op 3s`() {
            val state = createState_opChapterOnStart_24minutes()
            state.update(3000)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        fun `after op 6s`() {
            val state = createState_opChapterOnStart_24minutes()
            state.update(6000)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        fun `cancel on op`() {
            val state = createState_opChapterOnStart_24minutes()
            state.update(0)
            state.cancelSkipOpEd()
            state.update(1)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        fun `cancel after op 3s`() {
            val state = createState_opChapterOnStart_24minutes()
            state.update(3000)
            state.cancelSkipOpEd()
            state.update(3001)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        fun `cancel after op 6s`() {
            val state = createState_opChapterOnStart_24minutes()
            state.update(6000)
            state.cancelSkipOpEd()
            state.update(6001)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }
    }

    class `OP chapter on chapter 2` {

        private val opChapterOnChapter2 = listOf(
            Chapter("chapter1", 10_000L, 0),
            Chapter("chapter2 op", 90_000L, 10_000L),
            Chapter("chapter3", 10_000L, 110_000L),
        )

        private val videoLength = 24.minutes

        private fun createState_opChapterOnChapter2_24minutes(onSkip: (targetMillis: Long) -> Unit = {}): PlayerSkipOpEdState {
            return PlayerSkipOpEdState(
                stateOf(opChapterOnChapter2),
                onSkip = onSkip,
                stateOf(videoLength),
            )
        }

        @Test
        fun `before op 6s`() {
            val state = createState_opChapterOnChapter2_24minutes()
            state.update(4000L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        fun `before op 3s`() {
            val state = createState_opChapterOnChapter2_24minutes()
            state.update(7000L)
            assertEquals(true, state.showSkipTips)
            assertEquals(true, state.skipped)
        }

        @Test
        fun `on op`() {
            var skipTime = 0L
            val localState = createState_opChapterOnChapter2_24minutes {
                skipTime = it
            }
            localState.update(10_000L)
            assertEquals(100_000L, skipTime)
            assertEquals(false, localState.showSkipTips)
            assertEquals(false, localState.skipped)
        }

        @Test
        fun `after op 3s`() {
            val state = createState_opChapterOnChapter2_24minutes()
            state.update(13_000L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        fun `after op 6s`() {
            val state = createState_opChapterOnChapter2_24minutes()
            state.update(16_000L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        fun `cancel before op 6s`() {
            val state = createState_opChapterOnChapter2_24minutes()
            state.update(4_000L)
            state.cancelSkipOpEd()
            state.update(4_001L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        fun `cancel before op 3s`() {
            val state = createState_opChapterOnChapter2_24minutes()
            state.update(7_000L)
            state.cancelSkipOpEd()
            state.update(7_001L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        fun `cancel on op`() {
            var skipTime = 0L
            val localState = createState_opChapterOnChapter2_24minutes {
                skipTime = it
            }
            localState.update(10_000L)
            localState.cancelSkipOpEd()
            localState.update(10_001L)
            assertEquals(100_000L, skipTime)
            assertEquals(false, localState.showSkipTips)
            assertEquals(false, localState.skipped)
        }

        @Test
        fun `cancel after op 3s`() {
            val state = createState_opChapterOnChapter2_24minutes()
            state.update(13_000L)
            state.cancelSkipOpEd()
            state.update(13_001L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        fun `cancel after op 6s`() {
            val state = createState_opChapterOnChapter2_24minutes()
            state.update(16_000L)
            state.cancelSkipOpEd()
            state.update(16_001L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        fun `cancel before op 6s then play to op`() {
            var skipTime = 0L
            val localState = createState_opChapterOnChapter2_24minutes {
                skipTime = it
            }
            localState.update(4_000L)
            localState.cancelSkipOpEd()
            localState.update(4_001L)
            assertEquals(false, localState.showSkipTips)
            assertEquals(false, localState.skipped)
            localState.update(10_000L)
            assertEquals(100_000L, skipTime)
            assertEquals(false, localState.showSkipTips)
            assertEquals(false, localState.skipped)
        }

        @Test
        fun `cancel before op 3s then play to op`() {
            var skipTime = 0L
            val localState = createState_opChapterOnChapter2_24minutes {
                skipTime = it
            }
            localState.update(7_000L)
            localState.cancelSkipOpEd()
            localState.update(7_001L)
            assertEquals(false, localState.showSkipTips)
            assertEquals(false, localState.skipped)
            localState.update(10_000L)
            assertEquals(0L, skipTime)
            assertEquals(false, localState.showSkipTips)
            assertEquals(false, localState.skipped)
        }

        @Test
        fun `after show tips user seek to other place`() {
            val state = createState_opChapterOnChapter2_24minutes()
            state.update(7_000L)
            assertEquals(true, state.showSkipTips)
            assertEquals(true, state.skipped)
            state.update(40_000L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

    }
}