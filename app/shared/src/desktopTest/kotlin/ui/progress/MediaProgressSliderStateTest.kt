package ui.progress

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.him188.ani.app.ui.framework.runComposeStateTest
import me.him188.ani.app.videoplayer.ui.progress.MediaProgressSliderState
import me.him188.ani.app.videoplayer.ui.state.Chapter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class MediaProgressSliderStateTest {

    @Test
    fun testMediaProgressSliderState() = runComposeStateTest {
        val chapters = object : ImmutableList<Chapter> {
            override val size: Int
                get() = TODO("Not yet implemented")

            override fun get(index: Int): Chapter {
                TODO("Not yet implemented")
            }

            override fun isEmpty(): Boolean {
                TODO("Not yet implemented")
            }

            override fun iterator(): Iterator<Chapter> {
                TODO("Not yet implemented")
            }

            override fun listIterator(): ListIterator<Chapter> {
                TODO("Not yet implemented")
            }

            override fun listIterator(index: Int): ListIterator<Chapter> {
                TODO("Not yet implemented")
            }

            override fun lastIndexOf(element: Chapter): Int {
                TODO("Not yet implemented")
            }

            override fun indexOf(element: Chapter): Int {
                TODO("Not yet implemented")
            }

            override fun containsAll(elements: Collection<Chapter>): Boolean {
                TODO("Not yet implemented")
            }

            override fun contains(element: Chapter): Boolean {
                TODO("Not yet implemented")
            }

        }
        var positionState by mutableLongStateOf(0L)
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            while (true) {
                if (positionState > 100) break
                positionState += 1
                delay(1000)
            }
        }

        val state = MediaProgressSliderState(
            { positionState }, { 100L }, mutableStateOf(chapters), {},
            {
                positionState = it
            },
        )

        state.previewPositionRatio(0.5f)
        state.finishPreview()
        assertEquals(0.5f, state.displayPositionRatio)
        delay(5.seconds)
        assertEquals(0.55f, state.displayPositionRatio)
    }
}