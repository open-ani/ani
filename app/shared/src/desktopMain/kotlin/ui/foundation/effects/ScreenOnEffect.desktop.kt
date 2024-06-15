package me.him188.ani.app.ui.foundation.effects

import androidx.compose.runtime.Composable


/**
 * Composes an effect that keeps the screen on.
 *
 * When the composable gets removed from the view hierarchy, the screen will be allowed to turn off again.
 */
@Composable
actual fun ScreenOnEffectImpl() {
    // no-op
}