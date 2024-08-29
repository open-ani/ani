import me.him188.ani.danmaku.ui.ElapsedFrame
import kotlin.test.Test
import kotlin.test.assertEquals

class ElapsedFrameTest {
    @Test
    fun test() {
        val elapsedFrame = ElapsedFrame(0, 0)
        assertEquals(0, elapsedFrame.avg())
    }

    @Test
    fun testInitialValue() {
        val elapsedFrame = ElapsedFrame(231, 3)
        assertEquals(77, elapsedFrame.avg())
    }
    
    @Test
    fun testIncremental() {
        var elapsedFrame = ElapsedFrame(0, 0)
        elapsedFrame += 100
        assertEquals(100, elapsedFrame.avg())
        elapsedFrame += 100
        assertEquals(100, elapsedFrame.avg())
        elapsedFrame += 31
        assertEquals(77, elapsedFrame.avg())
    }
}