package me.him188.ani.app.platform.window

import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import me.him188.ani.utils.platform.Platform
import java.awt.Cursor
import java.awt.Point
import java.awt.Toolkit
import java.awt.image.BufferedImage


/**
 * @see AwtWindowUtils
 */
interface WindowUtils {
    fun setTitleBarColor(hwnd: Long, color: Color): Boolean {
        return false
    }

    fun setUndecoratedFullscreen(window: PlatformWindow, undecorated: Boolean) {
    }

    fun setPreventScreenSaver(prevent: Boolean) {
    }

    fun isCursorVisible(window: ComposeWindow): Boolean

    fun setCursorVisible(window: ComposeWindow, visible: Boolean) {
    }

    companion object : WindowUtils by (when (me.him188.ani.utils.platform.currentPlatformDesktop()) {
        is Platform.MacOS -> MacosWindowUtils()
        is Platform.Windows -> WindowsWindowUtils()
    })
}

abstract class AwtWindowUtils : WindowUtils {
    companion object {
        val blankCursor: Cursor by lazy {
            Toolkit.getDefaultToolkit().createCustomCursor(
                BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), Point(0, 0), "blank cursor",
            )
        }
    }

    override fun isCursorVisible(window: ComposeWindow): Boolean = window.cursor != blankCursor

    override fun setCursorVisible(window: ComposeWindow, visible: Boolean) {
        val cursor = if (visible) Cursor.getDefaultCursor() else blankCursor
        window.cursor = cursor
        window.contentPane.cursor = cursor
    }
}

fun ComposeWindow.setTitleBarColor(color: Color) {
    WindowUtils.setTitleBarColor(windowHandle, color)
}

