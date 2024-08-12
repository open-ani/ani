package ui.subject.episode.video

import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.subject.episode.video.PlayerSkipOpEdState
import me.him188.ani.app.videoplayer.ui.state.Chapter
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class PlayerSkipOpEdStateTest {

    class `24 minutes - opChapterOnStart` : NeedTest() {
        override val opChapterOnStart = listOf(
            Chapter("chapter1 op", 90_000L, 0),
            Chapter("chapter2", 10_000L, 100_000L),
            Chapter("chapter3", 10_000L, 110_000L),
        )

        @BeforeTest
        fun setup() {
            state = createState_opChapterOnStart_24minutes()
        }

        override val videoLength: Duration
            get() = 24.minutes

        override fun `before op 6s`() {

        }

        override fun `before op 3s`() {
        }

        @Test
        override fun `on op`() {
            state.update(0)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipCancel)
        }

        @Test
        override fun `after op 3s`() {
            state.update(3000)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipCancel)
        }

        @Test
        override fun `after op 6s`() {
            state.update(6000)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipCancel)
        }

        override fun `cancel before op 6s`() {
        }

        override fun `cancel before op 3s`() {

        }

        @Test
        override fun `cancel on op`() {
            state.cancelSkipOpEd()
            state.update(0)
            assertEquals(false, state.showSkipTips)
            assertEquals(true, state.skipCancel)
        }

        @Test
        override fun `cancel after op 3s`() {
            state.cancelSkipOpEd()
            state.update(3000)
            assertEquals(false, state.showSkipTips)
            assertEquals(true, state.skipCancel)
        }

        @Test
        override fun `cancel after op 6s`() {
            state.cancelSkipOpEd()
            state.update(6000)
            assertEquals(false, state.showSkipTips)
            assertEquals(true, state.skipCancel)
        }

    }

    class `24 minutes - opChapterOnChapter2` : NeedTest() {
        override val videoLength: Duration
            get() = 24.minutes

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
            assertEquals(false, state.skipCancel)
        }

        @Test
        override fun `before op 3s`() {
            state.update(7000L)
            assertEquals(true, state.showSkipTips)
            assertEquals(false, state.skipCancel)
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
            assertEquals(false, localState.skipCancel)
        }

        @Test
        override fun `after op 3s`() {
            state.update(13_000L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipCancel)
        }

        @Test
        override fun `after op 6s`() {
            state.update(16_000L)
            assertEquals(false, state.showSkipTips)
            assertEquals(false, state.skipCancel)
        }

        @Test
        override fun `cancel before op 6s`() {
            state.cancelSkipOpEd()
            state.update(4_000L)
            assertEquals(false, state.showSkipTips)
            assertEquals(true, state.skipCancel)
        }

        @Test
        override fun `cancel before op 3s`() {
            state.cancelSkipOpEd()
            state.update(7_000L)
            assertEquals(false, state.showSkipTips)
            assertEquals(true, state.skipCancel)
        }

        @Test
        override fun `cancel on op`() {
            var skipTime = 0L
            val localState = createState_opChapterOnChapter2_24minutes {
                skipTime = it
            }
            localState.cancelSkipOpEd()
            localState.update(10_000L)
            assertEquals(0L, skipTime)
            assertEquals(false, localState.showSkipTips)
            assertEquals(true, localState.skipCancel)
        }

        @Test
        override fun `cancel after op 3s`() {
            state.cancelSkipOpEd()
            state.update(13_000L)
            assertEquals(false, state.showSkipTips)
            assertEquals(true, state.skipCancel)
        }

        @Test
        override fun `cancel after op 6s`() {
            state.cancelSkipOpEd()
            state.update(16_000L)
            assertEquals(false, state.showSkipTips)
            assertEquals(true, state.skipCancel)
        }

    }

    class `24 minutes - noOpChapter` : NeedTest() {
        override val videoLength: Duration
            get() = 24.minutes

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

    }
}

abstract class NeedTest {
    lateinit var state: PlayerSkipOpEdState
    abstract val videoLength: Duration
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