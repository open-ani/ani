package me.him188.ani.app.ui.foundation.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import me.him188.ani.app.platform.window.WindowUtils


/**
 * Composes an effect that keeps the screen on.
 *
 * When the composable gets removed from the view hierarchy, the screen will be allowed to turn off again.
 */
@Composable
actual fun ScreenOnEffectImpl() {
    DisposableEffect(true) {
        WindowUtils.setPreventScreenSaver(true)
        onDispose {
            WindowUtils.setPreventScreenSaver(false)
        }
    }
}
