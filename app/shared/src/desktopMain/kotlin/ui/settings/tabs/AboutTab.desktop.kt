package me.him188.ani.app.ui.settings.tabs

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.him188.ani.app.platform.DesktopContext
import me.him188.ani.app.platform.LocalContext
import java.awt.Desktop

@Composable
internal actual fun ColumnScope.PlatformDebugInfoItems() {
    val context = LocalContext.current
    FilledTonalButton(
        {
            Desktop.getDesktop().open((context as DesktopContext).logsDir)
//        below also works on macOS, not sure about Windows
//        KoinPlatform.getKoin().get<BrowserNavigator>()
//            .openBrowser(context, "file://" + (context as DesktopContext).logsDir.absolutePath.replace(" ", "%20"))
        },
    ) {
        Text("打开日志目录")
    }
}
