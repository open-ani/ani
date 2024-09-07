package me.him188.ani.danmaku.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import me.him188.ani.app.platform.isInLandscapeMode
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.episode.danmaku.DanmakuEditor
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettings
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.danmaku.api.DanmakuPresentation
import me.him188.ani.utils.platform.currentTimeMillis
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.random.nextLong
import kotlin.time.Duration.Companion.milliseconds

@Composable
@Preview(showBackground = true)
@Preview(showBackground = true, device = Devices.TABLET)
internal fun PreviewDanmakuHost() = ProvideCompositionLocalsForPreview {
    var emitted by remember { mutableIntStateOf(0) }
    val config = remember { mutableStateOf(
        DanmakuConfig(
            displayArea = 1.0f, 
            isDebug = true,
            style = DanmakuStyle.Default.copy(
                strokeWidth = 2f,
                strokeColor = Color.DarkGray,
                fontWeight = FontWeight.Normal
            )
        )
    ) }

    val startTime = remember { currentTimeMillis() }
    
    val data = remember {
        flow {
            var counter = 0
            
            fun danmaku() =
                Danmaku(
                    counter++.toString(),
                    "dummy",
                    currentTimeMillis() - startTime, 
                    "1",
                    DanmakuLocation.entries.random(),
                    text = LoremIpsum(Random.nextInt(1..5)).values.first(),
                    Color.Black.value.toInt(),
                )
            
            emit(danmaku())
            emit(danmaku())
            emit(danmaku())
            while (true) {
                emit(danmaku())
                emitted++
                delay(Random.nextLong(5, 10).milliseconds)
            }
        }
    }
    
    val state = remember { DanmakuHostState(config) }
    
    var editingText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(true) {
        data.collect {
            state.trySend(
                DanmakuPresentation(
                    it,
                    isSelf = false,
                ),
            )
        }
    }
    
    @Composable
    fun Editor(modifier: Modifier) {
        DanmakuEditor(
            text = editingText,
            modifier = modifier,
            onTextChange = { editingText = it },
            isSending = false,
            placeholderText = "placeholder of DanmakuEditor",
            onSend = {
                scope.launch {
                    val text = editingText
                    editingText = ""
                    state.send(
                        DanmakuPresentation(
                            Danmaku(
                                "self${Random.Default.nextLong(100000000L..999999999L)}",
                                "dummy sender",
                                currentTimeMillis() - startTime,
                                "2",
                                DanmakuLocation.entries.random(),
                                text = text,
                                0xfe1010,
                            ),
                            isSelf = true
                        )
                    )
                }
            }
        )
    }

    if (isInLandscapeMode()) {
        Row {
            Column(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.weight(1f)) {
                    DanmakuHost(state,
                        Modifier
                            .fillMaxSize()
                            .background(Color.Transparent))
                }
                Editor(
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp))
            }
            VerticalDivider()
            EpisodeVideoSettings(
                danmakuConfig = config.value,
                setDanmakuConfig = { config.value = it },
                modifier = Modifier.width(300.dp),
                danmakuRegexFilterGroup = { }
            )
        }
    } else {
        Column {
            DanmakuHost(state,
                Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .background(Color.Transparent))
            HorizontalDivider()
            EpisodeVideoSettings(
                danmakuConfig = config.value,
                setDanmakuConfig = { config.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                danmakuRegexFilterGroup = { },
            )
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
        val iter = remember { (0..360 step 36).map { with(density) { it.dp.toPx() } } }
        val danmaku = remember { dummyDanmaku(measurer, baseStyle, DanmakuStyle.Default) }
        
        Canvas(modifier = Modifier.size(width = 450.dp, height = 360.dp)) {
            iter.forEach { off ->
                with(danmaku) {
                    draw(
                        screenPosX = Random.nextFloat() * 100,
                        screenPosY = off
                    )
                }
            }
        }
    }
}