package me.him188.ani.app.videoplayer.ui.gesture

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.videoplayer.ui.guesture.GestureLock

@PreviewLightDark
@Composable
private fun PreviewGestureLockLocked() {
    GestureLock(true, {})
}

@PreviewLightDark
@Composable
private fun PreviewGestureLockUnlocked() {
    GestureLock(false, {})
}
