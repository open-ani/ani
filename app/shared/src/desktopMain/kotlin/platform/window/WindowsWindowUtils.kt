package me.him188.ani.app.platform.window

import androidx.annotation.Keep
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.W32Errors
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.DWORD
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.platform.win32.WinUser.MONITORINFO
import com.sun.jna.platform.win32.WinUser.SWP_FRAMECHANGED
import com.sun.jna.platform.win32.WinUser.SWP_NOZORDER
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions
import me.him188.ani.app.platform.window.ExtendedUser32.Companion.MONITOR_DEFAULTTONEAREST
import me.him188.ani.app.platform.window.ExtendedUser32.Companion.SWP_NOACTIVATE
import me.him188.ani.app.platform.window.ExtendedUser32.Companion.WS_EX_CLIENTEDGE
import me.him188.ani.app.platform.window.ExtendedUser32.Companion.WS_EX_DLGMODALFRAME
import me.him188.ani.app.platform.window.ExtendedUser32.Companion.WS_EX_STATICEDGE
import me.him188.ani.app.platform.window.ExtendedUser32.Companion.WS_EX_WINDOWEDGE
import kotlin.math.roundToInt

class WindowsWindowUtils : AwtWindowUtils() {
    private val dwmAPi: Dwmapi = Native.load("dwmapi", Dwmapi::class.java, W32APIOptions.DEFAULT_OPTIONS)
    private val extendedUser32: ExtendedUser32 =
        Native.load("user32", ExtendedUser32::class.java, W32APIOptions.DEFAULT_OPTIONS)


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
            WinUser.SWP_NOMOVE or WinUser.SWP_NOSIZE or SWP_NOZORDER or SWP_FRAMECHANGED,
        )
    }

    // https://learn.microsoft.com/en-us/windows/win32/winmsg/extended-window-styles
    override fun setUndecoratedFullscreen(window: PlatformWindow, undecorated: Boolean) {
        // copied from vlcj
        // uk.co.caprica.vlcj.player.embedded.fullscreen.windows.Win32FullScreenHandler

        val hwnd = HWND(Pointer.createConstant(window.windowHandle))
        if (undecorated) {
            // Remove window borders and title bar
            val currentStyle = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_STYLE)
            if (currentStyle and WinUser.WS_CAPTION == 0) {
                // 目前没有标题, 说明已经是全屏了
                return
            }

            // 保存原始窗口状态
            window.savedWindowState = SavedWindowState(
                style = currentStyle,
                exStyle = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE),
                rect = WinDef.RECT().apply {
                    User32.INSTANCE.GetWindowRect(hwnd, this)
                }.toComposeRect(),
                maximized = extendedUser32.IsZoomed(hwnd),
            )

            User32.INSTANCE.SetWindowLong(
                hwnd, WinUser.GWL_STYLE,
                currentStyle and (WinUser.WS_CAPTION or WinUser.WS_THICKFRAME).inv(),
            )

            // Remove extended window styles
            User32.INSTANCE.SetWindowLong(
                hwnd, WinUser.GWL_EXSTYLE,
                User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE)
                    .and(WS_EX_DLGMODALFRAME or WS_EX_WINDOWEDGE or WS_EX_CLIENTEDGE or WS_EX_STATICEDGE),
            )

            val rect = getMonitorInfo(hwnd).rcMonitor!!

            extendedUser32.SetWindowPos(
                hwnd,
                null,
                rect.left,
                rect.top,
                rect.right - rect.left,
                rect.bottom - rect.top,
                SWP_NOZORDER or SWP_NOACTIVATE or SWP_FRAMECHANGED,
            )
            window.isUndecoratedFullscreen = true
        } else {
            // Restore window borders and title bar
            val style = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_STYLE)
            if (style and WinUser.WS_CAPTION != 0) {
                // 目前有标题, 说明不是全屏
                return
            }

            val savedWindowState = checkNotNull(window.savedWindowState) {
                "window.savedWindowState is null, cannot restore window state"
            }
            User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_STYLE, savedWindowState.style)
            User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, savedWindowState.exStyle)
            savedWindowState.rect.run {
                User32.INSTANCE.SetWindowPos(
                    hwnd, null,
                    left.roundToInt(), top.roundToInt(), (right - left).roundToInt(), bottom.roundToInt(),
                    SWP_NOZORDER or SWP_NOACTIVATE or SWP_FRAMECHANGED,
                )
            }
            if (savedWindowState.maximized) {
                User32.INSTANCE.SendMessage(
                    hwnd,
                    User32.WM_SYSCOMMAND,
                    WinDef.WPARAM(WinUser.SC_MAXIMIZE.toLong()),
                    WinDef.LPARAM(0),
                )
            }
            window.savedWindowState = null
            window.isUndecoratedFullscreen = false
        }
    }

    private fun getMonitorInfo(hwnd: HWND): MONITORINFO {
        return MONITORINFO().apply {
            extendedUser32.GetMonitorInfoA(
                extendedUser32.MonitorFromWindow(hwnd, MONITOR_DEFAULTTONEAREST),
                this,
            )
        }
    }

    override fun setPreventScreenSaver(prevent: Boolean) {
        if (prevent) {
            Kernel32.INSTANCE.SetThreadExecutionState(Kernel32.ES_CONTINUOUS or Kernel32.ES_SYSTEM_REQUIRED or Kernel32.ES_DISPLAY_REQUIRED)
        } else {
            Kernel32.INSTANCE.SetThreadExecutionState(Kernel32.ES_CONTINUOUS)
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

internal interface ExtendedUser32 : User32 {
    /**
     * Is the window zoomed (maximised) or not?
     *
     * @param hWnd native window handle
     * @return `true` if the window is zoomed; `false` if it is not
     */
    fun IsZoomed(hWnd: HWND?): Boolean

    /**
     * Get a native monitor handle from a window handle.
     *
     * @param hWnd native window handle
     * @param dwFlags flags
     * @return native monitor handle
     */
    fun MonitorFromWindow(hWnd: HWND?, dwFlags: DWORD?): Pointer?

    /**
     * Get native monitor information.
     *
     * @param hMonitor native monitor handle
     * @param lpMonitorInfo structure to receive monitor information
     * @return `true` on success; `false` otherwise
     */
    fun GetMonitorInfoA(hMonitor: Pointer?, lpMonitorInfo: WinUser.MONITORINFO?): Boolean

    /**
     * Send a message to a native window.
     *
     * @param hWnd native window handle
     * @param Msg message identifier
     * @param wParam message parameter
     * @param lParam message parameter
     * @return result
     */
    override fun SendMessage(hWnd: HWND, Msg: Int, wParam: WinDef.WPARAM, lParam: WinDef.LPARAM): WinDef.LRESULT

    companion object {
        const val SC_RESTORE: Int = 0x0000f120

        const val WS_THICKFRAME: Int = 0x00040000
        const val WS_CAPTION: Int = 0x00c00000

        const val WS_EX_DLGMODALFRAME: Int = 0x00000001
        const val WS_EX_WINDOWEDGE: Int = 0x00000100
        const val WS_EX_CLIENTEDGE: Int = 0x00000200
        const val WS_EX_STATICEDGE: Int = 0x00020000

        const val SWP_NOZORDER: Int = 0x0004
        const val SWP_NOACTIVATE: Int = 0x0010
        const val SWP_FRAMECHANGED: Int = 0x0020

        val MONITOR_DEFAULTTONEAREST: DWORD = DWORD(2)
    }
}
