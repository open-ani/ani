import androidx.compose.runtime.IntState
import androidx.compose.runtime.LongState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import me.him188.ani.app.ui.framework.runComposeStateTest
import me.him188.ani.app.ui.framework.takeSnapshot
import me.him188.ani.danmaku.ui.FloatingDanmakuTrack
import me.him188.ani.danmaku.ui.PositionedDanmakuState
import me.him188.ani.danmaku.ui.SizeSpecifiedDanmaku
import kotlin.test.Test
import kotlin.test.assertTrue

class FloatingDanmakuTrackTest {
    @Test
    fun `test`() = runComposeStateTest {
        val frameTimeNanosState = mutableLongStateOf(0)
        val presentDanmaku = mutableListOf<PositionedDanmakuState<TestFloatingDanmaku>>()
        // 轨道长度 1000px, 弹幕速度 100px/second
        val track = createFloatingDanmakuTrack(
            frameTimeNanosState = frameTimeNanosState, 
            trackWidth = mutableIntStateOf(1000),
            speedPxPerSecond = 100f,
            onRemoveDanmaku = { d -> presentDanmaku.removeAll { it.danmaku == d.danmaku } }
        ) 
        
        takeSnapshot()
        val danmaku = TestFloatingDanmaku(100)
        assertTrue(track.canPlace(danmaku, 0))
    }
}

private fun createFloatingDanmakuTrack(
    frameTimeNanosState: LongState,
    trackWidth: IntState,
    speedPxPerSecond: Float = 100f,
    safeSeparation: Float = 10f,
    onRemoveDanmaku: (PositionedDanmakuState<TestFloatingDanmaku>) -> Unit = { },
) = FloatingDanmakuTrack(
    trackIndex = 0, // 测试时没用
    frameTimeNanosState = frameTimeNanosState,
    trackWidth = trackWidth,
    trackHeight = mutableIntStateOf(50), // 测试时没用
    speedPxPerSecond = speedPxPerSecond,
    safeSeparation = safeSeparation,
    /*baseTextLength = 100f, // 如果不进行撞车测试, 那请设置 speedMultiplier 为 1f
    speedMultiplier = mutableFloatStateOf(1f),*/
    onRemoveDanmaku = onRemoveDanmaku
)

// 只要有长度就可以在 DanmakuTrack 里测量
private class TestFloatingDanmaku(
    override val danmakuWidth: Int
) : SizeSpecifiedDanmaku {
    // floating danmaku doesn't concern about danmaku height.
    override val danmakuHeight: Int = 0 
}