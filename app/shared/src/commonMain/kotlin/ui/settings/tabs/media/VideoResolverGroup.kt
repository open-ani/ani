package me.him188.ani.app.ui.settings.tabs.media

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.him188.ani.app.data.persistent.preference.WebViewDriver
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.isDesktop
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.settings.framework.components.DropdownItem
import me.him188.ani.app.ui.settings.framework.components.SettingsScope

@Composable
internal fun SettingsScope.VideoResolverGroup(vm: MediaSettingsViewModel) {
    // There are not many options for the player.
    if (!Platform.currentPlatform.isDesktop()) {
        return
    }

    VPGroup(vm)
}

@Composable
private fun SettingsScope.VPGroup(vm: MediaSettingsViewModel) {
    val config by vm.videoResolverSettings
    val driver by remember {
        derivedStateOf {
            config.driver
        }
    }

    Group(
        title = {
            Text("视频解析")
        },
    ) {
        val itemText: @Composable (WebViewDriver) -> Unit = {
            when (it) {
                WebViewDriver.CHROME -> Text("Chrome")
                WebViewDriver.EDGE -> Text("Edge浏览器")
                WebViewDriver.AUTO -> Text("自动选择")
            }
        }
        DropdownItem(
            selected = { driver },
            values = { WebViewDriver.enabledEntries },
            itemText = itemText,
            exposedItemText = itemText,
            onSelect = {
                vm.videoResolverSettings.update(
                    config.copy(
                        driver = it,
                    ),
                )
            },
            modifier = Modifier.placeholder(vm.videoResolverSettings.loading),
//            itemIcon = { WebViewDriverIcon(it) },
            title = { Text("浏览器引擎") },
            description = { Text("播放部分视频源时需要使用无头浏览器引擎，请在电脑上安装 Chrome 或 Edge 浏览器，Safari 不支持") },
        )
    }
}

// TODO: More accurate icons
//@Composable
//private fun WebViewDriverIcon(driver: WebViewDriver) {
////    when (driver) {
////        else -> Icon(Icons.Rounded.TravelExplore, driver.toString())
////    }
//    Icon(Icons.Rounded.TravelExplore, driver.toString())
//}