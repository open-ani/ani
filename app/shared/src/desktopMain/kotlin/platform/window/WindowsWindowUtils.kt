package me.him188.ani.app.platform.window

import androidx.annotation.Keep
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.W32Errors
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.DWORD
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions

class WindowsWindowUtils : WindowUtils {
    private val dwmAPi: Dwmapi = Native.load("dwmapi", Dwmapi::class.java, W32APIOptions.DEFAULT_OPTIONS)

    override fun setTitleBarColor(hwnd: Long, color: Color): Boolean {
        return setTitleBarColor(HWND(Pointer.createConstant(hwnd)), argbToRgb(color.toArgb()))
    }

    private fun argbToRgb(argb: Int): Int {
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF
        return (r shl 16) or (g shl 8) or b
    }

    private fun setTitleBarColor(hwnd: HWND, color: Int): Boolean {
        return kotlin.runCatching {
            val colorRef = IntByReference(color)
            W32Errors.SUCCEEDED(
                dwmAPi.DwmSetWindowAttribute(hwnd, Dwmapi.DWMWA_CAPTION_COLOR, colorRef.pointer, DWORD.SIZE),
            )
        }.getOrElse { false }
    }

    fun hideTitleBar(hwnd: HWND?) {
        var style = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_STYLE).toLong()
        style = style and WinUser.WS_CAPTION.toLong().inv() and WinUser.WS_BORDER.toLong()
            .inv() and WinUser.WS_THICKFRAME.toLong().inv()
        User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_STYLE, style.toInt())
        User32.INSTANCE.SetWindowPos(
            hwnd, null, 0, 0, 0, 0,
            WinUser.SWP_NOMOVE or WinUser.SWP_NOSIZE or WinUser.SWP_NOZORDER or WinUser.SWP_FRAMECHANGED,
        )
    }

    // https://learn.microsoft.com/en-us/windows/win32/winmsg/extended-window-styles
    override fun setFullscreen(window: PlatformWindow, fullscreen: Boolean) {
        val hwnd = HWND(Pointer.createConstant(window.windowHandle))
        if (fullscreen) {
            // Remove window borders and title bar
            var style = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_STYLE).toLong()
            if (style and WinUser.WS_CAPTION.toUInt().toLong() == 0L) {
                // 目前没有标题, 说明已经是全屏了
                return
            }
            window.nonFullscreenStyle = style
            style = style and (WinUser.WS_CAPTION or WinUser.WS_THICKFRAME).toUInt().toLong().inv()
            User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_STYLE, style.toInt())

            // Remove extended window styles
            var exStyle = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE).toLong()
            window.nonFullscreenExtStyle = exStyle
            exStyle =
                exStyle and (0x00000001L or 0x00000100L or 0x00000200L or 0x00020000L).inv()
            User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, exStyle.toInt())

            // 保存原始窗口大小和位置
            val originalRect = WinDef.RECT()
            if (User32.INSTANCE.GetWindowRect(hwnd, originalRect)) {
                window.nonFullscreenRect = originalRect.toComposeRect()
            }

            // Get the screen dimensions
            val screenWidth = User32.INSTANCE.GetSystemMetrics(WinUser.SM_CXSCREEN)
            val screenHeight = User32.INSTANCE.GetSystemMetrics(WinUser.SM_CYSCREEN)

            // Set window to fullscreen
            User32.INSTANCE.SetWindowPos(
                hwnd,
                HWND(Pointer.createConstant(-1)),
                0,
                0,
                screenWidth,
                screenHeight,
                WinUser.SWP_NOZORDER or WinUser.SWP_FRAMECHANGED,
            )
        } else {
            // Restore window borders and title bar
            var style = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_STYLE).toLong()
            if (style and WinUser.WS_CAPTION.toUInt().toLong() != 0L) {
                // 目前有标题, 说明不是全屏
                return
            }
            style = if (window.nonFullscreenStyle != 0L) {
                window.nonFullscreenStyle
            } else {
                style or WinUser.WS_CAPTION.toUInt().toLong() or WinUser.WS_THICKFRAME.toUInt().toLong()
            }
            User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_STYLE, style.toInt())

            // Restore extended window styles
            var exStyle = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE).toLong()
            exStyle = if (window.nonFullscreenExtStyle != 0L) {
                window.nonFullscreenExtStyle
            } else {
                exStyle or 0x00000001L or 0x00000100L or 0x00000200L or 0x00020000L
            }
            User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, exStyle.toInt())

            // Restore window to its original size and position
            window.nonFullscreenRect.takeIf { it != Rect.Zero }?.let { rect ->
                val originalRect = rect.toWinDefRect()
                User32.INSTANCE.SetWindowPos(
                    hwnd,
                    HWND(Pointer.createConstant(-1)),
                    originalRect.left,
                    originalRect.top,
                    originalRect.right - originalRect.left,
                    originalRect.bottom - originalRect.top,
                    WinUser.SWP_NOZORDER or WinUser.SWP_FRAMECHANGED,
                )
            }
        }
    }
}

private fun WinDef.RECT.toComposeRect(): Rect {
    return Rect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
}

private fun Rect.toWinDefRect(): WinDef.RECT {
    val compose = this
    return WinDef.RECT().apply {
        this.left = compose.left.toInt()
        this.top = compose.top.toInt()
        this.right = compose.right.toInt()
        this.bottom = compose.bottom.toInt()
    }
}

@Keep
@Suppress("FunctionName", "SpellCheckingInspection")
internal interface Dwmapi : StdCallLibrary {
    fun DwmSetWindowAttribute(hwnd: HWND, dwAttribute: Int, pvAttribute: Pointer, cbAttribute: Int): Int

    companion object {
        const val DWMWA_USE_IMMERSIVE_DARK_MODE: Int = 20
        const val DWMWA_CAPTION_COLOR: Int = 35
    }
}
