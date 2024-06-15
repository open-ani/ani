package me.him188.ani.app.ui.settings.tabs.media

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.settings.framework.components.RowButtonItem
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.SliderItem
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import kotlin.math.roundToInt

@Composable
internal fun SettingsScope.AutoCacheGroup(
    vm: MediaSettingsViewModel,
    navigator: AniNavigator
) {
    Group(
        title = { Text("自动缓存") },
        description = { Text("自动缓存 \"在看\" 分类中未观看的剧集") },
    ) {
        val mediaCacheSettings by vm.mediaCacheSettings
        SwitchItem(
            checked = mediaCacheSettings.enabled,
            onCheckedChange = {
                vm.updateMediaCacheSettings(mediaCacheSettings.copy(enabled = it))
            },
            title = { Text("启用自动缓存") },
            description = { Text("启用后下面的设置才有效") },
            modifier = Modifier.placeholder(vm.mediaCacheSettings.loading)
        )

        HorizontalDividerItem()

        var maxCount by remember(mediaCacheSettings) { mutableFloatStateOf(mediaCacheSettings.maxCountPerSubject.toFloat()) }
        SliderItem(
            title = { Text("最大自动缓存话数") },
            description = {
                Column {
                    Text("若手动缓存数量超过该设置值，将不会自动缓存")
                    Row {
                        Text(remember(maxCount) { autoCacheDescription(maxCount) })
                        if (maxCount == 10f) {
                            Text("可能会占用大量空间", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
        ) {
            Slider(
                value = maxCount,
                onValueChange = { maxCount = it },
                Modifier.placeholder(vm.mediaCacheSettings.loading),
                valueRange = 0f..10f,
                onValueChangeFinished = {
                    vm.updateMediaCacheSettings(mediaCacheSettings.copy(maxCountPerSubject = maxCount.roundToInt()))
                },
                steps = 9,
            )
        }

        HorizontalDividerItem()

        var mostRecentOnly by remember(mediaCacheSettings) {
            mutableStateOf(mediaCacheSettings.mostRecentOnly)
        } // for preview
        SwitchItem(
            checked = mostRecentOnly,
            onCheckedChange = {
                mostRecentOnly = it
                vm.updateMediaCacheSettings(mediaCacheSettings.copy(mostRecentOnly = it))
            },
            title = { Text("仅缓存最近看过的番剧") },
            Modifier.placeholder(vm.mediaCacheSettings.loading),
//            description = {
//                if (!mostRecentOnly) {
//                    Text("当前设置: 总是缓存 \"在看\" 分类中的全部番剧")
//                }
//            },
        )

        AnimatedVisibility(mostRecentOnly) {
            SubGroup {
                var mostRecentCount by remember(mediaCacheSettings) { mutableFloatStateOf(mediaCacheSettings.mostRecentCount.toFloat()) }
                SliderItem(
                    title = { Text("缓存数量") },
                    description = {
                        Text("当前设置: 仅缓存最近看过的 ${mostRecentCount.roundToInt()} 部番剧")
                    },
                ) {
                    Slider(
                        value = mostRecentCount,
                        onValueChange = { mostRecentCount = it },
                        Modifier.placeholder(vm.mediaCacheSettings.loading),
                        onValueChangeFinished = {
                            vm.updateMediaCacheSettings(mediaCacheSettings.copy(mostRecentCount = mostRecentCount.roundToInt()))
                        },
                        valueRange = 0f..30f,
                        steps = 30 - 1,
                    )
                }
            }
        }

        HorizontalDividerItem()

        RowButtonItem(
            onClick = { navigator.navigateCaches() },
            icon = { Icon(Icons.Rounded.ArrowOutward, null) },
        ) { Text("管理已缓存的剧集") }
    }
}

