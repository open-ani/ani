package me.him188.ani.app.ui.preference.tabs.media

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.isAndroid
import me.him188.ani.app.ui.preference.PreferenceScope
import me.him188.ani.app.ui.preference.framework.MediaSourceTesterView
import me.him188.ani.utils.ktor.ClientProxyConfigValidator

@Composable
internal fun PreferenceScope.TorrentEngineGroup(vm: MediaPreferenceViewModel) {
    if (Platform.currentPlatform.isAndroid()) {
        return // 安卓不需要设置, 安卓必须启用 libtorrent, 而 qBit 目前只支持本地, 安卓手机即使连接到 PC 的 qBit 也没用
    }

    QBGroup(vm)
}

@Composable
private fun PreferenceScope.QBGroup(vm: MediaPreferenceViewModel) {
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
                Text("目前 ${Platform.currentPlatform.name} 只支持使用 qBittorrent")
            }
        }
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

        var url by remember(clientConfig) {
            mutableStateOf(clientConfig.baseUrl)
        }
        TextFieldItem(
            url,
            { url = it.trim() },
            title = { Text("Web UI 连接地址") },
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
            onValueChangeCompleted = {
                vm.qBittorrentConfig.update(
                    config.copy(
                        clientConfig = clientConfig.copy(baseUrl = url)
                    )
                )
            },
            isErrorProvider = {
                !ClientProxyConfigValidator.isValidProxy(url, allowSocks = false)
            }
        )

        HorizontalDividerItem()

        var username by remember(clientConfig) {
            mutableStateOf(clientConfig.username ?: "")
        }

        TextFieldItem(
            username,
            { username = it },
            title = { Text("用户名") },
            description = { Text("可选") },
            placeholder = { Text("无") },
            onValueChangeCompleted = {
                vm.qBittorrentConfig.update(
                    config.copy(
                        clientConfig = clientConfig.copy(username = username)
                    )
                )
            }
        )

        HorizontalDividerItem()

        var password by remember(clientConfig) {
            mutableStateOf(clientConfig.password ?: "")
        }

        TextFieldItem(
            password,
            { password = it },
            title = { Text("密码") },
            description = { Text("可选") },
            placeholder = { Text("无") },
            onValueChangeCompleted = {
                vm.qBittorrentConfig.update(
                    config.copy(
                        clientConfig = clientConfig.copy(password = password)
                    )
                )
            }
        )

        HorizontalDividerItem()

        var category by remember(clientConfig) {
            mutableStateOf(clientConfig.category)
        }

        TextFieldItem(
            category,
            { category = it },
            title = { Text("分类") },
            description = { Text("创建的资源将会添加到该分类内，方便管理") },
            placeholder = { Text("") },
            onValueChangeCompleted = {
                vm.qBittorrentConfig.update(
                    config.copy(
                        clientConfig = clientConfig.copy(password = category)
                    )
                )
            }
        )

        HorizontalDividerItem()

        MediaSourceTesterView(
            vm.qBitTester.tester,
            icon = {
                Icon(
                    Icons.Rounded.Bolt, null,
                    tint = MaterialTheme.colorScheme.onSurface
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

