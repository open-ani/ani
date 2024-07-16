package me.him188.ani.app.videoplayer.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import me.him188.ani.app.ui.foundation.theme.aniDarkColorTheme
import me.him188.ani.app.ui.foundation.theme.disabledWeaken
import me.him188.ani.app.ui.foundation.theme.slightlyWeaken
import me.him188.ani.app.ui.foundation.theme.weaken
import me.him188.ani.app.videoplayer.ui.state.Chunk
import me.him188.ani.app.videoplayer.ui.state.ChunkState
import me.him188.ani.app.videoplayer.ui.state.MediaCacheProgressState
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import me.him188.ani.datasources.bangumi.processing.fixToString
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * 播放器进度滑块的状态.
 *
 * - 支持从 [currentPositionMillis] 同步当前播放位置, 从 [totalDurationMillis] 同步总时长.
 * - 使用 [onPreview] 和 [onPreviewFinished] 来处理用户拖动进度条的事件.
 *
 * @see MediaProgressSlider
 */
@Stable
class MediaProgressSliderState(
    currentPositionMillis: () -> Long,
    totalDurationMillis: () -> Long,
    /**
     * 当用户正在拖动进度条时触发. 每有一个 change 都会调用.
     */
    private val onPreview: (positionMillis: Long) -> Unit,
    /**
     * 当用户松开进度条时触发. 此时播放器应当要跳转到该位置.
     */
    private val onPreviewFinished: (positionMillis: Long) -> Unit,
) {
    val currentPositionMillis: Long by derivedStateOf(currentPositionMillis)
    val totalDurationMillis: Long by derivedStateOf(totalDurationMillis)

    private var previewPositionMillis: Long by mutableStateOf(-1L)

    private fun previewPosition(positionMillis: Long) {
        previewPositionMillis = positionMillis
        onPreview(positionMillis)
    }

    /**
     * Sets the slider to move to the given position.
     * [onPreview] will be triggered.
     */
    fun previewPositionRatio(ratio: Float) {
        previewPosition((totalDurationMillis * ratio).roundToLong())
    }

    /**
     * The position to display in the progress slider. If a preview is active, this will be the preview position.
     */
    private val displayPositionMillis: Long by derivedStateOf {
        val previewPositionMillis = previewPositionMillis
        if (previewPositionMillis != -1L) {
            previewPositionMillis
        } else {
            this.currentPositionMillis
        }
    }

    /**
     * The ratio of the current display position to the total duration. Range is `0..1`
     */
    val displayPositionRatio by derivedStateOf {
        val total = this.totalDurationMillis
        if (total == 0L) {
            return@derivedStateOf 0f
        }
        displayPositionMillis.toFloat() / total
    }

    fun finishPreview() {
        onPreviewFinished(previewPositionMillis)
        previewPositionMillis = -1L
    }
}

/**
 * 便捷方法, 从 [PlayerState.currentPositionMillis] 创建  [MediaProgressSliderState]
 */
@Composable
fun rememberMediaProgressSliderState(
    playerState: PlayerState,
    onPreview: (positionMillis: Long) -> Unit,
    onPreviewFinished: (positionMillis: Long) -> Unit,
): MediaProgressSliderState {
    val currentPosition by playerState.currentPositionMillis.collectAsStateWithLifecycle()
    val totalDuration by remember(playerState) {
        playerState.videoProperties.filterNotNull().map { it.durationMillis }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(0L)

    val onPreviewUpdated by rememberUpdatedState(onPreview)
    val onPreviewFinishedUpdated by rememberUpdatedState(onPreviewFinished)
    return remember(currentPosition, totalDuration) {
        MediaProgressSliderState(
            { currentPosition },
            { totalDuration },
            onPreviewUpdated,
            onPreviewFinishedUpdated,
        )
    }
}


/**
 * 视频播放器的进度条, 支持拖动调整播放位置, 支持显示缓冲进度.
 */
@Composable
fun MediaProgressSlider(
    state: MediaProgressSliderState,
    cacheState: MediaCacheProgressState,
    trackBackgroundColor: Color = aniDarkColorTheme().surface,
    trackProgressColor: Color = aniDarkColorTheme().primary,
    cachedProgressColor: Color = aniDarkColorTheme().onSurface.weaken(),
    downloadingColor: Color = aniDarkColorTheme().onSurface.disabledWeaken(),
    notAvailableColor: Color = aniDarkColorTheme().error.slightlyWeaken(),
    stopColor: Color = aniDarkColorTheme().primary,
    previewTimeBackgroundColor: Color = aniDarkColorTheme().surface.copy(alpha = 0.3f),
//    drawThumb: @Composable DrawScope.() -> Unit = {
//        drawCircle(
//            MaterialTheme.colorScheme.primary,
//            radius = 12f,
//        )
//    },
    modifier: Modifier = Modifier,
) {
    Box(
        modifier.fillMaxWidth()
            .height(24.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.fillMaxWidth().height(6.dp).padding(horizontal = 4.dp).clip(RoundedCornerShape(12.dp))) {
            Canvas(Modifier.matchParentSize()) {
                // draw track
                drawRect(
                    trackBackgroundColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height),
                )
            }

            Canvas(Modifier.matchParentSize()) {
                // draw cached progress

                cacheState.version // subscribe to state change

                var currentX = 0f

                // 连续的缓存区块连着画, 否则会因精度缺失导致不连续
                forEachConsecutiveChunk(cacheState.chunks) { state, weight ->
                    val color = when (state) {
                        ChunkState.NONE -> Color.Unspecified
                        ChunkState.DOWNLOADING -> downloadingColor
                        ChunkState.DONE -> cachedProgressColor
                        ChunkState.NOT_AVAILABLE -> notAvailableColor
                    }
                    if (color != Color.Unspecified) {
                        val size = Size(
                            weight * size.width,
                            size.height,
                        )// TODO: draw more cache states (colors)
                        drawRect(
                            color,
                            topLeft = Offset(currentX, 0f),
                            size = size,
                        )
                    }
                    currentX += weight * size.width
                }
            }

            Canvas(Modifier.matchParentSize()) {
                // draw play progress
                drawRect(
                    trackProgressColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width * state.displayPositionRatio, size.height),
                )
            }

            Canvas(Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)) {
                // draw stop
                drawCircle(
                    stopColor,
                    radius = 2.dp.toPx(),
                    blendMode = BlendMode.Src, // override background
                )
            }
        }

        var mousePosX by remember { mutableStateOf(0f) }
        var previewTimeVisible by remember { mutableStateOf(false) }
        var offsetX by remember { mutableIntStateOf(0) }
        var previewTimeText by remember { mutableStateOf("") }
        val previewTimeTextBox = @Composable {
            Box(
                modifier = Modifier
                    .background(previewTimeBackgroundColor, shape = RoundedCornerShape(6.dp)),
            ) {
                Text(
                    text = previewTimeText,
                    textAlign = TextAlign.Center,
                )
            }
        }
        val hoverInteraction = remember { MutableInteractionSource() }
        LaunchedEffect(true) {
            hoverInteraction.interactions.collect {
                when (it) {
                    is HoverInteraction.Enter -> previewTimeVisible = true
                    is HoverInteraction.Exit -> previewTimeVisible = false
                }
            }
        }
        // draw thumb
        val interactionSource = remember { MutableInteractionSource() }
        val thumb = @Composable {

            SliderDefaults.Thumb(
                interactionSource = interactionSource,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                ),
                enabled = true,
            )
        }
        val slider = @Composable {
            Slider(
                value = state.displayPositionRatio,
                valueRange = 0f..1f,
                onValueChange = { state.previewPositionRatio(it) },
                interactionSource = interactionSource,
                thumb = { thumb() },
                track = {
                    SliderDefaults.Track(
                        it,
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent,
                            disabledActiveTrackColor = Color.Transparent,
                            disabledInactiveTrackColor = Color.Transparent,
                        ),
                    )
                },
                onValueChangeFinished = {
                    state.finishPreview()
                },
                modifier = Modifier.fillMaxWidth().height(24.dp)
                    .hoverable(interactionSource = hoverInteraction)
                    .onPointerEvent(PointerEventType.Move) {
                        mousePosX = it.changes.first().position.x
                    },
            )
        }

        SubcomposeLayout { constraints ->
            var thumbWidth = 0
            subcompose("thumb", thumb).map {
                val placeable = it.measure(constraints)
                thumbWidth = placeable.width.coerceAtLeast(thumbWidth)
                placeable
            }
            var sliderWidth = 0
            val sliderPlaceables = subcompose("slider", slider).map {
                val placeable = it.measure(constraints)
                sliderWidth = placeable.width.coerceAtLeast(sliderWidth)
                placeable
            }
            var previewTimeTextWidth = 0
            val previewTimeTextPlaceables = subcompose("previewTimeTextBox", previewTimeTextBox).map {
                val placeable = it.measure(constraints)
                previewTimeTextWidth = placeable.width.coerceAtLeast(previewTimeTextWidth)
                placeable
            }
            val percent = mousePosX.minus(thumbWidth / 2).div(sliderWidth - thumbWidth)
            val previewTimeMillis = state.totalDurationMillis.times(percent).toLong()
            previewTimeText = renderSeconds(previewTimeMillis / 1000, state.totalDurationMillis / 1000)
            offsetX = (mousePosX - previewTimeTextWidth / 2).roundToInt()
            layout(constraints.maxWidth, constraints.maxHeight) {
                sliderPlaceables.forEach {
                    it.placeRelative(0, 0)
                }
                if (previewTimeVisible) {
                    previewTimeTextPlaceables.forEach {
                        it.placeRelative(offsetX, (-24).dp.roundToPx())
                    }
                }
            }
        }
    }
}

private fun renderSeconds(current: Long, total: Long?): String {
    if (total == null) {
        return "00:${current.fixToString(2)}"
    }
    return if (current < 60 && total < 60) {
        "00:${current.fixToString(2)}"
    } else if (current < 3600 && total < 3600) {
        val startM = (current / 60).fixToString(2)
        val startS = (current % 60).fixToString(2)
        """$startM:$startS"""
    } else {
        val startH = (current / 3600).fixToString(2)
        val startM = (current % 3600 / 60).fixToString(2)
        val startS = (current % 60).fixToString(2)
        """$startH:$startM:$startS"""
    }
}
private inline fun forEachConsecutiveChunk(
    chunks: List<Chunk>,
    action: (state: ChunkState, weight: Float) -> Unit
) {
    if (chunks.isEmpty()) return

    var currentState: ChunkState = chunks[0].state
    var start = 0
    var end = 0

    for (index in 1..chunks.lastIndex) {
        val chunk = chunks[index]
        if (chunk.state != currentState) {
            action(currentState, chunks.subList(start, end + 1).sumOf { it.weight })
            currentState = chunk.state
            start = index
        }
        end = index
    }
    // Handle the final chunk
    action(currentState, chunks.subList(start, end + 1).sumOf { it.weight })
}

@OverloadResolutionByLambdaReturnType
inline fun <T> Iterable<T>.sumOf(selector: (T) -> Float): Float {
    var sum: Float = 0.toFloat()
    for (element in this) {
        sum += selector(element)
    }
    return sum
}
