package me.him188.ani.app.platform.window

import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import me.him188.ani.app.platform.currentPlatformDesktop

interface WindowUtils {
    fun setTitleBarColor(hwnd: Long, color: Color): Boolean {
        return false
    }

    fun setFullscreen(window: PlatformWindow, fullscreen: Boolean) {
    }

    fun setPreventScreenSaver(prevent: Boolean) {
    }

    companion object : WindowUtils by (when (currentPlatformDesktop) {
        is me.him188.ani.app.platform.Platform.Linux -> NoopWindowUtils
        is me.him188.ani.app.platform.Platform.MacOS -> NoopWindowUtils
        is me.him188.ani.app.platform.Platform.Windows -> WindowsWindowUtils()
    })
}

fun ComposeWindow.setTitleBarColor(color: Color) {
    WindowUtils.setTitleBarColor(windowHandle, color)
}

private object NoopWindowUtils : WindowUtils
