package me.him188.ani.app.ui.foundation.layout

import androidx.compose.runtime.Composable
import me.him188.ani.app.platform.Context


/**
 * Request to set the fullscreen, landscape mode, hiding status bars and navigation bars.
 *
 * If [fullscreen] is `true`, the app will enter fullscreen mode.
 * Otherwise, the app go back to the users' preferred mode.
 *
 * Note that when [fullscreen] is `false`, the system bars will be visible,
 * but the app may be still in landscape mode if the user's system is in landscape mode.
 */
expect suspend fun Context.setRequestFullScreen(window: PlatformWindowMP, fullscreen: Boolean)

@Composable
inline fun isSystemInFullscreen(): Boolean = isSystemInFullscreenImpl()

/**
 * @see isInLandscapeMode
 */
@Composable
expect fun isSystemInFullscreenImpl(): Boolean
