package me.him188.ani.app.videoplayer.ui

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VideoControllerStateTest {
    @Test
    fun `test toggle visibility`() = runTest {
        val state = VideoControllerState(ControllerVisibility.Visible)
        state.toggleFullVisible()
        assertEquals(ControllerVisibility.Invisible, state.visibility)
        state.toggleFullVisible()
        assertEquals(ControllerVisibility.Visible, state.visibility)
        state.toggleFullVisible(true)
        assertEquals(ControllerVisibility.Visible, state.visibility)
        state.toggleFullVisible(false)
        assertEquals(ControllerVisibility.Invisible, state.visibility)
    }

    @Test
    fun `show detached slider when init invisible`() = runTest {
        val state = VideoControllerState(ControllerVisibility.Invisible)
        val requester = Any()
        state.setRequestProgressBar(requester)
        assertEquals(ControllerVisibility.DetachedSliderOnly, state.visibility)
        state.cancelRequestProgressBarVisible(requester)
        assertEquals(ControllerVisibility.Invisible, state.visibility)

    }

    @Test
    fun `do not show detached slider when init visible `() = runTest {
        val state = VideoControllerState(ControllerVisibility.Visible)
        val requester = Any()
        state.setRequestProgressBar(requester)
        assertEquals(ControllerVisibility.Visible, state.visibility)
        state.cancelRequestProgressBarVisible(requester)
        assertEquals(ControllerVisibility.Visible, state.visibility)
    }

    @Test
    fun `visibility when nothing`() {
        val state = createStateRequested(false, false, false)
        assertEquals(ControllerVisibility.Invisible, state.visibility)
    }

    @Test
    fun `visibility when alwaysOn`() {
        val state = createStateRequested(true, false, false)
        assertEquals(ControllerVisibility.Visible, state.visibility)
    }

    @Test
    fun `visibility when progressBarVisible`() {
        val state = createStateRequested(false, true, false)
        assertEquals(ControllerVisibility.DetachedSliderOnly, state.visibility)
    }

    @Test
    fun `visibility when fullVisible`() {
        val state = createStateRequested(false, false, true)
        assertEquals(ControllerVisibility.Visible, state.visibility)
    }

    @Test
    fun `visibility when alwaysOn progressBarVisible`() {
        val state = createStateRequested(true, true, false)
        assertEquals(ControllerVisibility.Visible, state.visibility)
    }

    @Test
    fun `visibility when alwaysOn fullVisible`() {
        val state = createStateRequested(true, false, true)
        assertEquals(ControllerVisibility.Visible, state.visibility)
    }

    @Test
    fun `visibility when progressBarVisible fullVisible`() {
        val state = createStateRequested(false, true, true)
        assertEquals(ControllerVisibility.Visible, state.visibility)
    }

    @Test
    fun `visibility when alwaysOn progressBarVisible fullVisible`() {
        val state = createStateRequested(true, true, true)
        assertEquals(ControllerVisibility.Visible, state.visibility)
    }

    private fun createStateRequested(
        alwaysOn: Boolean,
        progressBarVisible: Boolean,
        fullVisible: Boolean
    ): VideoControllerState {
        val state = VideoControllerState(ControllerVisibility.Visible)
        val requester = Any()
        state.setRequestAlwaysOn(requester, alwaysOn)
        if (progressBarVisible)
            state.setRequestProgressBar(requester)
        state.toggleFullVisible(fullVisible)
        return state
    }
}