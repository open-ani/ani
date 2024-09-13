package me.him188.ani.app.ui.settings.tabs

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.ContextMP
import me.him188.ani.app.platform.LocalContext
import org.koin.mp.KoinPlatform

object AniHelpNavigator {
    fun openJoinQQGroup(context: ContextMP) {
        KoinPlatform.getKoin().get<BrowserNavigator>().openJoinGroup(context)
    }

    fun openTelegram(context: ContextMP) {
        KoinPlatform.getKoin().get<BrowserNavigator>().openJoinTelegram(context)
    }

    fun openIssueTracker(context: ContextMP) {
        KoinPlatform.getKoin().get<BrowserNavigator>().openBrowser(context, ISSUE_TRACKER)
    }

    fun openGitHubHome(context: ContextMP) {
        KoinPlatform.getKoin().get<BrowserNavigator>().openBrowser(context, GITHUB_HOME)
    }

    fun openAniWebsite(context: ContextMP) {
        KoinPlatform.getKoin().get<BrowserNavigator>().openBrowser(context, ANI_WEBSITE)
    }
}

@Composable
fun HelpDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    DropdownMenu(expanded, onDismissRequest, modifier) {
        DropdownMenuItem(
            text = { Text("QQ") },
            onClick = {
                AniHelpNavigator.openJoinQQGroup(context)
            },
        )
        DropdownMenuItem(
            text = { Text("Telegram") },
            onClick = {
                AniHelpNavigator.openTelegram(context)
            },
        )
        DropdownMenuItem(
            text = { Text("GitHub") },
            onClick = { AniHelpNavigator.openGitHubHome(context) },
        )
        DropdownMenuItem(
            text = { Text("反馈问题") },
            onClick = { AniHelpNavigator.openIssueTracker(context) },
        )
        DropdownMenuItem(
            text = { Text("Ani 官网") },
            onClick = { AniHelpNavigator.openAniWebsite(context) },
        )
    }
}
