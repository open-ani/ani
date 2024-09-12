package me.him188.ani.app.ui.foundation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import me.him188.ani.utils.platform.currentPlatform

@Stable
val LocalPlatform = staticCompositionLocalOf {
    currentPlatform()
}
