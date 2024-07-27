package me.him188.ani.app.platform.window

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * PC 上的 Window. Android 上没有
 */
expect class PlatformWindow

typealias PlatformWindowMP = PlatformWindow

val LocalPlatformWindow: ProvidableCompositionLocal<PlatformWindowMP> = staticCompositionLocalOf {
    error("No PlatformWindow provided")
}
