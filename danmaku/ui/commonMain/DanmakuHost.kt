package me.him188.ani.danmaku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import kotlinx.coroutines.delay

@Composable
fun DanmakuHost(
    state: DanmakuHostState,
    modifier: Modifier = Modifier,
    baseStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val density = LocalDensity.current
    val trackStubMeasurer = rememberTextMeasurer(1)
    val danmakuTextMeasurer = rememberTextMeasurer(3000)
    SideEffect { 
        state.baseStyle = baseStyle 
        state.textMeasurer = danmakuTextMeasurer
    }
    // observe screen size changes to calculate track height and track count
    LaunchedEffect(trackStubMeasurer) { state.observeTrack(trackStubMeasurer, density) }
    // calculate current play time on every frame
    LaunchedEffect(state.paused) { if (!state.paused) state.interpolateFrameLoop() }
    // logical tick for danmaku track
    LaunchedEffect(true) { 
        while (true) {
            state.tick()
            delay(1000 / 30)
        }
    }
    
    BoxWithConstraints(modifier) {
        Canvas(
            Modifier
                .fillMaxSize()
                .alpha(state.canvasAlpha)
                .onSizeChanged { 
                    state.hostWidth = it.width
                    state.hostHeight = it.height
                }
        ) {
            for (danmaku in state.presentDanmaku) {
                drawDanmakuText(
                    state = danmaku.state, 
                    screenPosX = danmaku.calculatePosX(), 
                    screenPosY = danmaku.calculatePosY()
                )
            }
        }
    }
}