package me.him188.ani.app.ui.settings.tabs.media

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.SliderItem
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.Unspecified
import me.him188.ani.datasources.api.topic.FileSize.Companion.kiloBytes
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.utils.platform.format1f

@Composable
internal fun SettingsScope.TorrentEngineGroup(vm: MediaSettingsViewModel) {
    Group({ Text("BT 设置") }) {
        val torrentSettings by vm.torrentSettings

        RateSliderItem(
            torrentSettings.downloadRateLimit,
            onValueChangeFinished = {
                vm.torrentSettings.update(torrentSettings.copy(downloadRateLimit = it))
            },
            title = { Text("下载速度限制") },
        )

        Group(
            title = { Text("分享设置") },
            useThinHeader = true,
            description = { Text("BT 网络依赖用户间分享，你所看的视频均来自其他用户的分享。允许上传，共同维护健康的 BT 分享环境。") },
        ) {
//            val allowUpload by remember {
//                derivedStateOf {
//                    torrentSettings.uploadRateLimit != FileSize.Zero
//                }
//            }
//            SwitchItem(
//                checked = allowUpload,
//                onCheckedChange = {
//                    vm.torrentSettings.update(
//                        torrentSettings.copy(
//                            uploadRateLimit = if (it) 1.megaBytes else FileSize.Zero,
//                        ),
//                    )
//                },
//                title = { Text("允许上传") },
//                description = if (!allowUpload) {
//                    {
//                        Text(
//                            "BT 网络依赖用户间分享，你所看的视频均来自其他用户的分享。" +
//                                    "除特殊情况外，建议允许上传，共同维护健康的 BT 分享环境。" +
//                                    "禁用上传会导致许多用户不再分享视频给你。",
//                            color = MaterialTheme.colorScheme.error,
//                        )
//                    }
//                } else null,
//            )
//
//            AnimatedVisibility(allowUpload) {
//            RateTextFieldItem(
//                torrentSettings.uploadRateLimit,
//                title = { Text("上传速度限制") },
//                onValueChangeCompleted = { vm.torrentSettings.update(torrentSettings.copy(uploadRateLimit = it)) },
//            )

            RateSliderItem(
                torrentSettings.uploadRateLimit,
                onValueChangeFinished = {
                    vm.torrentSettings.update(torrentSettings.copy(uploadRateLimit = it))
                },
                title = { Text("上传速度限制") },
            )
        }
    }
}

@Composable
private fun SettingsScope.RateSliderItem(
    value: FileSize,
    onValueChangeFinished: (value: FileSize) -> Unit,
    title: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    var editingValue by remember(value) {
        mutableFloatStateOf(if (value == Unspecified) 10f else value.inMegaBytes)
    }
    SliderItem(
        if (editingValue == -1f) 10f else editingValue,
        onValueChange = { editingValue = it },
        title = title,
        valueRange = 1f..10f,
        steps = 0,
        onValueChangeFinished = {
            onValueChangeFinished(
                if (editingValue == 10f) Unspecified
                else editingValue.megaBytes,
            )
        },
        modifier = modifier,
        valueLabel = {
            Text(
                if (editingValue == 10f) "无限制"
                else "${String.format1f(editingValue)} MB/s",
            )
        },
    )
}

//@Composable
//private fun SettingsScope.RateTextFieldItem(
//    value: FileSize,
//    title: @Composable () -> Unit,
//    onValueChangeCompleted: (value: FileSize) -> Unit,
//    minValue: FileSize = FileSize.Zero,
//) {
//    TextFieldItem(
//        value.inKiloBytes.toString(),
//        title = title,
//        onValueChangeCompleted = { onValueChangeCompleted(it.toDoubleOrNull()?.kiloBytes ?: Unspecified) },
//        isErrorProvider = {
//            val double = it.toDoubleOrNull()
//            double == null || double.kiloBytes.inBytes < minValue.inBytes
//        },
//        sanitizeValue = { it.trim() },
//        textFieldDescription = { Text("单位为 KB/s，最低 1024 KB/s，-1 表示无限制\n\n当前设置: ${renderRateValue(it)}") },
//        exposedItem = { text ->
//            Text(renderRateValue(text))
//        },
//    )
//}

@Composable
private fun renderRateValue(text: String): String {
    val toLongOrNull = text.toLongOrNull()
    val size = toLongOrNull?.kiloBytes ?: Unspecified
    return if (toLongOrNull == -1L || size == Unspecified) {
        "无限制"
    } else {
        "$size/s"
    }
}
