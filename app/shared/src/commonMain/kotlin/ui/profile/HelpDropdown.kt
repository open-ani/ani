package me.him188.ani.app.ui.profile

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.LocalContext
import org.koin.core.context.GlobalContext

@Composable
fun HelpDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    DropdownMenu(expanded, onDismissRequest, modifier) {
        DropdownMenuItem(
            text = { Text("加入 QQ 群 927170241") },
            onClick = {
                GlobalContext.get().get<BrowserNavigator>().openJoinGroup(context)
            }
        )
        DropdownMenuItem(
            text = { Text("加入 Telegram 群 openani") },
            onClick = {
                GlobalContext.get().get<BrowserNavigator>().openJoinTelegram(context)
            }
        )
        DropdownMenuItem(
            text = { Text("GitHub 开源仓库") },
            onClick = { GlobalContext.get().get<BrowserNavigator>().openBrowser(context, ISSUE_TRACKER) }
        )
        DropdownMenuItem(
            text = { Text("反馈问题") },
            onClick = { GlobalContext.get().get<BrowserNavigator>().openBrowser(context, GITHUB_HOME) }
        )
        DropdownMenuItem(
            text = { Text("Ani 官网") },
            onClick = { GlobalContext.get().get<BrowserNavigator>().openBrowser(context, ANI_WEBSITE) }
        )
    }
}