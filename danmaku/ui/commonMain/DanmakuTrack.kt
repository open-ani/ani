package me.him188.ani.danmaku.ui

import androidx.annotation.UiThread
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.withFrameMillis
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.danmaku.api.DanmakuPresentation
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@Stable
class DanmakuState internal constructor(
    val danmaku: DanmakuPresentation,
    val initialOffset: Float = 0f,
) {
    /**
     * Layout width of the view in px, late-init by [onPlaced].
     *
     * Can be `-1` if not yet initialized.
     */
    internal var textWidth by mutableIntStateOf(-1)

    /**
     * Called when the danmaku is placed on the screen.
     */
    internal fun onPlaced(
        layoutCoordinates: LayoutCoordinates,
    ) {
        textWidth = layoutCoordinates.size.width
    }
}

@Stable
internal val DummyDanmakuState: DanmakuState = DanmakuState(
    DanmakuPresentation(
        Danmaku(
            UUID.randomUUID().toString(),
            "dummy",
            0L, "1",
            DanmakuLocation.NORMAL, "dummy", 0
        ),
        isSelf = false
    )
)

@Immutable
@Stable
class DanmakuProperties(
    /**
     * Shift of the danmaku to be considered as fully out of the screen.
     */
    val safeShift: Int = 20,
) {
    companion object {
        val Default = DanmakuProperties()
    }
}

class DanmakuTrackState(
    isPaused: State<Boolean>,
    private val maxCount: Int,
    private val danmakuProperties: DanmakuProperties = DanmakuProperties.Default,
) {
    private val channel = Channel<DanmakuPresentation>(1)

    /**
     * 尝试发送一条弹幕到这个轨道. 当轨道已满时返回 `false`.
     */
    fun trySend(danmaku: DanmakuPresentation): Boolean = channel.trySend(danmaku).isSuccess

    suspend fun send(danmaku: DanmakuPresentation) {
        channel.send(danmaku)
    }

    internal val isPaused by isPaused

    /**
     * Layout size of the track placed.
     */
    internal var trackSize: IntSize by mutableStateOf(IntSize.Zero)

    @Stable
    internal val visibleDanmaku: MutableList<DanmakuState> = SnapshotStateList()


    /**
     * 刚发送的还在屏幕右边边缘的弹幕
     */
    @Stable
    internal val startingDanmaku: MutableList<DanmakuState> = SnapshotStateList() // actually contains only one element

    /**
     * Called on every frame to update the state.
     */
    @UiThread
    internal fun update() {
        if (visibleDanmaku.size >= maxCount) { // `>` is impossible, just to be defensive
            return
        }
        if (startingDanmaku.isNotEmpty()) { // 有弹幕仍然在屏幕右边   
            return
        }

        val danmaku = channel.tryReceive().getOrNull() ?: return
        val state = DanmakuState(danmaku, initialOffset = offset)
        startingDanmaku.add(state)
        visibleDanmaku.add(state)
    }

    /**
     * Whether the animation has ever started, i.e. [animateMove] has been called at least once.
     * It will not be reset to `false` if it has started.
     */
    internal var animationStarted by mutableStateOf(false)

    /**
     * Animated offset of the danmaku track. Will be negative.
     *
     * Default value is `0` when [animateMove] is never called.
     * Note that value `0` may not always indicate an invalid value,
     * because the danmaku will need to move to a negative offset to be fully out of the screen.
     */
    var offset: Float by mutableFloatStateOf(0f)
        private set

    /**
     * Animate the danmaku from the end of the screen to the start.
     *
     * This function can be safely cancelled,
     * because it remembers the last [offset] of the danmaku.
     * However it does NOT re-animate it if it has already moved out of the screen.
     *
     * Returns when the animation has ended,
     * which means the danmaku has moved out of the screen (to the start).
     *
     * @param baseSpeed px/s
     */
    suspend fun animateMove(
        baseSpeed: Float,
    ) {
        val startTime = withFrameNanos { it }
        val speed = -baseSpeed / 1_000_000_000f // px/ns

        if (!animationStarted) {
            animationStarted = true
        }

        val startOffset = offset + trackSize.width

        while (true) {
            // Update offset on every frame
            withFrameNanos {
                val elapsed = it - startTime
                offset = startOffset + speed * elapsed
            }
        }
    }

}

abstract class DanmakuTrackScope {
    /**
     * Composes a Danmaku in the track.
     */
    @Composable
    fun danmaku(
        danmaku: DanmakuState,
        modifier: Modifier = Modifier,
        style: DanmakuStyle,
    ) {
        danmakuImpl(danmaku, style, modifier)
    }

    /**
     * @see DanmakuTrackScope.danmaku
     */
    @Composable
    internal abstract fun danmakuImpl(
        // need this because abstract composable cannot have defaults
        danmaku: DanmakuState,
        style: DanmakuStyle,
        modifier: Modifier,
    )
}

@Composable
fun DanmakuTrack(
    trackState: DanmakuTrackState,
    modifier: Modifier = Modifier,
    config: DanmakuConfig = DanmakuConfig.Default,
    content: @Composable DanmakuTrackScope.() -> Unit, // box scope
) {
    val configState by rememberUpdatedState(config)
    val safeSeparation by rememberUpdatedState(
        with(LocalDensity.current) {
            config.safeSeparation.toPx()
        }
    )

    val scope = remember(trackState) {
        object : DanmakuTrackScope() {
            @Composable
            override fun danmakuImpl(
                danmaku: DanmakuState,
                style: DanmakuStyle,
                modifier: Modifier
            ) {
                val layoutDirection by rememberUpdatedState(LocalLayoutDirection.current)

                // Late-init by [onPlaced]
                var positionInParent by remember { mutableStateOf(Offset.Zero) }
                val density by rememberUpdatedState(LocalDensity.current)

                // Automatically (re-)start animation also on configuration change
                LaunchedEffect(
                    danmaku.textWidth, // listen to settings change, because target distance will also change
                    trackState.isPaused,
                    configState.speed,
                    density
                ) {
                    if (trackState.trackSize == IntSize.Zero) {
                        return@LaunchedEffect // Not yet measured, i.e. 
                    }
                    if (trackState.isPaused) {
                        return@LaunchedEffect
                    }

                    // Below are two tasks to ensure the queueing of danmaku.
                    // We need the delays to calculate gently, because we need to ensure that the updating of offsets gets completed in every frame.
                    // Danmaku is moving on every frame, so [onPlace] is also called very frequently.
                    // If you do these tasks during [onPlace], you will read states and get it very slow. 
                    val delay = 0.1.seconds

                    // Remove the danmaku from the track when it is out of screen, 
                    // so that Track view will remove the Danmaku view from the layout.
                    launch {
                        while (isActive) {
                            val pos = positionInParent // in the DanmakuTrack
                            if (pos.x + danmaku.textWidth <= 0) {
                                // out of screen
                                trackState.visibleDanmaku.remove(danmaku)
                                return@launch
                            }
                            delay(delay)
                        }
                    }

                    // Remove the danmaku from [startingDanmaku] if it is fully visible on the screen (with [safeSeparation]),
                    // so that the track will receive the next danmaku and display it.
                    launch {
                        while (isActive) {
                            val pos = positionInParent // in the DanmakuTrack
                            if (positionInParent == Offset.Zero) {
                                // not yet placed
                                delay(delay)
                                continue
                            }
                            val isFullyVisible = if (layoutDirection == LayoutDirection.Ltr) {
                                pos.x + danmaku.textWidth + safeSeparation < trackState.trackSize.width
                            } else {
                                pos.x - safeSeparation > 0
                            }

                            if (isFullyVisible) {
                                trackState.startingDanmaku.remove(danmaku)
                                return@launch
                            }
                            delay(delay)
                        }
                    }
                }

                Box(
                    modifier
                        .alpha(if (trackState.animationStarted) 1f else 0f) // Don't use `danmaku.offset == 0`, see danmaku.offset comments.
                        .graphicsLayer {
                            translationX = -danmaku.initialOffset
                        }
                        .onPlaced { layoutCoordinates ->
                            danmaku.onPlaced(layoutCoordinates)
                            positionInParent = layoutCoordinates.positionInParent()
                        }
                        .wrapContentSize()
                ) {
                    DanmakuText(
                        danmaku,
                        style = style,
                    )
                }
            }
        }
    }

    LaunchedEffect(true) {
        while (isActive) {
            withFrameMillis {
                trackState.update()
            }
        }
    }

    LaunchedEffect(true, trackState.trackSize) {
        trackState.animateMove(config.speed)
    }

    BoxWithConstraints(modifier.onPlaced {
        trackState.trackSize = it.size
    }) {
        Box(Modifier.fillMaxWidth().graphicsLayer {
            translationX = trackState.offset
        }) {
            scope.content()
        }
    }
}

/**
 * The actual text of the danmaku.
 *
 * It is always white with black border.
 */
@Composable
fun DanmakuText(
    danmaku: DanmakuState,
    modifier: Modifier = Modifier,
    style: DanmakuStyle = DanmakuStyle.Default,
    baseStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    Box(modifier.alpha(style.alpha)) {
        // Black text with stronger stroke
        Text(
            danmaku.danmaku.danmaku.text,
            Modifier,
            overflow = TextOverflow.Clip,
            maxLines = 1,
            softWrap = false,
            style = baseStyle.merge(style.styleForBorder()),
            onTextLayout = onTextLayout
        )
        // Covered by a white, smaller text.
        // So the resulting look is a white text with black border.
        Text(
            danmaku.danmaku.danmaku.text,
            Modifier,
            overflow = TextOverflow.Clip,
            maxLines = 1,
            softWrap = false,
            style = baseStyle.merge(style.styleForText()),
            textDecoration = if (danmaku.danmaku.isSelf) TextDecoration.Underline else null,
        )
    }
}
