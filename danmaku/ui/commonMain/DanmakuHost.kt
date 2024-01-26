package me.him188.ani.danmaku.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

interface DanmakuHostState {
    val visibleDanmaku: List<DanmakuState>

    @Composable
    fun AttachCleaner()
}

interface DanmakuState {
    @Stable
    val danmaku: Danmaku

    @Composable
    fun offsetStart(): Dp
}

class DanmakuStateImpl(
    override val danmaku: Danmaku
) : DanmakuState {
    @Composable
    override fun offsetStart(): Dp {
        val layoutDirection = LocalLayoutDirection.current

        val windowWidth = currentWindowSize().width
        var offset by remember {
            mutableStateOf(
                when (layoutDirection) {
                    LayoutDirection.Ltr -> windowWidth
                    LayoutDirection.Rtl -> 0.dp
                }
            )
        }
        val state = animateDpAsState(offset, animationSpec = tween(30_000, easing = LinearEasing))
        LaunchedEffect(true) {
            offset = when (layoutDirection) {
                LayoutDirection.Ltr -> (-2000).dp // definitely out of screen
                LayoutDirection.Rtl -> windowWidth + 2000.dp
            }
        }
        return state.value
    }
}

class DanmakuHostStateImpl(
    private val danmakuFlow: Flow<Danmaku>,
    currentProgress: Flow<Duration>,
    scope: CoroutineScope,
) : DanmakuHostState {
    override val visibleDanmaku: MutableList<DanmakuState> = SnapshotStateList()
    private val currentProgress =
        currentProgress.stateIn(scope, started = SharingStarted.WhileSubscribed(5000), 0.milliseconds)

    init {
        scope.launch(Dispatchers.Main) {
            danmakuFlow.collect { danmaku ->
                visibleDanmaku.add(DanmakuStateImpl(danmaku))
            }
        }
    }

    @Composable
    override fun AttachCleaner() {
        LaunchedEffect(true) {
            for (danmaku in visibleDanmaku) {
                // TODO: optimize danmaku visibility
                if (currentProgress.value - 30.seconds > danmaku.danmaku.time.seconds) {
                    visibleDanmaku.remove(danmaku)
                }
            }
            delay(1.seconds)
        }
    }
}

@Composable
fun DanmakuHost(
    state: DanmakuHostStateImpl,
    modifier: Modifier = Modifier,
) {
    state.AttachCleaner()
    Surface(color = Color.Transparent, modifier = modifier) {
        for (danmaku in state.visibleDanmaku) {
            Danmaku(danmaku)
        }
    }
}

@Composable
fun Danmaku(
    danmaku: DanmakuState,
    modifier: Modifier = Modifier,
) {
    Box(modifier.offset(x = danmaku.offsetStart()).wrapContentSize()) {
        Text(
            danmaku.danmaku.text,
            overflow = TextOverflow.Visible,
            maxLines = 1,
            softWrap = false,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
internal expect fun currentWindowSize(): DpSize


@Composable
internal expect fun PreviewDanmakuHost()

@Composable
internal fun PreviewDanmakuHostImpl() {
    val state = remember {
        DanmakuHostStateImpl(
            danmakuFlow = flowOf(
                Danmaku(0.0, "1", DanmakuLocation.NORMAL, "Hello, world!", 0),
                Danmaku(1.0, "1", DanmakuLocation.NORMAL, "Hello, world! 2", 0),
                Danmaku(2.0, "1", DanmakuLocation.NORMAL, "Hello, world! 3", 0),
                Danmaku(3.0, "1", DanmakuLocation.NORMAL, "Hello, world! 4", 0),
            ),
            currentProgress = emptyFlow(),
            scope = CoroutineScope(EmptyCoroutineContext),
        )
    }
    DanmakuHost(state, Modifier.width(300.dp).height(200.dp))
}