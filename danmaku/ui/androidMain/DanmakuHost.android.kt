package me.him188.ani.danmaku.ui.new

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import me.him188.ani.app.data.models.danmaku.DanmakuFilterConfig
import me.him188.ani.app.data.models.danmaku.DanmakuRegexFilter
import me.him188.ani.app.platform.isInLandscapeMode
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettings
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettingsViewModel
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.danmaku.api.DanmakuPresentation
import me.him188.ani.danmaku.ui.DanmakuConfig
import me.him188.ani.danmaku.ui.DanmakuHost
import me.him188.ani.danmaku.ui.DanmakuHostState
import me.him188.ani.danmaku.ui.DanmakuStyle
import me.him188.ani.danmaku.ui.drawDanmakuText
import me.him188.ani.danmaku.ui.dummyDanmaku
import me.him188.ani.utils.platform.currentTimeMillis
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
@Preview(showBackground = true)
@Preview(showBackground = true, device = Devices.TABLET)
internal fun PreviewDanmakuHost() = ProvideCompositionLocalsForPreview {
    var emitted by remember { mutableIntStateOf(0) }
    val config = remember { mutableStateOf(DanmakuConfig(displayArea = 1.0f, isDebug = true)) }
    
    val data = remember {
        flow {
            var counter = 0
            val startTime = currentTimeMillis()
            
            fun danmaku() =
                Danmaku(
                    counter++.toString(),
                    "dummy",
                    currentTimeMillis() - startTime, 
                    "1",
                    DanmakuLocation.entries.random(),
                    text = LoremIpsum(Random.nextInt(1..5)).values.first(),
                    0xffffff,
                )
            
            emit(danmaku())
            emit(danmaku())
            emit(danmaku())
            while (true) {
                emit(danmaku())
                emitted++
                delay(Random.nextLong(5, 5).milliseconds)
            }
        }
    }
    
    val state = remember { DanmakuHostState(config) }
    
    LaunchedEffect(true) {
        data.collect {
            state.trySend(
                DanmakuPresentation(
                    it,
                    isSelf = Random.nextBoolean(),
                ),
            )
        }
    }

    if (isInLandscapeMode()) {
        Row {
            Box(Modifier.weight(1f)) {
                DanmakuHost(state, Modifier.fillMaxHeight(),)
            }
            VerticalDivider()
            EpisodeVideoSettings(
                remember { TestEpisodeVideoSettingsViewModel(config) { config.value = it } },
                Modifier.width(300.dp)
            )
        }
    } else {
        Column {
            DanmakuHost(state, Modifier.fillMaxWidth().height(360.dp),)
            HorizontalDivider()
            Box(Modifier.weight(1f)) {
                EpisodeVideoSettings(
                    remember { TestEpisodeVideoSettingsViewModel(config) { config.value = it } }
                )
            }
        }
    }
}

@Composable
@Preview("Light", showBackground = true)
@Preview("Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
private fun PreviewDanmakuText() {
    ProvideCompositionLocalsForPreview {
        val measurer = rememberTextMeasurer()
        val baseStyle = MaterialTheme.typography.bodyMedium
        val density = LocalDensity.current
        val iter = (0..360 step 36).map { with(density) { it.dp.toPx() } }
        Canvas(modifier = Modifier.size(width = 450.dp, height = 360.dp)) {
            iter.forEach { off ->
                drawDanmakuText(
                    dummyDanmaku(measurer, baseStyle, DanmakuStyle.Default),
                    screenPosX = Random.nextFloat() * 100,
                    screenPosY = off
                )
            }
        }
    }
}

class TestEpisodeVideoSettingsViewModel(
    private val danmakuConfigState: State<DanmakuConfig>,
    private val setDanmakuConfig: (DanmakuConfig) -> Unit,
) : EpisodeVideoSettingsViewModel {
    override val danmakuConfig: DanmakuConfig by danmakuConfigState
    override val danmakuRegexFilterList: List<DanmakuRegexFilter> = emptyList()
    override val danmakuFilterConfig: DanmakuFilterConfig = DanmakuFilterConfig.Default
    override val isLoading: Boolean = false

    override fun setDanmakuConfig(config: DanmakuConfig) {
        setDanmakuConfig.invoke(config)
    }

    override fun addDanmakuRegexFilter(filter: DanmakuRegexFilter) { }
    override fun editDanmakuRegexFilter(id: String, filter: DanmakuRegexFilter) { }
    override fun removeDanmakuRegexFilter(filter: DanmakuRegexFilter) { }
    override fun switchDanmakuRegexFilterCompletely() { }
    override fun switchDanmakuRegexFilter(filter: DanmakuRegexFilter) { }
}