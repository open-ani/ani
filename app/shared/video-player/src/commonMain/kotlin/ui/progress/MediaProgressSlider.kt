package me.him188.ani.app.videoplayer.ui.progress

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import kotlinx.collections.immutable.ImmutableList
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.PlatformPopupProperties
import me.him188.ani.app.platform.isMobile
import me.him188.ani.app.ui.foundation.effects.onPointerEventMultiplatform
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.theme.aniDarkColorTheme
import me.him188.ani.app.ui.foundation.theme.slightlyWeaken
import me.him188.ani.app.ui.foundation.theme.weaken
import me.him188.ani.app.videoplayer.ui.state.Chapter
import me.him188.ani.app.videoplayer.ui.state.Chunk
import me.him188.ani.app.videoplayer.ui.state.ChunkState
import me.him188.ani.app.videoplayer.ui.state.MediaCacheProgressState
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import kotlin.math.roundToInt
import kotlin.math.roundToLong

internal const val TAG_PROGRESS_SLIDER_PREVIEW_POPUP = "ProgressSliderPreviewPopup"

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
    chapters: State<ImmutableList<Chapter>>,
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
    val chapters by chapters

    private var previewPositionRatio: Float by mutableFloatStateOf(Float.NaN)
    private val previewRequests = SnapshotStateList<Any>()

    fun setRequestPreview(requester: Any, isPreview: Boolean) {
        if (isPreview) {
            previewRequests.add(requester)
        } else {
            previewRequests.remove(requester)
        }

        if (previewRequests.isEmpty()) {
            finishPreview()
        }
    }

    val preview: Boolean by derivedStateOf {
        previewRequests.isNotEmpty()
    }

    /**
     * Sets the slider to move to the given position.
     * [onPreview] will be triggered.
     */
    fun previewPositionRatio(ratio: Float) {
        previewPositionRatio = ratio
        onPreview((totalDurationMillis * ratio).roundToLong())
    }

    /**
     * The ratio of the current display position to the total duration. Range is `0..1`
     */
    val displayPositionRatio by derivedStateOf {
        val previewPositionRatio = this.previewPositionRatio
        if (!previewPositionRatio.isNaN()) {
            return@derivedStateOf previewPositionRatio
        }

        val total = this.totalDurationMillis
        if (total == 0L) {
            return@derivedStateOf 0f
        }
        this.currentPositionMillis.toFloat() / total
    }

    fun finishPreview() {
        val ratio = this.previewPositionRatio
        if (ratio.isNaN()) return
        onPreviewFinished((ratio * totalDurationMillis).roundToLong())
        previewPositionRatio = Float.NaN
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
    val videoProperties by playerState.videoProperties.collectAsStateWithLifecycle()
    val totalDuration by remember {
        derivedStateOf {
            videoProperties?.durationMillis ?: 0L
        }
    }

    val onPreviewUpdated by rememberUpdatedState(onPreview)
    val onPreviewFinishedUpdated by rememberUpdatedState(onPreviewFinished)
    val chapters = playerState.chapters.collectAsStateWithLifecycle()
    return remember {
        MediaProgressSliderState(
            { currentPosition },
            { totalDuration },
            chapters,
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
    downloadingColor: Color = Color.Yellow,
    notAvailableColor: Color = aniDarkColorTheme().error.slightlyWeaken(),
    chapterColor: Color = aniDarkColorTheme().onSurface,
    previewTimeBackgroundColor: Color = aniDarkColorTheme().surface,
    previewTimeTextColor: Color = aniDarkColorTheme().onSurface,
    enabled: Boolean = true,
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

            Canvas(Modifier.matchParentSize()) {
                if (state.totalDurationMillis == 0L) return@Canvas
                state.chapters.forEach {
                    val percent = it.offsetMillis.toFloat().div(state.totalDurationMillis)
                    drawCircle(
                        color = chapterColor,
                        radius = 2.dp.toPx(),
                        center = Offset(size.width * percent, this.center.y),
                        blendMode = BlendMode.Src, // override background
                    )
                }
            }
        }

        var mousePosX by rememberSaveable { mutableStateOf(0f) }
        var thumbWidth by rememberSaveable { mutableIntStateOf(0) }
        var sliderWidth by rememberSaveable { mutableIntStateOf(0) }

        fun renderPreviewTime(previewTimeMillis: Long): String {
            state.chapters.find {
                previewTimeMillis in it.offsetMillis..<it.offsetMillis + it.durationMillis
            }?.let {
                val chapterName = if (it.name.isBlank()) "" else it.name + "\n"
                return chapterName + renderSeconds(
                    previewTimeMillis / 1000,
                    state.totalDurationMillis / 1000,
                ).substringBefore(" ")
            }

            return renderSeconds(previewTimeMillis / 1000, state.totalDurationMillis / 1000).substringBefore(" ")
        }

        val previewTimeText by remember {
            derivedStateOf {
                val containerWidth = sliderWidth - thumbWidth
                if (containerWidth == 0) { // avoid division by zero during preview or in a extremely small container
                    ""
                } else {
                    val percent = mousePosX.minus(thumbWidth / 2).div(containerWidth)
                        .coerceIn(0f, 1f)
                    val previewTimeMillis = state.totalDurationMillis.times(percent).toLong()

                    renderPreviewTime(previewTimeMillis)
                }
            }
        }
        val previewTimeOnThumb by remember(state) {
            derivedStateOf {
                val previewTimeMillis = state.totalDurationMillis.times(state.displayPositionRatio).toLong()

                renderPreviewTime(previewTimeMillis)
            }
        }
        val hoverInteraction = remember { MutableInteractionSource() }
        val isHovered by hoverInteraction.collectIsHoveredAsState() // works only for desktop
        var isPressed by remember { mutableStateOf(false) }
        val showPreviewTime by remember {
            derivedStateOf {
                isHovered || isPressed
            }
        }
        if (showPreviewTime) {
            ProgressSliderPreviewPopup(
                offsetX = { mousePosX.roundToInt() },
                previewTimeBackgroundColor = previewTimeBackgroundColor,
            ) {
                PreviewTimeText(previewTimeText, previewTimeTextColor)
            }
        }
        // draw thumb
        val interactionSource = remember { MutableInteractionSource() }
        Slider(
            value = state.displayPositionRatio,
            valueRange = 0f..1f,
            onValueChange = { state.previewPositionRatio(it) },
            interactionSource = interactionSource,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                    ),
                    enabled = true,
                    modifier = Modifier.onSizeChanged {
                        thumbWidth = it.width
                    },
                )
                if (state.preview) {
                    ProgressSliderPreviewPopup(
                        offsetX = { thumbWidth / 2 },
                        previewTimeBackgroundColor = previewTimeBackgroundColor,
                    ) {
                        PreviewTimeText(previewTimeOnThumb, previewTimeTextColor)
                    }
                }
            },
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
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().height(24.dp)
                .onSizeChanged {
                    sliderWidth = it.width
                }
                .hoverable(interactionSource = hoverInteraction)
                .onPointerEventMultiplatform(PointerEventType.Move) {
                    mousePosX = it.changes.firstOrNull()?.position?.x ?: return@onPointerEventMultiplatform
                }
                // for android
                .ifThen(Platform.currentPlatform.isMobile()) {
                    onPointerEventMultiplatform(PointerEventType.Press) {
                        isPressed = it.changes.firstOrNull()?.pressed ?: return@onPointerEventMultiplatform
                        mousePosX = it.changes.firstOrNull()?.position?.x ?: return@onPointerEventMultiplatform
                    }.onPointerEventMultiplatform(PointerEventType.Release) {
                        isPressed = it.changes.firstOrNull()?.pressed ?: return@onPointerEventMultiplatform
                    }
                },
        )
    }
}

@Composable
fun ProgressSliderPreviewPopup(
    offsetX: () -> Int,
    previewTimeBackgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val popupPositionProviderState = remember {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val anchor = IntRect(
                    offset = IntOffset(
                        offsetX(),
                        with(density) { -8.dp.toPx().toInt() },
                    ) + anchorBounds.topLeft,
                    size = IntSize.Zero,
                )
                val tooltipArea = IntRect(
                    IntOffset(
                        anchor.left - popupContentSize.width,
                        anchor.top - popupContentSize.height,
                    ),
                    IntSize(
                        popupContentSize.width * 2,
                        popupContentSize.height * 2,
                    ),
                )
                val position = Alignment.TopCenter.align(popupContentSize, tooltipArea.size, layoutDirection)

                return IntOffset(
                    x = (tooltipArea.left + position.x).coerceIn(0, windowSize.width - popupContentSize.width),
                    y = (tooltipArea.top + position.y).coerceIn(0, windowSize.height - popupContentSize.height),
                )
            }
        }
    }
    Popup(
        properties = PlatformPopupProperties(usePlatformInsets = false),
        popupPositionProvider = popupPositionProviderState,
    ) {
        Box(
            modifier = modifier
                .testTag(TAG_PROGRESS_SLIDER_PREVIEW_POPUP)
                .clip(shape = CircleShape)
                .background(previewTimeBackgroundColor)
                .animateContentSize(),
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}

@Composable
fun PreviewTimeText(
    text: String,
    previewTimeTextColor: Color,
) {
    ProvideTextStyle(MaterialTheme.typography.labelLarge) {
        Text(
            // 占位置
            text = text,
            Modifier.alpha(0f),
            fontFamily = FontFamily.Monospace,
        )
        Text(
            text = text,
            color = previewTimeTextColor,
            textAlign = TextAlign.Center,
        )
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
