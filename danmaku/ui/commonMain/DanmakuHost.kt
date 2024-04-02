package me.him188.ani.danmaku.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import java.util.UUID

interface DanmakuHostState {
    @Stable
    val tracks: List<DanmakuTrackState>
}

/**
 * Creates a [DanmakuHostState].
 *
 * @param danmakuFlow 待展示的弹幕流.
 * 从这个流中 collect 到的每个 [Danmaku] 都会被分配到一个轨道上.
 * 若当时所有轨道都已满, 则会被丢弃.
 */
fun DanmakuHostState(
    danmakuFlow: Flow<Danmaku>,
): DanmakuHostState = DanmakuHostStateImpl(
    danmakuFlow,
)

/**
 * 容纳[弹幕轨道][DanmakuTrack]的 [Column]. 自动将弹幕分配到轨道上.
 *
 * @see DanmakuHostState
 */
@Composable
fun DanmakuHost(
    state: DanmakuHostState,
    modifier: Modifier = Modifier,
    config: DanmakuConfig = DanmakuConfig.Default,
) {
    Column(modifier.background(Color.Transparent)) {
        for (track in state.tracks) {
            DanmakuTrack(track, Modifier.fillMaxWidth(), config) {
                // fix height even if there is no danmaku showing
                // so that the second track will not move to the top of the screen when the first track is empty.
                danmaku(DummyDanmakuState, Modifier.alpha(0f), style = config.style)

                for (danmaku in track.visibleDanmaku) {
                    key(danmaku.danmaku.id) {
                        danmaku(danmaku, style = config.style)
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

internal object DummyDanmakuState : DanmakuState {
    override val danmaku: Danmaku = Danmaku(
        UUID.randomUUID().toString(), 0.0, "1",
        DanmakuLocation.NORMAL, "dummy", 0
    )
}

internal class DanmakuHostStateImpl(
    private val danmakuFlow: Flow<Danmaku>,
) : DanmakuHostState, AbstractViewModel() {
    override val tracks: List<DanmakuTrackState> = listOf(
        DanmakuTrackState(10),
        DanmakuTrackState(10),
        DanmakuTrackState(10),
        DanmakuTrackState(10),
        DanmakuTrackState(10)
    )

    override fun init() {
        super.init()
        launchInBackground {
            danmakuFlow.collect { danmaku ->
                tracks.firstOrNull { it.trySend(danmaku) }
            }
        }
    }
}


@Composable
internal expect fun PreviewDanmakuHost()
