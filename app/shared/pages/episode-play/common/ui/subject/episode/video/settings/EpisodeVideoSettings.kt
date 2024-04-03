package me.him188.ani.app.ui.subject.episode.video.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.merge
import me.him188.ani.app.data.PreferencesRepository
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.theme.aniDarkColorTheme
import me.him188.ani.danmaku.ui.DanmakuConfig
import me.him188.ani.danmaku.ui.DanmakuStyle
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

interface EpisodeVideoSettingsViewModel {
    val danmakuConfig: Flow<DanmakuConfig>
    fun setDanmakuConfig(config: DanmakuConfig)
}


fun EpisodeVideoSettingsViewModel(): EpisodeVideoSettingsViewModel = EpisodeVideoSettingsViewModelImpl()

private class EpisodeVideoSettingsViewModelImpl : EpisodeVideoSettingsViewModel, AbstractViewModel(), KoinComponent {
    private val preferencesRepository by inject<PreferencesRepository>()

    private val danmakuConfigPersistent = preferencesRepository.danmakuConfig.flow
    private val danmakuConfigUpdate: MutableStateFlow<DanmakuConfig?> = MutableStateFlow(null)
    override val danmakuConfig = merge(danmakuConfigPersistent, danmakuConfigUpdate).filterNotNull()

    override fun init() {
        super.init()
        launchInBackground {
            danmakuConfigUpdate.debounce(0.1.seconds).filterNotNull().collect {
                logger.info { "Saving DanmakuConfig: $it" }
                preferencesRepository.danmakuConfig.set(it)
            }
        }
    }

    override fun setDanmakuConfig(config: DanmakuConfig) {
        danmakuConfigUpdate.value = config
    }

    private companion object {
        private val logger = logger(EpisodeVideoSettingsViewModelImpl::class)
    }
}

private data class StepRange(
    val range: ClosedFloatingPointRange<Float>,
    val steps: Int,
)

//@Stable
//private fun generateRange(
//    maxRange: ClosedFloatingPointRange<Float>,
//    stepSize: Float,
//    steps: Int
//): StepRange {
//    return base - stepSize * steps / 2..base + stepSize * steps / 2
//}

@Composable
fun EpisodeVideoSettings(
    vm: EpisodeVideoSettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val danmakuConfig by vm.danmakuConfig.collectAsStateWithLifecycle(DanmakuConfig.Default)
    return EpisodeVideoSettings(
        danmakuConfig = danmakuConfig,
        setDanmakuConfig = vm::setDanmakuConfig,
        modifier = modifier,
    )
}

@Composable
fun EpisodeVideoSettings(
    danmakuConfig: DanmakuConfig,
    setDanmakuConfig: (config: DanmakuConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            "弹幕设置",
            Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium
        )

        Column {
            val fontSize by remember(danmakuConfig) {
                mutableFloatStateOf(danmakuConfig.style.fontSize.value / DanmakuStyle.Default.fontSize.value)
            }
            Text(
                "弹幕字号: ${(fontSize * 100).roundToInt()}%",
                Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            Slider(
                value = fontSize,
                onValueChange = {
                    setDanmakuConfig(
                        danmakuConfig.copy(danmakuConfig.style.copy(fontSize = DanmakuStyle.Default.fontSize * it))
                    )
                },
                valueRange = 0.20f..3f,
                steps = ((3f - 0.20f) / 0.1f).toInt() - 1,
            )
        }

        Column {
            val alpha by remember(danmakuConfig) {
                mutableFloatStateOf(danmakuConfig.style.alpha)
            }
            Text(
                "不透明度: ${(alpha * 100).roundToInt()}%",
                Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            Slider(
                value = alpha,
                onValueChange = {
                    setDanmakuConfig(
                        danmakuConfig.copy(danmakuConfig.style.copy(alpha = it))
                    )
                },
                valueRange = 0f..1f,
                steps = ((1f - 0f) / 0.05f).toInt() - 1,
            )
        }


        Column {
            var strokeMiterValue by remember(danmakuConfig) {
                mutableFloatStateOf(danmakuConfig.style.strokeWidth / DanmakuStyle.Default.strokeWidth)
            }
            Text(
                "描边宽度: ${(strokeMiterValue * 100).roundToInt()}%",
                Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = strokeMiterValue,
                onValueChange = { strokeMiterValue = it },
                valueRange = 0f..2f,
                steps = ((2f - 0f) / 0.1f).toInt() - 1,
                onValueChangeFinished = {
                    setDanmakuConfig(
                        danmakuConfig.copy(danmakuConfig.style.copy(strokeMidth = strokeMiterValue * DanmakuStyle.Default.strokeWidth))
                    )
                }
            )
        }

        Column {
            var speed by remember(danmakuConfig) {
                mutableFloatStateOf(
                    danmakuConfig.speed / DanmakuConfig.Default.speed
                )
            }
            Text(
                "弹幕速度: ${(speed * 100).roundToInt()}%",
                Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "弹幕速度不会跟随视频倍速变化",
                Modifier.padding(horizontal = 8.dp).padding(top = 4.dp),
                style = MaterialTheme.typography.labelSmall
            )
            Slider(
                value = speed,
                onValueChange = { speed = it },
                valueRange = 0.2f..3f,
                steps = ((3f - 0.2f) / 0.1f).toInt() - 1,
                onValueChangeFinished = {
                    setDanmakuConfig(
                        danmakuConfig.copy(
                            speed = speed * DanmakuConfig.Default.speed
                        )
                    )
                }
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