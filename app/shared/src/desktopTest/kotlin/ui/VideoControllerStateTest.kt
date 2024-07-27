package ui

import kotlinx.coroutines.test.runTest
import me.him188.ani.app.videoplayer.ui.ControllerVisibility
import me.him188.ani.app.videoplayer.ui.VideoControllerState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class VideoControllerStateTest {
    @Test
    fun `test toggle visibility`() = runTest {
        val state = VideoControllerState(ControllerVisibility.Visible)
        state.toggleVisibility()
        assertEquals(ControllerVisibility.Invisible, state.visibility)
        state.toggleVisibility()
        assertEquals(ControllerVisibility.Visible, state.visibility)
        state.toggleVisibility(ControllerVisibility.Visible)
        assertEquals(ControllerVisibility.Visible, state.visibility)
        state.toggleVisibility(ControllerVisibility.Invisible)
        assertEquals(ControllerVisibility.Invisible, state.visibility)
    }

    @Test
    fun `show detached slider when init invisible`() = runTest {
        val state = VideoControllerState(ControllerVisibility.Invisible)
        state.setRequestProgressBarVisible()
        assertEquals(ControllerVisibility.DetachedSliderOnly, state.visibility)
        state.cancelRequestProgressBarVisible()
        assertEquals(ControllerVisibility.Invisible, state.visibility)

    }

    @Test
    fun `do not show detached slider when init visible `() = runTest {
        val state = VideoControllerState(ControllerVisibility.Visible)
        state.setRequestProgressBarVisible()
        assertEquals(ControllerVisibility.Visible, state.visibility)
        state.cancelRequestProgressBarVisible()
        assertEquals(ControllerVisibility.Visible, state.visibility)
    }
}