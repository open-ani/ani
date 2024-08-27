package me.him188.ani.danmaku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.math.roundToInt

/**
 * 容纳[弹幕轨道][FloatingDanmakuTrack]的 [Column].
 *
 * @see DanmakuHostState
 */
@Composable
fun DanmakuHost(
    state: DanmakuHostState,
    modifier: Modifier = Modifier,
) {
    val baseStyle = MaterialTheme.typography.bodyMedium
    val textMeasurer = rememberTextMeasurer(state.textMeasurerCacheSize * 2)
    val verticalPadding = 1.dp // to both top and bottom

    val density by rememberUpdatedState(LocalDensity.current)

    // update floating danmaku when enabled
    if (state.config.enableFloating) {
        val layoutDirection by rememberUpdatedState(LocalLayoutDirection.current)
        val safeSeparation by remember { derivedStateOf { with(density) { state.config.safeSeparation.toPx() } } }
        val speedPxPerSecond by remember { derivedStateOf { with(density) { state.config.speed.dp.toPx() } } }

        LaunchedEffect(
            state.floatingTracks,
            state.isPaused,
            state.trackWidth,
            state.trackHeight,
            state.config
        ) {
            supervisorScope {
                var currentYOffset = 0f
                val verticalPaddingPx = with(density) { (verticalPadding * 2).toPx() }
                
                for (track in state.floatingTracks) {
                    // update y offset of floating track
                    track.trackPosY = currentYOffset
                    currentYOffset += state.trackHeight + verticalPaddingPx
                    
                    launch {
                        while (isActive) {
                            track.tick(layoutDirection, safeSeparation)
                            delay(1000 / 30)
                        }
                    }
                    
                    launch { 
                        while (isActive) {
                            track.receiveNewDanmaku()
                            delay(1000 / 30)
                        }
                    }

                    if (!state.isPaused && state.trackWidth != 0) {
                        launch { track.animateMove(speedPxPerSecond) }
                    }
                }
            }
        }
    }
    
    BoxWithConstraints(modifier) {
        val screenHeightPx by rememberUpdatedState(constraints.maxHeight)
        
        SideEffect {
            state.baseStyle = baseStyle
            state.textMeasurer = textMeasurer
        }

        // 更新显示区域
        val dummyTextMeasurer = rememberTextMeasurer(1)
        LaunchedEffect(dummyTextMeasurer) {
            combine(
                snapshotFlow { state.config }.distinctUntilChanged { old, new -> 
                    old.displayArea == new.displayArea && old.style == new.style 
                },
                snapshotFlow { screenHeightPx }.debounce(500)
            ) { config, screenHeightPx ->
                val danmakuTextHeight = dummyTextMeasurer.measure(
                    DummyDanmakuState.presentation.danmaku.text,
                    style = config.style.styleForText(),
                ).size.height
                val verticalPaddingPx = with(density) { (verticalPadding * 2).toPx() }
                
                val danmakuTrackHeight = danmakuTextHeight + verticalPaddingPx
                val danmakuTrackCount = screenHeightPx / danmakuTrackHeight * config.displayArea
                
                Pair(
                    danmakuTrackCount.roundToInt().coerceAtLeast(1), 
                    danmakuTrackHeight.toInt()
                )
            }
                .distinctUntilChanged()
                .collect { (trackCount, trackHeight) ->
                    state.setTrackCount(trackCount)
                    // update track height
                    state.trackHeightState.value = trackHeight
                }
        }
        
        
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                // update track width
                .onSizeChanged { state.trackWidthState.value = it.width }
        ) {
            // 浮动弹幕
            if (state.config.enableFloating) {
                for (track in state.floatingTracks) {
                    track.visibleDanmaku.forEach { danmaku ->
                        drawDanmakuText(
                            danmaku.state,
                            borderTextMeasurer = textMeasurer,
                            solidTextMeasurer = textMeasurer,
                            offsetX = danmaku.posXInScreen,
                            offsetY = danmaku.posYInScreen,
                            baseStyle = baseStyle,
                            onTextLayout = { danmaku.state.textWidth = it.size.width }
                        )
                    }
                }
            }
        }
        
        return@BoxWithConstraints

        /*// 顶部和底部
        Column(Modifier.matchParentSize().background(Color.Transparent)) {
            if (config.enableTop) {
                for (track in state.topTracks) {
                    FixedDanmakuTrack(
                        track,
                        Modifier.fillMaxWidth(),
                        configProvider,
                        baseStyle = baseStyle,
                        frozen = frozen,
                    ) {
                        HeightHolder(verticalPadding)
                        track.visibleDanmaku?.let {
                            danmaku(it)
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            if (config.enableBottom) {
                for (track in state.bottomTracks) {
                    FixedDanmakuTrack(
                        track,
                        Modifier.fillMaxWidth(),
                        configProvider,
                        baseStyle = baseStyle,
                        frozen = frozen,
                    ) {
                        HeightHolder(verticalPadding)
                        track.visibleDanmaku?.let {
                            danmaku(it)
                        }
                    }
                }
            }
        }

        // 浮动弹幕
        Column(Modifier.background(Color.Transparent)) {
            if (config.enableFloating) {
                val measurer = rememberTextMeasurer(1)
                val density by rememberUpdatedState(LocalDensity.current)
                // 更新显示区域
                

                for (track in state.floatingTracks) {
                    FloatingDanmakuTrack(
                        track,
                        Modifier.fillMaxWidth(),
                        configProvider,
                        baseStyle = baseStyle,
                        frozen = frozen,
                    ) {
                        HeightHolder(verticalPadding)

                        for (danmaku in track.visibleDanmaku) {
                            key(danmaku.presentation.id) {
                                danmaku(danmaku)
                            }
                        }
                    }
                }
            }
        }*/
    }
}

@Composable
private fun DanmakuTrackScope.HeightHolder(verticalPadding: Dp) {
    // fix height even if there is no danmaku showing
    // so that the second track will not move to the top of the screen when the first track is empty.
    danmaku(DummyDanmakuState, Modifier.alpha(0f).padding(vertical = verticalPadding))
}