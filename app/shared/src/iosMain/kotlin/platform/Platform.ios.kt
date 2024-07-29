package me.him188.ani.app.platform

import androidx.compose.runtime.Stable

@Stable
actual fun Platform.Companion.currentPlatformImpl(): Platform {
    return Platform.Ios
}