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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce

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
        state.setUIContext(baseStyle, danmakuTextMeasurer, density)
    }
    
    // observe screen size changes to calculate track height and track count
    LaunchedEffect(trackStubMeasurer) { state.observeTrack(trackStubMeasurer) }
    // calculate current play time on every frame
    LaunchedEffect(state.paused) { if (!state.paused) state.interpolateFrameLoop() }
    // logical tick for removal of danmaku
    LaunchedEffect(true) { 
        while (true) {
            state.tick()
            delay(1000 / 10) // 10 fps
        }
    }
    
    BoxWithConstraints(modifier) {
        val screenWidth by rememberUpdatedState(constraints.maxWidth)
        val screenHeight by rememberUpdatedState(constraints.maxHeight)
        
        LaunchedEffect(true) {
            snapshotFlow { screenWidth to screenHeight }
                .debounce(1000 / 30)
                .collect { (width, height) ->
                    state.hostWidth = width
                    state.hostHeight = height
                }
        }
        
        Canvas(
            modifier = Modifier.fillMaxSize()
                .clipToBounds()
                .alpha(state.canvasAlpha)
        ) {
            state.elapsedFrameTimeNanos // subscribe changes
            for (danmaku in state.presentDanmaku) {
                drawDanmakuText(
                    state = danmaku.danmaku,
                    screenPosX = danmaku.x, 
                    screenPosY = danmaku.y
                )
            }
        }
    }
    
    if (state.isDebug) {
        Column(modifier = Modifier.padding(4.dp).fillMaxSize()) { 
            Text("DanmakuHost state: ")
            Text("  hostSize: ${state.hostWidth}x${state.hostHeight}, trackHeight: ${state.trackHeight}")
            Text("  paused: ${state.paused}, elapsedFrameTimeMillis: ${state.elapsedFrameTimeNanos / 1_000_000}")
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