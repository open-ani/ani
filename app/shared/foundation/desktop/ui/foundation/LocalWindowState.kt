package me.him188.ani.app.ui.foundation

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.WindowState

@Stable
val LocalWindowState: ProvidableCompositionLocal<WindowState> = staticCompositionLocalOf {
    error("LocalWindowState not initialized")
}