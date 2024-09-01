import androidx.compose.runtime.IntState
import androidx.compose.runtime.LongState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.test.TestScope
import me.him188.ani.app.ui.framework.runComposeStateTest
import me.him188.ani.app.ui.framework.takeSnapshot
import me.him188.ani.danmaku.ui.FloatingDanmakuTrack
import me.him188.ani.danmaku.ui.PositionedDanmakuState
import me.him188.ani.danmaku.ui.SizeSpecifiedDanmaku
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

class FloatingDanmakuTrackTest {
    @Test
    fun `test basic`() = runComposeStateTest {
        val frameTimeNanosState = mutableLongStateOf(0)
        var frameTimeNanos by frameTimeNanosState
        
        val presentDanmaku = mutableListOf<PositionedDanmakuState<TestFloatingDanmaku>>()
        // 轨道长度 1000px, 弹幕速度 100px/second
        val track = createFloatingDanmakuTrack(
            frameTimeNanosState = frameTimeNanosState, 
            trackWidth = mutableIntStateOf(1000),
            speedPxPerSecond = 100f,
            onRemoveDanmaku = { d -> presentDanmaku.removeAll { it.danmaku == d.danmaku } }
        )

        // 现在轨道没弹幕，放一个
        val danmaku = TestFloatingDanmaku(100)
        val positioned = track.tryPlace(danmaku)
        // 一定可以放
        assertNotNull(positioned)
        presentDanmaku.add(positioned)
        assertEquals(danmaku, positioned.danmaku)
        assertEquals(PositionedDanmakuState.NOT_PLACED, positioned.placeFrameTimeNanos)

        // 模拟在 UI 帧中放置弹幕
        positioned.placeFrameTimeNanos = frameTimeNanos
        snapshot(track, positioned)

        // 在轨道最右侧
        assertEquals(1000f, positioned.x) 
        assertEquals(1, presentDanmaku.size)

        // UI 帧现在是 300ms
        frameTimeNanos = 300L.ms2ns
        snapshot(track, positioned)
        assertEquals(970f, positioned.x)

        // UI 帧现在是 3000ms
        frameTimeNanos = 3000L.ms2ns
        snapshot(track, positioned)
        assertEquals(700f, positioned.x)

        // UI 帧现在是 10000ms, 这条弹幕左侧刚好在轨道左侧
        frameTimeNanos = 10000L.ms2ns
        snapshot(track, positioned)
        assertEquals(0f, positioned.calculatePosX())

        // 弹幕已完全运动过去
        frameTimeNanos = 11000L.ms2ns
        snapshot(track, positioned)
        // 应该被移除
        assertEquals(0, presentDanmaku.size) 
    }
    
    @Test
    fun `test placement`() = runComposeStateTest {
        val frameTimeNanosState = mutableLongStateOf(0)
        var frameTimeNanos by frameTimeNanosState
        
        val presentDanmaku = mutableListOf<PositionedDanmakuState<TestFloatingDanmaku>>()
        // 轨道长度 1000px, 弹幕速度 100px/second
        val track = createFloatingDanmakuTrack(
            frameTimeNanosState = frameTimeNanosState,
            trackWidth = mutableIntStateOf(1000),
            speedPxPerSecond = 100f,
            onRemoveDanmaku = { d -> presentDanmaku.removeAll { it.danmaku == d.danmaku } }
        )
        
        val danmaku1 = TestFloatingDanmaku(100)
        val positioned1 = track.tryPlace(danmaku1)
        assertNotNull(positioned1) // 一定可以放
        presentDanmaku.add(positioned1)
        assertEquals(danmaku1, positioned1.danmaku)
        assertEquals(PositionedDanmakuState.NOT_PLACED, positioned1.placeFrameTimeNanos)
        
        // 有未确定放置时间的弹幕，没办法再放一个未确定时间的弹幕
        assertNull(track.tryPlace(TestFloatingDanmaku(50))) 
        // 无法放到轨道右侧边缘之外
        assertNull(track.tryPlace(TestFloatingDanmaku(50), 1000L.ms2ns))

        positioned1.placeFrameTimeNanos = frameTimeNanos
        snapshot(track, positioned1)
        
        // 运动 10000 ms, 现在 positioned1 应该在轨道左侧
        frameTimeNanos = 10000L.ms2ns
        snapshot(track, positioned1)
        assertEquals(0f, positioned1.x)

        // 放到帧时间 2000L 的位置
        // 模拟 repopulate 指定放置时间的操作
        val danmaku2 = TestFloatingDanmaku(50)
        val positioned2 = track.tryPlace(danmaku2, 2000L.ms2ns)
        assertNotNull(positioned2)
        presentDanmaku.add(positioned2)
        assertEquals(danmaku2, positioned2.danmaku)
        assertEquals(2000L.ms2ns, positioned2.placeFrameTimeNanos)
        
        snapshot(track, positioned1, positioned2)
        assertEquals(200f, positioned2.x)

        // 放一个未指定放置时间的弹幕
        val danmaku3 = TestFloatingDanmaku(200)
        val positioned3 = track.tryPlace(danmaku3)
        assertNotNull(positioned3)
        presentDanmaku.add(positioned3)
        assertEquals(danmaku3, positioned3.danmaku)

        // 模拟在 UI 帧中放置弹幕
        positioned3.placeFrameTimeNanos = frameTimeNanos
        snapshot(track, positioned1, positioned2, positioned3)
        
        // UI帧运动 50ms
        frameTimeNanos += 1500L.ms2ns
        snapshot(track, positioned1, positioned2, positioned3)
        
        // positioned1 在 UI 帧运动 150ms 后应该消失
        assertEquals(2, presentDanmaku.size)
        assertEquals(850f, positioned3.x)
        // positioned3 还没完全显示出来，没办法再放未确定放置时间的弹幕
        assertNull(track.tryPlace(TestFloatingDanmaku(100)))
        
        // 再运动 15000ms，所有弹幕都应该消失了
        frameTimeNanos += 15000L.ms2ns
        snapshot(track, positioned2, positioned3)
        assertEquals(0, presentDanmaku.size)
    }
}

private val Long.ms2ns get() = milliseconds.inWholeNanoseconds

private suspend fun TestScope.snapshot(
    track: FloatingDanmakuTrack<TestFloatingDanmaku>,
    vararg positioned: PositionedDanmakuState<TestFloatingDanmaku>
) {
    takeSnapshot()
    positioned.forEach { it.calculatePos() }
    track.tick()
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