package me.him188.ani.danmaku.ui.new

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import me.him188.ani.app.data.models.danmaku.DanmakuFilterConfig
import me.him188.ani.app.data.models.danmaku.DanmakuRegexFilter
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettings
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettingsViewModel
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.danmaku.api.DanmakuPresentation
import me.him188.ani.danmaku.ui.DanmakuConfig
import me.him188.ani.utils.platform.annotations.TestOnly
import me.him188.ani.utils.platform.currentTimeMillis
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.milliseconds

/**
 * Note, use "Run Preview" to preview on your phone or the emulator if you only see the initial danmaku-s.
 */
@Composable
@Preview(showBackground = true, device = Devices.TABLET)
internal fun PreviewDanmakuHost() = ProvideCompositionLocalsForPreview {
    var emitted by remember { mutableIntStateOf(0) }
    val config = remember { mutableStateOf(DanmakuConfig(displayArea = 1.0f)) }
    var upstreamCurrent by remember { mutableLongStateOf(0) }
    
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
                delay(Random.nextLong(10, 25).milliseconds)
            }
        }
    }
    
    val progress = remember {
        flow {
            val startTime = currentTimeMillis()
            while (true) {
                val time = currentTimeMillis() -  startTime
                upstreamCurrent = time
                emit(time.milliseconds)
                delay(1000 / 30)
            }
        }
    }
    
    val state = remember { DanmakuHostState(progress, config) }
    
    LaunchedEffect(true) {
        data.collect {
            state.send(
                DanmakuPresentation(
                    it,
                    isSelf = Random.nextBoolean(),
                ),
            )
        }
    }

    Row {
        Box(Modifier.weight(1f)) {
            DanmakuHost(
                state,
                Modifier.fillMaxHeight(),
            )
            Column(modifier = Modifier.padding(4.dp)) {
                Text("emitted: $emitted")
                Text("upstream time: ${upstreamCurrent}, current time millis: ${state.currentTimeMillis}")
                // Text("  gli: ${state.glitched}, delta: ${state.delta}, interpCurr: ${state.interpCurr}, interpUpst: ${state.interpUpst}")
                // Text("frame version: ${state.frameVersion}")
                // Text("frame time delta: ${state.delta}")
                Text("present danmaku count: ${state.presentDanmaku.size}")
                Text("trackHeight: ${state.trackHeightState.value}")
                HorizontalDivider()
                state.floatingTrack.forEach { danmakuTrackState ->
                    Text(
                        "track floating ${danmakuTrackState.trackIndex}: " +
                                "size=${danmakuTrackState.danmakuList.size}, "
                    )
                }
                HorizontalDivider()
                state.topTrack.forEach { danmakuTrackState ->
                    Text(
                        "track top ${danmakuTrackState.trackIndex}: " +
                                "curr=${danmakuTrackState.currentDanmaku}, "
                    )
                }
                HorizontalDivider()
                state.bottomTrack.forEach { danmakuTrackState ->
                    Text(
                        "track bottom ${danmakuTrackState.trackIndex}: " +
                                "curr=${danmakuTrackState.currentDanmaku}, "
                    )
                }
            }
        }
        VerticalDivider()
        EpisodeVideoSettings(
            remember {
                TestEpisodeVideoSettingsViewModel(config) { config.value = it }
            },
            modifier = Modifier.width(300.dp)
        )
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

    override fun addDanmakuRegexFilter(filter: DanmakuRegexFilter) {
        //Do nothing in preview
    }

    override fun editDanmakuRegexFilter(id: String, filter: DanmakuRegexFilter) {
        //Do nothing in preview
    }

    override fun removeDanmakuRegexFilter(filter: DanmakuRegexFilter) {
        //Do nothing in preview
    }

    override fun switchDanmakuRegexFilterCompletely() {
        //Do nothing in preview

    }

    override fun switchDanmakuRegexFilter(filter: DanmakuRegexFilter) {
        // Do nothing in preview
    }
}