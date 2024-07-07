package me.him188.ani.app.ui.settings.tabs.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.files
import me.him188.ani.app.platform.isAndroid
import me.him188.ani.app.ui.foundation.widgets.RichDialogLayout
import me.him188.ani.app.ui.settings.framework.components.SettingsScope

@Composable
internal fun SettingsScope.TorrentEngineGroup(vm: MediaSettingsViewModel) {
    if (Platform.currentPlatform.isAndroid()) {
        return // 安卓不需要设置, 安卓必须启用 libtorrent, 而 qBit 目前只支持本地, 安卓手机即使连接到 PC 的 qBit 也没用
    }
}

@Composable
internal fun AniQBHelpLayout(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    RichDialogLayout(
        title = { Text("Ani 会如何使用我的 qBittorrent？") },
        buttons = {
            Button(onClick = onDismissRequest) {
                Text("关闭")
            }
        },
        modifier,
    ) {
        val context = LocalContext.current
        ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
            Text(
                """
                Ani 会在播放或缓存视频时，创建一个 qBittorrent 任务。该任务会在 "Ani" 分类中，可单独管理。
                资源的下载目录为 Ani 的缓存目录，即：
                ${context.files.cacheDir}
                
                在删除缓存时，Ani 会删除该任务，以及任务对应的文件。
                在线播放的视频数据不会立即删除，会在每次启动 Ani 时统一删除。
                
                Ani 不会操作你自己在 qBittorrent 创建的其他任务。
                
                如有其他疑问，请加群询问或在 GitHub 创建一个 issue。
                """.trimIndent(),
            )
        }
    }
}

@Composable
internal fun WebUIHelpLayout(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    RichDialogLayout(
        title = { Text("如何开启 Web UI") },
        buttons = {
            Button(onClick = onDismissRequest) {
                Text("关闭")
            }
        },
        modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("1. 打开 qBittorrent 或 qBittorrent Enhanced 设置")
            Text("2. 在左侧选择 Web UI")
            Text("3. 勾选 \"Web 用户界面 (远程控制)\"")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("4. 设置一个 \"IP 地址\"，可以使用默认的 ")
                InlineCodeText {
                    Text("*")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("5. 设置一个 \"端口\"，可以使用默认的 ")
                InlineCodeText {
                    Text("8080")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("6. 设置一个用户名和密码")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("7. 点击右下角 \"确定\"")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("8. 关闭本页面，正确填写连接地址、用户名和密码")
            }
        }
    }
}

@Composable
private fun InlineCodeText(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            Modifier.padding(vertical = 0.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}