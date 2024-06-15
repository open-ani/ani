package me.him188.ani.app.ui.update

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.tools.update.UpdateInstaller
import me.him188.ani.app.ui.foundation.widgets.RichDialogLayout
import org.koin.core.context.GlobalContext

@Composable
fun FailedToInstallDialog(
    onDismissRequest: () -> Unit,
    logoState: () -> UpdateLogoState,
) {
    val context = LocalContext.current
    BasicAlertDialog(onDismissRequest) {
        RichDialogLayout(
            title = { Text("自动安装失败") },
            buttons = {
                TextButton(onDismissRequest) { Text("取消更新") }
                Button(
                    onClick = {
                        (logoState() as? UpdateLogoState.Downloaded)?.file?.let {
                            GlobalContext.get().get<UpdateInstaller>().openForManualInstallation(it, context)
                        }
                    }
                ) { Text("查看安装包") }
            }
        ) {
            Text("自动安装失败, 请手动安装")
        }
    }
}
