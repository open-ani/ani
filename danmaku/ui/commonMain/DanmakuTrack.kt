package me.him188.ani.danmaku.ui

import androidx.annotation.UiThread
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt

class DanmakuState internal constructor(
    @Stable
    val danmaku: Danmaku,
    private val properties: DanmakuProperties = DanmakuProperties.Default,
) {
    // Whether the rhs edge is fully visible on the screen (in LTR, RTL vice versa)
    private var fullyVisible by mutableStateOf(false)

    /**
     * Layout width of the view in px, late-init by [onPlaced].
     */
    private var textWidth by mutableIntStateOf(-1)
    private var targetOffset by mutableIntStateOf(0)
    private var animationDurationMillis by mutableIntStateOf(Int.MAX_VALUE)

    /**
     * Whether [textWidth] has been measured.
     *
     * [textWidth] is measured when the Danmaku view is placed on the compose tree, calling [onPlaced].
     */
    internal val hasMeasured by derivedStateOf {
        textWidth != -1
    }

    /**
     * Animated offset of the danmaku.
     */
    val offset: State<Int>
        @Composable
        get() {
            val offsetState = animateIntAsState(
                this.targetOffset,
                animationSpec = tween(
                    durationMillis = this.animationDurationMillis,
                    easing = LinearEasing,
                ),
                label = if (currentAniBuildConfig.isDebug) "Danmaku offset for '${this.danmaku.text}'" else "Danmaku offset",
            )
            return offsetState
        }

    /**
     * Called when the danmaku is placed on the screen.
     * @param safeSeparation See [DanmakuConfig.safeSeparation]
     */
    internal fun onPlaced(
        track: DanmakuTrackState,
        layoutCoordinates: LayoutCoordinates,
        safeSeparation: Float,
    ) {
        textWidth = layoutCoordinates.size.width

        val pos = layoutCoordinates.positionInParent() // in the DanmakuTrack
        if (pos.x + textWidth <= 0) {
            // out of screen
            track.visibleDanmaku.remove(this)
        }

        if (!fullyVisible &&
            pos.x + textWidth + safeSeparation < track.trackSize.width
        ) {
            fullyVisible = true
            // in screen
            track.startingDanmaku.remove(danmaku)
        }
    }

    /**
     * Animate the danmaku to move from the end of the screen to the start.
     */
    suspend fun animateMove(
        screenWidth: Int,
        durationMillis: Int,
//        layoutDirection: LayoutDirection,
    ) {
        require(screenWidth != 0) { "screenWidth must not be 0" }
        val safeWidth = textWidth + properties.safeShift

        // To achieve constant speed:
        // speed = screenWidth / durationMillis = actualWidth / t
        // t = durationMillis * actualWidth / screenWidth
        animationDurationMillis =
            (durationMillis * abs((screenWidth + safeWidth).toDouble() / screenWidth)).roundToInt()

        if (targetOffset == 0) {
            targetOffset = screenWidth
//            targetOffset = when (layoutDirection) {
//                LayoutDirection.Ltr -> screenWidth
//                LayoutDirection.Rtl -> 0
//            }
        }

        withFrameMillis { it }
        withFrameMillis { }

        targetOffset = -safeWidth// definitely out of screen

//        targetOffset = when (layoutDirection) {
//            LayoutDirection.Ltr -> -(textWidth + properties.safeShift)// definitely out of screen
//            LayoutDirection.Rtl -> screenWidth + textWidth + properties.safeShift
//        }

        delay(animationDurationMillis.toLong())
    }

}

internal val DummyDanmakuState: DanmakuState = DanmakuState(
    Danmaku(
        UUID.randomUUID().toString(), 0.0, "1",
        DanmakuLocation.NORMAL, "dummy", 0
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
    private val maxCount: Int,
    private val danmakuProperties: DanmakuProperties = DanmakuProperties.Default,
) {
    private val channel = Channel<Danmaku>(1)

    /**
     * 尝试发送一条弹幕到这个轨道. 当轨道已满时返回 `false`.
     */
    fun trySend(danmaku: Danmaku): Boolean = channel.trySend(danmaku).isSuccess

    internal var isPaused by mutableStateOf(false)

    /**
     * Pauses the movement of danmaku.
     */
    fun pause() {
        isPaused = true
    }

    /**
     * Resumes the movement of danmaku. Danmaku will continue to move from where it was paused.
     */
    fun resume() {
        isPaused = false
    }


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
    internal val startingDanmaku: MutableList<Danmaku> = SnapshotStateList() // actually contains only one element

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
        startingDanmaku.add(danmaku)
        visibleDanmaku.add(DanmakuState(danmaku, danmakuProperties))
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
                // Automatically (re-)start animation also on configuration change
                LaunchedEffect(
                    danmaku.hasMeasured,
                    trackState.isPaused
                ) {
                    if (!danmaku.hasMeasured || trackState.trackSize == IntSize.Zero) {
                        return@LaunchedEffect // Not yet measured, i.e. 
                    }
                    danmaku.animateMove(
                        trackState.trackSize.width,
                        configState.durationMillis
                    )
                }

                val danmakuOffset by danmaku.offset

                Box(
                    modifier
                        .offset {
                            IntOffset(x = danmakuOffset, y = 0)
                        }
                        .onPlaced { layoutCoordinates ->
                            danmaku.onPlaced(trackState, layoutCoordinates, safeSeparation)
                        }
                        .wrapContentSize()
                ) {
                    DanmakuText(danmaku, style = style)
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

    BoxWithConstraints(modifier.onPlaced {
        trackState.trackSize = it.size
    }) {
        scope.content()
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
) {
    Box(modifier.alpha(style.alpha)) {
        // Black text with stronger stroke
        Text(
            danmaku.danmaku.text,
            Modifier,
            overflow = TextOverflow.Visible,
            maxLines = 1,
            softWrap = false,
            style = baseStyle.merge(style.styleForBorder()),
        )
        // Covered by a white, smaller text.
        // So the resulting look is a white text with black border.
        Text(
            danmaku.danmaku.text,
            Modifier,
            overflow = TextOverflow.Visible,
            maxLines = 1,
            softWrap = false,
            style = baseStyle.merge(style.styleForText()),
        )
    }
}
