package me.him188.ani.danmaku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

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
        state.density = density
        state.baseStyle = baseStyle 
        state.textMeasurer = danmakuTextMeasurer
    }
    
    // observe screen size changes to calculate track height and track count
    LaunchedEffect(trackStubMeasurer) { state.observeTrack(trackStubMeasurer, density) }
    // calculate current play time on every frame
    LaunchedEffect(state.paused) { if (!state.paused) state.interpolateFrameLoop() }
    // logical tick for removal of danmaku
    LaunchedEffect(true) { state.tickLoop() }
    
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
    
    if (state.isDebug) {
        Column(modifier = Modifier.padding(4.dp).fillMaxSize()) { 
            Text("DanmakuHost state: ")
            Text("  paused: ${state.paused}")
            Text("  hostSize: ${state.hostWidth}x${state.hostHeight}, trackHeight: ${state.trackHeight}")
            Text("  elapsedFrameTimeMillis: ${state.elapsedFrameTimeNanos / 1_000_000}, avgFrameTimeMillis: ${state.avgFrameTimeNanos.avg() / 1_000_000}")
            Text("  presentDanmakuCount: ${state.presentDanmaku.size}")
            HorizontalDivider()
            Text("  floating tracks: ")
            for (track in state.floatingTrack) {
                Text("    $track")
            }
            Text("  top tracks: ")
            for (track in state.topTrack) {
                Text("    $track")
            }
            Text("  bottom tracks: ")
            for (track in state.bottomTrack) {
                Text("    $track")
            }
        }
    }
}