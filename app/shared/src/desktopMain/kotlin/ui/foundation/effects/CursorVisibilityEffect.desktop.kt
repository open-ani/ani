package me.him188.ani.app.ui.foundation.effects

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import me.him188.ani.app.platform.window.LocalPlatformWindow
import me.him188.ani.app.platform.window.WindowUtils
import me.him188.ani.app.ui.foundation.LocalIsPreviewing

const val TAG_CURSOR_VISIBILITY_EFFECT_VISIBLE = "CursorVisibilityEffect-visible"
const val TAG_CURSOR_VISIBILITY_EFFECT_INVISIBLE = "CursorVisibilityEffect-invisible"

@Composable
actual fun CursorVisibilityEffect(key: Any?, visible: Boolean) {
    val isPreviewing = LocalIsPreviewing.current
    if (isPreviewing) {
        if (visible) {
            Box(Modifier.testTag(TAG_CURSOR_VISIBILITY_EFFECT_VISIBLE))
        } else {
            Box(Modifier.testTag(TAG_CURSOR_VISIBILITY_EFFECT_INVISIBLE))
        }
        return
    }

    val window = LocalPlatformWindow.current
    DisposableEffect(key, visible, window) {
        val original = true
        WindowUtils.setCursorVisible(window.composeWindow, visible)
        onDispose {
            WindowUtils.setCursorVisible(window.composeWindow, original)
        }
    }
}
