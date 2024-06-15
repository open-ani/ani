package me.him188.ani.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.widgets.RichDialogLayout

/**
 * 类似 Dialog, 用于请求 Bangumi OAuth 授权.
 *
 * - On Android: An embedded WebView
 * - On Desktop: A http server and opens a browser window
 */
@Composable
expect fun BangumiOAuthRequest(
    vm: AuthViewModel,
    modifier: Modifier = Modifier,
)

@Composable
fun BangumiAuthIntroLayout(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    RichDialogLayout(
        title = { Text("登录 Bangumi") },
        buttons = {
            var showHelp by remember { mutableStateOf(false) }
            TextButton({ showHelp = true }) {
                Text("获取帮助")
            }
            HelpDropdown(
                showHelp,
                onDismissRequest = { showHelp = false },
            )

            Button(onDismissRequest) {
                Text("继续")
            }
        },
        modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("欢迎使用 Ani", style = MaterialTheme.typography.titleMedium)

            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                Text("""Ani 本身不提供资源保存和下载等服务。Ani 只使用 Bangumi 的番剧条目数据，搜索互联网上的番剧资源，本质上为一个整合了进度管理、视频播放、弹幕、互联网资源搜索引擎等功能的工具。""")
                Text("""Bangumi 番组计划 是一个中文ACGN互联网分享与交流项目。该网站只是一个番剧交流平台，不提供资源下载。""")
                Text("""需要登录到 Bangumi 才能使用 Ani。""")
            }

            Text("Bangumi 注册帮助", style = MaterialTheme.typography.titleMedium)

            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                Text("""· 注册时建议使用常见邮箱，例如 QQ, 网易, Outlook""")
                Text("""· 如果提示激活失败，尝试删除激活码的最后一个字再手动输入""")
                Text("""· 如果有其他问题，可加群获取帮助或在 GitHub 上提交 issue""")
            }
        }
    }
}
       