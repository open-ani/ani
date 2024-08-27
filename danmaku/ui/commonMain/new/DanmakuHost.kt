package me.him188.ani.danmaku.ui.new

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer

@Composable
fun DanmakuHost(
    state: DanmakuHostState,
    modifier: Modifier = Modifier,
    baseStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val density = LocalDensity.current
    val trackStubMeasurer = rememberTextMeasurer(1)
    
    SideEffect { state.baseStyle = baseStyle }
    LaunchedEffect(trackStubMeasurer) { state.observeTrack(trackStubMeasurer, density) }
    LaunchedEffect(true) { state.interpolateFrameLoop() }
    
    BoxWithConstraints(modifier) {
        SideEffect {
            state.hostWidth = constraints.maxWidth
            state.hostHeight = constraints.maxHeight
        }
        
        val width by remember { derivedStateOf { with(density) { state.hostWidth.toDp() } } }
        val height by remember { derivedStateOf { with(density) { state.hostHeight.toDp() } } }
        
        
        Canvas(Modifier.size(width, height)) {
            var currentIndex = 0
            for (danmaku in state.presentDanmaku) {
                drawDanmakuText(
                    danmaku.state, 
                    state.presentDanmakuPositions[currentIndex * 2], 
                    state.presentDanmakuPositions[currentIndex * 2 + 1]
                )
                currentIndex ++
            }
        }
    }
}