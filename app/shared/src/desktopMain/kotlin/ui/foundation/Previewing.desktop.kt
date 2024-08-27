package me.him188.ani.app.ui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import me.him188.ani.app.platform.DesktopContext
import me.him188.ani.app.platform.ExtraWindowProperties
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.window.LocalPlatformWindow
import me.him188.ani.app.platform.window.PlatformWindow
import java.io.File

@Composable
actual fun PlatformPreviewCompositionLocalProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalContext provides remember {
            DesktopContext(
                WindowState(size = DpSize(1920.dp, 1080.dp)),
                File("."),
                File("."),
                File("./logs"),
                ExtraWindowProperties(false),
            )
        },
        LocalPlatformWindow provides remember {
            PlatformWindow(0L)
        },
    ) {
        content()
    }
}
