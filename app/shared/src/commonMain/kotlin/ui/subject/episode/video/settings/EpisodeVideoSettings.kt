/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.episode.video.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.models.danmaku.DanmakuFilterConfig
import me.him188.ani.app.data.models.danmaku.DanmakuRegexFilter
import me.him188.ani.app.data.repository.DanmakuRegexFilterRepository
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.rememberDebugSettingsViewModel
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.framework.AbstractSettingsViewModel
import me.him188.ani.app.ui.settings.framework.SettingsState
import me.him188.ani.app.ui.settings.framework.components.SettingsDefaults
import me.him188.ani.app.ui.settings.framework.components.SliderItem
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import me.him188.ani.app.ui.settings.framework.components.TextItem
import me.him188.ani.danmaku.ui.DanmakuConfig
import me.him188.ani.danmaku.ui.DanmakuStyle
import me.him188.ani.utils.platform.isDesktop
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.roundToInt

@Stable
class EpisodeVideoSettingsViewModel : AbstractSettingsViewModel(), KoinComponent {
    private val settingsRepository by inject<SettingsRepository>()
    private val danmakuRegexFilterRepository by inject<DanmakuRegexFilterRepository>()

    private val danmakuConfigState: SettingsState<DanmakuConfig> =
        settingsRepository.danmakuConfig.stateInBackground(
            placeholder = DanmakuConfig.Default,
        )

    private val danmakuFilterConfigState =
        settingsRepository.danmakuFilterConfig.stateInBackground(
            DanmakuFilterConfig.Default.copy(_placeholder = -1),
        )

    val danmakuConfig: DanmakuConfig by danmakuConfigState
    val danmakuRegexFilterList: List<DanmakuRegexFilter> by danmakuRegexFilterRepository.flow.produceState(
        initialValue = emptyList(),
    )
    val danmakuFilterConfig: DanmakuFilterConfig by danmakuFilterConfigState
    val isLoading: Boolean by derivedStateOf {
        danmakuConfigState.isLoading || danmakuFilterConfigState.isLoading
    }

    fun setDanmakuConfig(config: DanmakuConfig) {
        danmakuConfigState.update(config)
    }

    fun switchDanmakuRegexFilterCompletely() {
        danmakuFilterConfigState.update(
            danmakuFilterConfig.copy(enableRegexFilter = !danmakuFilterConfig.enableRegexFilter),
        )
    }
}

@Composable
fun EpisodeVideoSettings(
    vm: EpisodeVideoSettingsViewModel,
    onManageRegexFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    return EpisodeVideoSettings(
        danmakuConfig = vm.danmakuConfig,
        setDanmakuConfig = remember(vm) {
            vm::setDanmakuConfig
        },
        modifier = modifier,
        onManageRegexFilters = onManageRegexFilters,
        enableRegexFilter = vm.danmakuFilterConfig.enableRegexFilter,
        switchDanmakuRegexFilterCompletely = vm::switchDanmakuRegexFilterCompletely,
    )
}

@Composable
fun EpisodeVideoSettings(
    danmakuConfig: DanmakuConfig,
    setDanmakuConfig: (config: DanmakuConfig) -> Unit,
    enableRegexFilter: Boolean,
    onManageRegexFilters: () -> Unit,
    switchDanmakuRegexFilterCompletely: () -> Unit,
    modifier: Modifier = Modifier,
    useThinSlider: Boolean = true
) {
    SettingsTab(modifier.verticalScroll(rememberScrollState())) {
        Column {
            Surface(Modifier.fillMaxWidth(), color = SettingsDefaults.groupBackgroundColor) {
                FlowRow(
                    Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ElevatedFilterChip(
                        selected = danmakuConfig.enableTop,
                        onClick = { setDanmakuConfig(danmakuConfig.copy(enableTop = !danmakuConfig.enableTop)) },
                        leadingIcon = {
                            if (danmakuConfig.enableTop) Icon(Icons.Rounded.Check, contentDescription = null)
                            else Icon(Icons.Rounded.Close, contentDescription = null)
                        },
                        label = { Text("顶部", maxLines = 1) },
                    )
                    ElevatedFilterChip(
                        selected = danmakuConfig.enableFloating,
                        onClick = { setDanmakuConfig(danmakuConfig.copy(enableFloating = !danmakuConfig.enableFloating)) },
                        label = { Text("滚动", maxLines = 1) },
                        leadingIcon = {
                            if (danmakuConfig.enableFloating) Icon(Icons.Rounded.Check, contentDescription = null)
                            else Icon(Icons.Rounded.Close, contentDescription = null)
                        },
                    )
                    ElevatedFilterChip(
                        selected = danmakuConfig.enableBottom,
                        onClick = { setDanmakuConfig(danmakuConfig.copy(enableBottom = !danmakuConfig.enableBottom)) },
                        label = { Text("底部", maxLines = 1) },
                        leadingIcon = {
                            if (danmakuConfig.enableBottom) Icon(Icons.Rounded.Check, contentDescription = null)
                            else Icon(Icons.Rounded.Close, contentDescription = null)
                        },
                    )
                    ElevatedFilterChip(
                        selected = danmakuConfig.enableColor,
                        onClick = {
                            setDanmakuConfig(
                                danmakuConfig.copy(enableColor = !danmakuConfig.enableColor),
                            )
                        },
                        leadingIcon = {
                            if (danmakuConfig.enableColor) Icon(Icons.Rounded.Check, contentDescription = null)
                            else Icon(Icons.Rounded.Close, contentDescription = null)
                        },
                        label = { Text("彩色", maxLines = 1) },
                    )
                }
            }
            val fontSize by remember(danmakuConfig) {
                mutableFloatStateOf(danmakuConfig.style.fontSize.value / DanmakuStyle.Default.fontSize.value)
            }
            SliderItem(
                value = fontSize,
                onValueChange = {
                    // 故意每次改都更新, 可以即时预览
                    setDanmakuConfig(
                        danmakuConfig.copy(style = danmakuConfig.style.copy(fontSize = DanmakuStyle.Default.fontSize * it)),
                    )
                },
                valueRange = 0.50f..3f,
//                steps = ((3f - 0.50f) / 0.05f).toInt() - 1,
                title = { Text("弹幕字号") },
                valueLabel = { Text(remember(fontSize) { "${(fontSize * 100).roundToInt()}%" }) },
                useThinSlider = useThinSlider,
            )

            val alpha by remember(danmakuConfig) {
                mutableFloatStateOf(danmakuConfig.style.alpha)
            }
            SliderItem(
                value = alpha,
                onValueChange = {
                    // 故意每次改都更新, 可以即时预览
                    setDanmakuConfig(
                        danmakuConfig.copy(style = danmakuConfig.style.copy(alpha = it)),
                    )
                },
                valueRange = 0f..1f,
//                steps = ((1f - 0f) / 0.05f).toInt() - 1,
                title = { Text("不透明度") },
                valueLabel = { Text(remember(alpha) { "${(alpha * 100).roundToInt()}%" }) },
                useThinSlider = useThinSlider,
            )

            val strokeWidth by remember(danmakuConfig) {
                mutableFloatStateOf(danmakuConfig.style.strokeWidth / DanmakuStyle.Default.strokeWidth)
            }
            SliderItem(
                value = strokeWidth,
                onValueChange = {
                    // 故意每次改都更新, 可以即时预览
                    setDanmakuConfig(
                        danmakuConfig.copy(style = danmakuConfig.style.copy(strokeWidth = it * DanmakuStyle.Default.strokeWidth)),
                    )
                },
                valueRange = 0f..2f,
//                steps = ((2f - 0f) / 0.1f).toInt() - 1,
                title = { Text("描边宽度") },
                valueLabel = { Text(remember(strokeWidth) { "${(strokeWidth * 100).roundToInt()}%" }) },
                useThinSlider = useThinSlider,
            )

            val fontWeight by remember(danmakuConfig) {
                derivedStateOf {
                    danmakuConfig.style.fontWeight.weight.toFloat()
                }
            }
            SliderItem(
                value = fontWeight,
                onValueChange = {
                    if (it != fontWeight) {
                        // 故意每次改都更新, 可以即时预览
                        setDanmakuConfig(
                            danmakuConfig.copy(style = danmakuConfig.style.copy(fontWeight = FontWeight(it.toInt()))),
                        )
                    }
                },
                valueRange = 100f..900f,
//                steps = ((900 - 100) / 100) - 1,
                title = { Text("弹幕字重") },
                valueLabel = { Text(remember(fontWeight) { "${fontWeight.toInt()}" }) },
                useThinSlider = useThinSlider,
            )

            val speed by remember(danmakuConfig) {
                mutableFloatStateOf(
                    danmakuConfig.speed / DanmakuConfig.Default.speed,
                )
            }
            SliderItem(
                value = speed,
                onValueChange = {
                    setDanmakuConfig(
                        danmakuConfig.copy(
                            speed = it * DanmakuConfig.Default.speed,
                        ),
                    )
                },
                valueRange = 0.2f..3f,
//                steps = ((3f - 0.2f) / 0.1f).toInt() - 1,
                title = { Text("弹幕速度") },
                description = { Text("弹幕速度不会跟随视频倍速变化") },
                valueLabel = { Text(remember(speed) { "${(speed * 100).roundToInt()}%" }) },
                useThinSlider = useThinSlider,
            )

            val platform = LocalPlatform.current
            val displayDensityRange = remember(platform) {
                // 100% .. 0%
                36.dp..(if (platform.isDesktop()) 720.dp else 240.dp)
            }
            var displayDensity by remember(danmakuConfig) {
                mutableFloatStateOf(
                    1.minus(
                        (danmakuConfig.safeSeparation - displayDensityRange.start) /
                                (displayDensityRange.endInclusive - displayDensityRange.start + 1.dp),
                    ).div(0.1f).roundToInt().toFloat(),
                )
            }
            SliderItem(
                value = displayDensity,
                onValueChange = {
                    displayDensity = it
                },
                // 这个会导致 repopulate, 所以改完了才更新
                onValueChangeFinished = {
                    setDanmakuConfig(
                        danmakuConfig.copy(
                            safeSeparation = displayDensityRange.start +
                                    ((displayDensityRange.endInclusive - displayDensityRange.start + 1.dp)
                                        .times((1 - displayDensity * 0.1f))),
                        ),
                    )
                },
                valueRange = 0f..10f,
                steps = 9,
                title = { Text("同屏密度") },
                valueLabel = {
                    when (displayDensity.toInt()) {
                        in 7..10 -> Text("密集")
                        in 4..6 -> Text("适中")
                        in 0..3 -> Text("稀疏")
                    }
                },
                useThinSlider = useThinSlider,
            )


            var displayArea by remember(danmakuConfig) {
                mutableFloatStateOf(
                    when (danmakuConfig.displayArea) {
                        0.125f -> 1f
                        0.25f -> 2f
                        0.50f -> 3f
                        0.75f -> 4f
                        1f -> 5f
                        else -> 2f
                    },
                )
            }
            SliderItem(
                value = displayArea,
                onValueChange = {
                    displayArea = it
                },
                // 这个会导致 repopulate, 所以改完了才更新
                onValueChangeFinished = {
                    setDanmakuConfig(
                        danmakuConfig.copy(
                            displayArea = when (displayArea) {
                                1f -> 0.125f
                                2f -> 0.25f
                                3f -> 0.50f
                                4f -> 0.75f
                                5f -> 1f
                                else -> 0.25f
                            },
                        ),
                    )
                },
                valueRange = 1f..5f,
                steps = 3,
                title = { Text("显示区域") },
                valueLabel = {
                    when (displayArea) {
                        1f -> Text("1/8 屏")
                        2f -> Text("1/4 屏")
                        3f -> Text("半屏")
                        4f -> Text("3/4 屏")
                        5f -> Text("全屏")
                    }
                },
                useThinSlider = useThinSlider,
            )

            SwitchItem(
                enableRegexFilter,
                onCheckedChange = {
                    switchDanmakuRegexFilterCompletely()
                },
                title = { Text("启用正则弹幕过滤器") },
            )

            TextItem(
                onClick = { onManageRegexFilters() },
            ) {
                Text("管理正则弹幕过滤器")
            }

            val debugViewModel = rememberDebugSettingsViewModel()
            if (debugViewModel.isAppInDebugMode) {

                SwitchItem(
                    danmakuConfig.isDebug,
                    onCheckedChange = {
                        setDanmakuConfig(
                            danmakuConfig.copy(isDebug = it),
                        )
                    },
                    title = { Text("弹幕调试模式") },
                )

                val debugSettings by debugViewModel.debugSettings
                SwitchItem(
                    debugSettings.showControllerAlwaysOnRequesters,
                    onCheckedChange = {
                        debugViewModel.debugSettings.update(debugSettings.copy(showControllerAlwaysOnRequesters = it))
                    },
                    title = { Text("showControllerAlwaysOnRequesters") },
                )
            }
        }
    }
}

