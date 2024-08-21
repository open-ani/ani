package me.him188.ani.app.ui.settings.tabs.media

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.him188.ani.app.data.models.preference.VideoPlayerCore
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.isAndroid
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.settings.framework.components.DropdownItem
import me.him188.ani.app.ui.settings.framework.components.SettingsScope

@Composable
internal fun SettingsScope.VideoPlayerSelectionGroup(vm: MediaSettingsViewModel) {
    // There are not many options for the player.
    if (!Platform.currentPlatform.isAndroid()) {
        return
    }

    VPGroup(vm)
}

@Composable
private fun SettingsScope.VPGroup(vm: MediaSettingsViewModel) {
    val config by vm.videoPlayerSelectionSettings
    val core by remember {
        derivedStateOf {
            config.core
        }
    }

    Group(
        title = {
            Text("视频解析")
        },
    ) {
        val itemText: @Composable (VideoPlayerCore) -> Unit = {
            when (it) {
                VideoPlayerCore.VLC_ANDROID -> Text("VLC Android")
                VideoPlayerCore.EXO -> Text("Exo")
            }
        }
        DropdownItem(
            selected = { core },
            values = { VideoPlayerCore.enabledEntries },
            itemText = itemText,
            exposedItemText = itemText,
            onSelect = {
                vm.videoPlayerSelectionSettings.update(
                    config.copy(
                        core = it,
                    ),
                )
            },
            modifier = Modifier.placeholder(vm.videoPlayerSelectionSettings.loading),
//            itemIcon = { WebViewDriverIcon(it) },
            title = { Text("切换播放器内核") },
            description = { Text("VLC Android内核支持的视频格式更丰富") },
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