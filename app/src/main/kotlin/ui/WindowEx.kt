package me.him188.animationgarden.desktop.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.*
import androidx.compose.ui.window.*
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame

/**
 * @see Window
 */
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@Composable
fun WindowEx(
    onCloseRequest: () -> Unit,
    state: WindowState = rememberWindowState(),
    visible: Boolean = true,
    title: String = "Untitled",
    icon: Painter? = null,
    undecorated: Boolean = false,
    transparent: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
    minimumSize: Size = Size.Unspecified, // added
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    content: @Composable FrameWindowScope.() -> Unit
) {
    val currentState by rememberUpdatedState(state)
    val currentTitle by rememberUpdatedState(title)
    val currentIcon by rememberUpdatedState(icon)
    val currentUndecorated by rememberUpdatedState(undecorated)
    val currentTransparent by rememberUpdatedState(transparent)
    val currentResizable by rememberUpdatedState(resizable)
    val currentEnabled by rememberUpdatedState(enabled)
    val currentFocusable by rememberUpdatedState(focusable)
    val currentAlwaysOnTop by rememberUpdatedState(alwaysOnTop)
    val currentOnCloseRequest by rememberUpdatedState(onCloseRequest)
    val currentMinimumSize by rememberUpdatedState(minimumSize)

    val updater = remember {
        ComponentUpdater()
    }

    // the state applied to the window. exist to avoid races between WindowState changes and the state stored inside the native window
    val appliedState = remember {
        object {
            var size: DpSize? = null
            var position: WindowPosition? = null
            var placement: WindowPlacement? = null
            var isMinimized: Boolean? = null
        }
    }

    Window(
        visible = visible,
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
        create = {
            ComposeWindow().apply {
                // close state is controlled by WindowState.isOpen
                defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
                addWindowListener(object : WindowAdapter() {
                    override fun windowClosing(e: WindowEvent) {
                        currentOnCloseRequest()
                    }
                })
                addWindowStateListener {
                    currentState.placement = placement
                    currentState.isMinimized = isMinimized
                    appliedState.placement = currentState.placement
                    appliedState.isMinimized = currentState.isMinimized
                }
                addComponentListener(object : ComponentAdapter() {
                    override fun componentResized(e: ComponentEvent) {
                        // we check placement here and in windowStateChanged,
                        // because fullscreen changing doesn't
                        // fire windowStateChanged, only componentResized
                        currentState.placement = placement
                        currentState.size = DpSize(width.dp, height.dp)
                        appliedState.placement = currentState.placement
                        appliedState.size = currentState.size
                    }

                    override fun componentMoved(e: ComponentEvent) {
                        currentState.position = WindowPosition(x.dp, y.dp)
                        appliedState.position = currentState.position
                    }
                })
            }
        },
        dispose = ComposeWindow::dispose,
        update = { window ->
            updater.update {
                set(currentTitle, window::setTitle)
                set(currentIcon, window::setIcon)
                set(currentUndecorated, window::setUndecoratedSafely)
                set(currentTransparent, window::isTransparent::set)
                set(currentResizable, window::setResizable)
                set(currentEnabled, window::setEnabled)
                set(currentFocusable, window::setFocusableWindowState)
                set(currentAlwaysOnTop, window::setAlwaysOnTop)
                set(currentMinimumSize) { // added
                    window.minimumSize = Dimension(it.width.toInt(), it.height.toInt())
                }
            }
            if (state.size != appliedState.size) {
                window.setSizeSafely(state.size)
                appliedState.size = state.size
            }
            if (state.position != appliedState.position) {
                window.setPositionSafely(state.position)
                appliedState.position = state.position
            }
            if (state.placement != appliedState.placement) {
                window.placement = state.placement
                appliedState.placement = state.placement
            }
            if (state.isMinimized != appliedState.isMinimized) {
                window.isMinimized = state.isMinimized
                appliedState.isMinimized = state.isMinimized
            }
        },
        content = content
    )
}
