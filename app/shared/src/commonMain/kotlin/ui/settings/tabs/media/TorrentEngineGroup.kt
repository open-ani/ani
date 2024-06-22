package me.him188.ani.app.ui.settings.tabs.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.files
import me.him188.ani.app.platform.isAndroid
import me.him188.ani.app.ui.foundation.widgets.RichDialogLayout
import me.him188.ani.app.ui.settings.framework.MediaSourceTesterView
import me.him188.ani.app.ui.settings.framework.components.RowButtonItem
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.TextButtonItem
import me.him188.ani.app.ui.settings.framework.components.TextFieldItem
import me.him188.ani.utils.ktor.ClientProxyConfigValidator

@Composable
internal fun SettingsScope.TorrentEngineGroup(vm: MediaSettingsViewModel) {
    if (Platform.currentPlatform.isAndroid()) {
        return // 安卓不需要设置, 安卓必须启用 libtorrent, 而 qBit 目前只支持本地, 安卓手机即使连接到 PC 的 qBit 也没用
    }

    QBGroup(vm)
}

@Composable
private fun SettingsScope.QBGroup(vm: MediaSettingsViewModel) {
    val config by vm.qBittorrentConfig
    val clientConfig by derivedStateOf {
        config.clientConfig
    }
    Group(
        title = {
            Text("qBittorrent")
        },
        description = {
            Column {
                Text("使用 qBittorrent 远程控制作为 BT 下载器")
                Text("目前 ${Platform.currentPlatform.name} 只支持使用 qBittorrent (以及其他兼容版本如 qBittorrent Enhanced)")
            }
        },
    ) {
        // 目前仅在 PC 显示该设置, 不允许关闭
//        SwitchItem(
//            checked = config.enabled,
//            onCheckedChange = {
//                vm.qBittorrentConfig.update(
//                    config.copy(
//                        enabled = it
//                    )
//                )
//            },
//            title = { Text("启用") },
//        )
//
//        HorizontalDividerItem()

        val url by remember {
            derivedStateOf { clientConfig.baseUrl }
        }
        TextFieldItem(
            url,
            title = { Text("Web UI 连接地址") },
            onValueChangeCompleted = {
                vm.qBittorrentConfig.update(
                    config.copy(
                        clientConfig = clientConfig.copy(baseUrl = it),
                    ),
                )
            },
            isErrorProvider = {
                !ClientProxyConfigValidator.isValidProxy(it, allowSocks = false)
            },
            sanitizeValue = { it.trim() },
            textFieldDescription = {
                Column {
                    Text("示例：http://127.0.0.1:8080")
                    Text("支持 HTTP 和 HTTPS。在 qBittorrent 中的设置: 设置 - Web UI - IP 地址与端口。")
                    Spacer(Modifier.height(8.dp))
                    Text("若连接本机 qBittorrent，只需修改示例中的端口号 8080 为 qBittorrent 设置的端口号即可。")
                    Spacer(Modifier.height(8.dp))
                    Text("注意：目前仅支持连接本机 qBittorrent。连接远程实例时可以管理但不能播放。如你希望支持远程连接，请在 GitHub 创建一个 issue。")
                }
            },
        )

        HorizontalDividerItem()

        val username by remember {
            derivedStateOf { clientConfig.username ?: "" }
        }

        TextFieldItem(
            username,
            title = { Text("用户名") },
            description = { Text("可选") },
            placeholder = { Text("无") },
            onValueChangeCompleted = {
                vm.qBittorrentConfig.update(
                    config.copy(
                        clientConfig = clientConfig.copy(username = it),
                    ),
                )
            },
        )

        HorizontalDividerItem()

        val password by remember {
            derivedStateOf { clientConfig.password ?: "" }
        }

        TextFieldItem(
            password,
            title = { Text("密码") },
            description = { Text("可选") },
            placeholder = { Text("无") },
            onValueChangeCompleted = {
                vm.qBittorrentConfig.update(
                    config.copy(
                        clientConfig = clientConfig.copy(password = it),
                    ),
                )
            },
        )

        HorizontalDividerItem()

        val category by remember {
            derivedStateOf { clientConfig.category }
        }

        TextFieldItem(
            category,
            title = { Text("分类") },
            description = { Text("创建的资源将会添加到该分类内，方便管理") },
            placeholder = { Text("") },
            onValueChangeCompleted = {
                vm.qBittorrentConfig.update(
                    config.copy(
                        clientConfig = clientConfig.copy(category = it),
                    ),
                )
            },
            sanitizeValue = { it.trim() },
        )

        HorizontalDividerItem()

        var showWebUIHelp by remember { mutableStateOf(false) }
        if (showWebUIHelp) {
            BasicAlertDialog(onDismissRequest = { showWebUIHelp = false }) {
                WebUIHelpLayout({ showWebUIHelp = false })
            }
        }
        RowButtonItem(
            onClick = { showWebUIHelp = true },
            icon = { Icon(Icons.Outlined.Info, null) },
        ) { Text("如何开启 qBittorrent 的 Web UI 功能？") }

        HorizontalDividerItem()

        var showAniHelp by remember { mutableStateOf(false) }
        if (showAniHelp) {
            BasicAlertDialog(onDismissRequest = { showAniHelp = false }) {
                AniQBHelpLayout({ showAniHelp = false })
            }
        }
        RowButtonItem(
            onClick = { showAniHelp = true },
            icon = { Icon(Icons.Outlined.Info, null) },
        ) { Text("Ani 会如何使用我的 qBittorrent？") }

        HorizontalDividerItem()

        MediaSourceTesterView(
            vm.qBitTester.tester,
            icon = {
                Icon(
                    Icons.Rounded.Bolt, null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            },
            title = {
                Text("测试连接")
            },
            showTime = false,
        )

        TextButtonItem(
            onClick = {
                vm.qBitTester.toggleTest()
            },
            title = {
                if (vm.qBitTester.anyTesting) {
                    Text("终止测试")
                } else {
                    Text("开始测试")
                }
            },
        )
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