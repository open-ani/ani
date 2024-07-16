package me.him188.ani.app.ui.settings.tabs.media

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import me.him188.ani.app.desktop.projectDirectories
import me.him188.ani.app.desktop.torrentCacheDir
import me.him188.ani.app.ui.settings.framework.components.RowButtonItem
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.TextFieldItem
import java.awt.Desktop
import java.io.File

@Composable
actual fun SettingsScope.CacheDirectoryGroup(vm: MediaSettingsViewModel) {
    Group({ Text("存储设置") }) {
        val mediaCacheSettings by vm.mediaCacheSettings
        TextFieldItem(
            mediaCacheSettings.saveDir ?: remember { projectDirectories.torrentCacheDir.absolutePath },
            title = { Text("BT 视频缓存位置") },
            textFieldDescription = {
                Text("修改后不会自动迁移数据，也不会自动删除旧数据。\n如需删除旧数据，请在修改之前点击 \"打开 BT 缓存目录\" 并删除该目录下的所有文件。\n\n重启生效")
            },
            onValueChangeCompleted = {
                vm.updateMediaCacheSettings(mediaCacheSettings.copy(saveDir = it))
            },
        )
        RowButtonItem(
            title = { Text("打开 BT 缓存目录") },
            icon = { Icon(Icons.Rounded.ArrowOutward, null) },
            onClick = {
                Desktop.getDesktop().open(
                    File(mediaCacheSettings.saveDir ?: projectDirectories.torrentCacheDir.absolutePath),
                )
            },
        )
    }
}
