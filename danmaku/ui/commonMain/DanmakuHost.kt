package me.him188.ani.danmaku.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

interface DanmakuHostState {
    val tracks: List<DanmakuTrackState>
}

/**
 * Remembers a [DanmakuHostState] that is bound to the lifecycle of current composable.
 *
 * @param danmakuFlow 待展示的弹幕流.
 * 从这个流中 collect 到的每个 [Danmaku] 都会被分配到一个轨道上.
 * 若当时所有轨道都已满, 则会被丢弃.
 */
@Composable
fun rememberDanmakuHostState(
    danmakuFlow: Flow<Danmaku>,
): DanmakuHostState {
    val uiScope = rememberCoroutineScope()
    return remember(danmakuFlow) {
        DanmakuHostStateImpl(danmakuFlow, uiScope)
    }
}

/**
 * 容纳[弹幕轨道][DanmakuTrack]的 [Column]. 自动将弹幕分配到轨道上.
 *
 * @see rememberDanmakuHostState
 */
@Composable
fun DanmakuHost(
    state: DanmakuHostState,
    modifier: Modifier = Modifier,
) {
    Column(modifier.background(Color.Transparent)) {
        for (track in state.tracks) {
            DanmakuTrack(track, Modifier.fillMaxWidth()) {
                for (danmaku in track.visibleDanmaku) {
                    key(danmaku.danmaku.id) {
                        danmaku(danmaku)
                    }
                }
            }
        }
    }
}


interface DanmakuState {
    @Stable
    val danmaku: Danmaku
}

class DanmakuHostStateImpl(
    private val danmakuFlow: Flow<Danmaku>,
    uiScope: CoroutineScope,
) : DanmakuHostState {
    override val tracks: List<DanmakuTrackState> = listOf(
        DanmakuTrackState(10),
        DanmakuTrackState(10),
        DanmakuTrackState(10),
        DanmakuTrackState(10),
        DanmakuTrackState(10)
    )

    init {
        uiScope.launch(Dispatchers.Main) {
            danmakuFlow.collect { danmaku ->
                tracks.firstOrNull { it.trySend(danmaku) }
            }
        }
    }
}

class DanmakuTrackState(
    private val maxCount: Int,
) {
    private val channel = Channel<Danmaku>(1)

    /**
     * 尝试发送一条弹幕到这个轨道. 当轨道已满时返回 `false`.
     */
    fun trySend(danmaku: Danmaku): Boolean = channel.trySend(danmaku).isSuccess

    @Stable
    internal val visibleDanmaku: MutableList<DanmakuState> = SnapshotStateList()


    /**
     * 刚发送的还在屏幕右边边缘的弹幕
     */
    internal val startingDanmaku: MutableList<Danmaku> = SnapshotStateList() // queue

    class DanmakuStateImpl(
        override val danmaku: Danmaku
    ) : DanmakuState

    internal fun update() {
        if (visibleDanmaku.size >= maxCount) { // `>` is impossible, just to be defensive
            return
        }
        if (startingDanmaku.isNotEmpty()) { // 有弹幕仍然在屏幕右边   
            return
        }

        val danmaku = channel.tryReceive().getOrNull() ?: return
        startingDanmaku.add(danmaku)
        visibleDanmaku.add(DanmakuStateImpl(danmaku))
    }
}

abstract class DanmakuTrackScope {
    @Composable
    internal abstract fun danmakuImpl(
        danmaku: DanmakuState,
        style: TextStyle,
    )
}

@Composable
fun DanmakuTrackScope.danmaku(
    danmaku: DanmakuState,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    danmakuImpl(danmaku, style)
}

@Composable
fun DanmakuTrack(
    state: DanmakuTrackState,
    modifier: Modifier = Modifier,
    separation: Dp = 32.dp,
    content: @Composable DanmakuTrackScope.() -> Unit,
) {
    var trackSize by remember { mutableStateOf(IntSize(0, 0)) }
    val separationPixel by rememberUpdatedState(
        with(LocalDensity.current) {
            separation.toPx()
        })
    val scope = remember(state) {
        object : DanmakuTrackScope() {
            /**
             * 计算弹幕的 offset, 从左到右, 自动适配 RTL.
             */
            @Composable
            private fun danmakuOffset(textWidth: Int): Int {
                val layoutDirection = LocalLayoutDirection.current

                val windowWidth = trackSize.width
                var offset by remember {
                    mutableStateOf(
                        when (layoutDirection) {
                            LayoutDirection.Ltr -> windowWidth
                            LayoutDirection.Rtl -> 0
                        }
                    )
                }
                val offsetState = animateIntAsState(offset, animationSpec = tween(10_000, easing = LinearEasing))
                LaunchedEffect(textWidth) {
                    if (textWidth != 0) {
                        offset = when (layoutDirection) {
                            LayoutDirection.Ltr -> -textWidth - 20 // definitely out of screen
                            LayoutDirection.Rtl -> windowWidth + textWidth + 20
                        }
                    }
                }
                return offsetState.value
            }

            @Composable
            override fun danmakuImpl(danmaku: DanmakuState, style: TextStyle) {
                var removed by remember { mutableStateOf(false) }
                var textWidth by remember { mutableStateOf(0) }

                val danmakuOffset = danmakuOffset(textWidth)

                Box(
                    Modifier
                        .offset {
                            IntOffset(x = danmakuOffset, y = 0)
                        }
                        .onPlaced { layoutCoordinates ->
                            textWidth = layoutCoordinates.size.width

                            val bounds = layoutCoordinates.positionInParent()
                            if (bounds.x + layoutCoordinates.size.width <= 0) {
                                // out of screen
                                state.visibleDanmaku.remove(danmaku)
                            }

                            if (!removed && bounds.x + layoutCoordinates.size.width + separationPixel < trackSize.width) {
                                removed = true
                                // in screen
                                state.startingDanmaku.remove(danmaku.danmaku)
                            }
                        }
                        .wrapContentSize()
                ) {
                    Text(
                        danmaku.danmaku.text,
                        overflow = TextOverflow.Visible,
                        maxLines = 1,
                        softWrap = false,
                        style = style.copy(
                            color = Color.Black,
                            drawStyle = Stroke(
                                miter = 10f,
                                width = 5f,
                                join = StrokeJoin.Round,
                            )
                        ),
                    )
                    Text(
                        danmaku.danmaku.text,
                        overflow = TextOverflow.Visible,
                        maxLines = 1,
                        softWrap = false,
                        style = style,
                    )
                }
            }
        }
    }

    LaunchedEffect(true) {
        while (isActive) {
            withFrameMillis {
                state.update()
            }
        }
    }

    BoxWithConstraints(modifier.onPlaced {
        trackSize = it.size
    }) {
        scope.content()
    }
}

@Composable
internal expect fun PreviewDanmakuHost()

@Composable
internal fun PreviewDanmakuHostImpl() {
    val state = rememberDanmakuHostState(
        danmakuFlow = flow {
            var counter = 0
            fun danmaku() = Danmaku("", 0.0, "1", DanmakuLocation.NORMAL, "Hello, world! ${++counter}", 0)

            emit(danmaku())
            emit(danmaku())
            emit(danmaku())
            while (true) {
                emit(danmaku())
                delay(Random.nextLong(50, 1000).milliseconds)
            }
        },
    )
    DanmakuHost(state, Modifier.fillMaxWidth().height(300.dp))
}
