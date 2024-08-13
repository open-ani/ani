package me.him188.ani.app.ui.subject.episode.video

import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.videoplayer.ui.state.Chapter
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class PlayerSkipOpEdStateTest {

    class `OP chapter on start` : NeedTest() {
        override val opChapterOnStart = listOf(
            Chapter("chapter1 op", 90_000L, 0),
            Chapter("chapter2", 10_000L, 100_000L),
            Chapter("chapter3", 10_000L, 110_000L),
        )

        @BeforeTest
        fun setup() {
            state = createState_opChapterOnStart_24minutes()
        }

        override fun `before op 6s`() {
        }

        override fun `before op 3s`() {
        }

        @Test
        override fun `on op`() {
            state.update(0)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        override fun `after op 3s`() {
            state.update(3000)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        override fun `after op 6s`() {
            state.update(6000)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        override fun `cancel before op 6s`() {
        }

        override fun `cancel before op 3s`() {
        }

        @Test
        override fun `cancel on op`() {
            state.update(0)
            state.cancelSkipOpEd()
            state.update(1)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        override fun `cancel after op 3s`() {
            state.update(3000)
            state.cancelSkipOpEd()
            state.update(3001)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        override fun `cancel after op 6s`() {
            state.update(6000)
            state.cancelSkipOpEd()
            state.update(6001)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        override fun `cancel before op 6s then play to op`() {
        }

        override fun `cancel before op 3s then play to op`() {
        }

        override fun `after show tips user seek to other place`() {
        }

    }

    class `OP chapter on chapter 2` : NeedTest() {

        override val opChapterOnChapter2 = listOf(
            Chapter("chapter1", 10_000L, 0),
            Chapter("chapter2 op", 90_000L, 10_000L),
            Chapter("chapter3", 10_000L, 110_000L),
        )

        @BeforeTest
        fun setup() {
            state = createState_opChapterOnChapter2_24minutes()
        }

        @Test
        override fun `before op 6s`() {
            state.update(4000L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        override fun `before op 3s`() {
            state.update(7000L)
            assertEquals(true, state.showSkipTips)
            assertEquals(true, state.skipped)
        }

        @Test
        override fun `on op`() {
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
        override fun `after op 3s`() {
            state.update(13_000L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        override fun `after op 6s`() {
            state.update(16_000L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        override fun `cancel before op 6s`() {
            state.update(4_000L)
            state.cancelSkipOpEd()
            state.update(4_001L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        override fun `cancel before op 3s`() {
            state.update(7_000L)
            state.cancelSkipOpEd()
            state.update(7_001L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        override fun `cancel on op`() {
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
        override fun `cancel after op 3s`() {
            state.update(13_000L)
            state.cancelSkipOpEd()
            state.update(13_001L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        @Test
        override fun `cancel after op 6s`() {
            state.update(16_001L)
            state.cancelSkipOpEd()
            state.update(16_000L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

        override fun `cancel before op 6s then play to op`() {
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
            assertEquals(0L, skipTime)
            assertEquals(false, localState.showSkipTips)
            assertEquals(true, localState.skipped)
        }

        override fun `cancel before op 3s then play to op`() {
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

        override fun `after show tips user seek to other place`() {
            state.update(7_000L)
            assertEquals(true, state.showSkipTips)
            assertEquals(true, state.skipped)
            state.update(40_000L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipped)
        }

    }

    class `no OP chapter` : NeedTest() {

        override val noOpChapter = listOf(
            Chapter("chapter1", 10_000L, 0),
            Chapter("chapter2", 50_000L, 10_000L),
            Chapter("chapter3", 10_000L, 110_000L),
        )

        override fun `before op 6s`() {
            TODO("Not yet implemented")
        }

        override fun `before op 3s`() {
            TODO("Not yet implemented")
        }

        override fun `on op`() {
            TODO("Not yet implemented")
        }

        override fun `after op 3s`() {
            TODO("Not yet implemented")
        }

        override fun `after op 6s`() {
            TODO("Not yet implemented")
        }

        override fun `cancel before op 6s`() {
            TODO("Not yet implemented")
        }

        override fun `cancel before op 3s`() {
            TODO("Not yet implemented")
        }

        override fun `cancel on op`() {
            TODO("Not yet implemented")
        }

        override fun `cancel after op 3s`() {
            TODO("Not yet implemented")
        }

        override fun `cancel after op 6s`() {
            TODO("Not yet implemented")
        }

        override fun `cancel before op 6s then play to op`() {
            TODO("Not yet implemented")
        }

        override fun `cancel before op 3s then play to op`() {
            TODO("Not yet implemented")
        }

        override fun `after show tips user seek to other place`() {
            TODO("Not yet implemented")
        }

    }
}

abstract class NeedTest {
    lateinit var state: PlayerSkipOpEdState
    private val videoLength: Duration = 24.minutes
    open val opChapterOnStart: List<Chapter> = emptyList()
    open val opChapterOnChapter2: List<Chapter> = emptyList()
    open val noOpChapter: List<Chapter> = emptyList()
    abstract fun `before op 6s`()
    abstract fun `before op 3s`()
    abstract fun `on op`()
    abstract fun `after op 3s`()
    abstract fun `after op 6s`()

    abstract fun `cancel before op 6s`()
    abstract fun `cancel before op 3s`()
    abstract fun `cancel on op`()
    abstract fun `cancel after op 3s`()
    abstract fun `cancel after op 6s`()

    abstract fun `cancel before op 6s then play to op`()
    abstract fun `cancel before op 3s then play to op`()

    abstract fun `after show tips user seek to other place`()

    fun createState_opChapterOnStart_24minutes(onSkip: (targetMillis: Long) -> Unit = {}): PlayerSkipOpEdState {
        return PlayerSkipOpEdState(
            stateOf(opChapterOnStart),
            onSkip = onSkip,
            stateOf(videoLength),
        )
    }

    fun createState_opChapterOnChapter2_24minutes(onSkip: (targetMillis: Long) -> Unit = {}): PlayerSkipOpEdState {
        return PlayerSkipOpEdState(
            stateOf(opChapterOnChapter2),
            onSkip = onSkip,
            stateOf(videoLength),
        )
    }

    fun createState_noOpChapter_24minutes(onSkip: (targetMillis: Long) -> Unit = {}): PlayerSkipOpEdState {
        return PlayerSkipOpEdState(
            stateOf(noOpChapter),
            onSkip = onSkip,
            stateOf(videoLength),
        )
    }
}