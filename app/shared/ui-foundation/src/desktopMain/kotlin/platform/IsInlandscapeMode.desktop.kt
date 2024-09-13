package me.him188.ani.app.platform

import androidx.compose.runtime.Composable

/**
 * 横屏模式. 横屏模式不一定是全屏.
 *
 * PC 一定处于横屏模式.
 *
 * @see isSystemInFullscreenImpl
 */
@Composable
actual fun isInLandscapeMode(): Boolean = true
