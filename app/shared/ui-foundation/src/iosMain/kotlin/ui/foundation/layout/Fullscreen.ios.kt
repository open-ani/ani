package me.him188.ani.app.ui.foundation.layout

import androidx.compose.runtime.Composable
import me.him188.ani.app.platform.Context

/**
 * @see isInLandscapeMode
 */
@Composable
actual fun isSystemInFullscreenImpl(): Boolean {
    TODO("Not yet implemented isSystemInFullscreenImpl")
}

actual suspend fun Context.setRequestFullScreen(window: PlatformWindowMP, fullscreen: Boolean) {
    TODO("Not yet implemented setRequestFullScreen")
}

actual suspend fun Context.setSystemBarVisible(visible: Boolean) {
}