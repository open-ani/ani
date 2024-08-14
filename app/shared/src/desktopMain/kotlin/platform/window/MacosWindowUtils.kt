package me.him188.ani.app.platform.window

import androidx.compose.ui.awt.ComposeWindow
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Platform

internal class MacosWindowUtils : AwtWindowUtils() {
    @Suppress("FunctionName")
    interface Quartz : Library {
        // Function to hide the cursor
        fun CGDisplayHideCursor(displayID: Int)

        // Function to show the cursor
        fun CGDisplayShowCursor(displayID: Int)

        // Function to check if the cursor is visible
        fun CGCursorIsVisible(): Boolean

        companion object {
            val INSTANCE: Quartz = Native.load(if (Platform.isMac()) "Quartz" else "c", Quartz::class.java)
        }
    }

    override fun isCursorVisible(window: ComposeWindow): Boolean {
        return Quartz.INSTANCE.CGCursorIsVisible()
    }

    override fun setCursorVisible(window: ComposeWindow, visible: Boolean) {
        Quartz.INSTANCE.apply {
            if (visible) {
                CGDisplayShowCursor(0)
            } else {
                CGDisplayHideCursor(0)
            }
        }
    }
}