package me.him188.ani.app.ui.subject.episode.video.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.framework.AbstractSettingsViewModel
import me.him188.ani.app.ui.settings.framework.components.SliderItem
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import me.him188.ani.app.ui.theme.aniDarkColorTheme
import me.him188.ani.danmaku.ui.DanmakuConfig
import me.him188.ani.danmaku.ui.DanmakuStyle
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.roundToInt

@Stable
interface EpisodeVideoSettingsViewModel {
    val danmakuConfig: DanmakuConfig
    val isLoading: Boolean

    fun setDanmakuConfig(config: DanmakuConfig)
}

fun EpisodeVideoSettingsViewModel(): EpisodeVideoSettingsViewModel = EpisodeVideoSettingsViewModelImpl()

private class EpisodeVideoSettingsViewModelImpl : EpisodeVideoSettingsViewModel, AbstractSettingsViewModel(),
    KoinComponent {
    private val settingsRepository by inject<SettingsRepository>()

    val danmakuConfigSettings by settings(
        settingsRepository.danmakuConfig,
        DanmakuConfig(_placeholder = -1)
    )

    override val danmakuConfig: DanmakuConfig by danmakuConfigSettings
    override val isLoading: Boolean get() = danmakuConfigSettings.loading

    override fun setDanmakuConfig(config: DanmakuConfig) {
        danmakuConfigSettings.update(config)
    }
}

@Composable
fun EpisodeVideoSettings(
    vm: EpisodeVideoSettingsViewModel,
    modifier: Modifier = Modifier,
) {
    return EpisodeVideoSettings(
        danmakuConfig = vm.danmakuConfig,
        setDanmakuConfig = remember(vm) {
            vm::setDanmakuConfig
        },
        isLoading = remember(vm) {
            { vm.isLoading }
        },
        modifier = modifier,
    )
}

@Stable
private val LOADING_FALSE = { false }

@Composable
fun EpisodeVideoSettings(
    danmakuConfig: DanmakuConfig,
    setDanmakuConfig: (config: DanmakuConfig) -> Unit,
    isLoading: () -> Boolean = LOADING_FALSE,
    modifier: Modifier = Modifier,
) {
    val isLoadingState by remember(isLoading) {
        derivedStateOf(isLoading)
    }
    SettingsTab(modifier) {
        Group(
            useThinHeader = true,
            title = {
                Text("弹幕设置")
            },
        ) {
            val fontSize by remember(danmakuConfig) {
                mutableFloatStateOf(danmakuConfig.style.fontSize.value / DanmakuStyle.Default.fontSize.value)
            }
            SliderItem(
                value = fontSize,
                onValueChange = {
                    // 故意每次改都更新, 可以即时预览
                    setDanmakuConfig(
                        danmakuConfig.copy(style = danmakuConfig.style.copy(fontSize = DanmakuStyle.Default.fontSize * it))
                    )
                },
                valueRange = 0.50f..3f,
                steps = ((3f - 0.50f) / 0.05f).toInt() - 1,
                title = { Text("弹幕字号") },
                valueLabel = { Text(remember(fontSize) { "${(fontSize * 100).roundToInt()}%" }) },
                modifier = Modifier.placeholder(isLoadingState),
            )

            val alpha by remember(danmakuConfig) {
                mutableFloatStateOf(danmakuConfig.style.alpha)
            }
            SliderItem(
                value = alpha,
                onValueChange = {
                    setDanmakuConfig(
                        danmakuConfig.copy(style = danmakuConfig.style.copy(alpha = it))
                    )
                },
                valueRange = 0f..1f,
                steps = ((1f - 0f) / 0.05f).toInt() - 1,
                title = { Text("不透明度") },
                valueLabel = { Text(remember(alpha) { "${(alpha * 100).roundToInt()}%" }) },
                modifier = Modifier.placeholder(isLoadingState),
            )

            val strokeWidth by remember(danmakuConfig) {
                mutableFloatStateOf(danmakuConfig.style.strokeWidth / DanmakuStyle.Default.strokeWidth)
            }
            SliderItem(
                value = strokeWidth,
                onValueChange = {
                    setDanmakuConfig(
                        danmakuConfig.copy(style = danmakuConfig.style.copy(strokeWidth = it * DanmakuStyle.Default.strokeWidth))
                    )
                },
                valueRange = 0f..2f,
                steps = ((2f - 0f) / 0.1f).toInt() - 1,
                title = { Text("描边宽度") },
                valueLabel = { Text(remember(strokeWidth) { "${(strokeWidth * 100).roundToInt()}%" }) },
                modifier = Modifier.placeholder(isLoadingState),
            )

            var speed by remember(danmakuConfig) {
                mutableFloatStateOf(
                    danmakuConfig.speed / DanmakuConfig.Default.speed
                )
            }
            SliderItem(
                value = speed,
                onValueChange = { speed = it },
                valueRange = 0.2f..3f,
                steps = ((3f - 0.2f) / 0.1f).toInt() - 1,
                title = { Text("弹幕速度") },
                description = { Text("弹幕速度不会跟随视频倍速变化") },
                onValueChangeFinished = {
                    setDanmakuConfig(
                        danmakuConfig.copy(
                            speed = speed * DanmakuConfig.Default.speed
                        )
                    )
                },
                valueLabel = { Text(remember(speed) { "${(speed * 100).roundToInt()}%" }) },
                modifier = Modifier.placeholder(isLoadingState),
            )
            var enableColor = remember(danmakuConfig) { danmakuConfig.enableColor }
            SwitchItem(
                enableColor,
                onCheckedChange = {
                    enableColor = it
                    setDanmakuConfig(
                        danmakuConfig.copy(enableColor = it)
                    )
                },
                title = { Text("彩色弹幕") },
                description = { Text("关闭后所有彩色弹幕都会显示为白色") },
                modifier = Modifier.placeholder(isLoadingState),
            )
        }
    }
}

@Composable
fun VideoSettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier
    ) {
        Icon(
            Icons.Rounded.Settings, contentDescription = "Settings",
            tint = aniDarkColorTheme().onBackground,
        )
    }
}