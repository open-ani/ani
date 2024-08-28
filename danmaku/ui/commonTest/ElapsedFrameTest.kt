import me.him188.ani.danmaku.ui.new.ElapsedFrame
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
        elapsedFrame = elapsedFrame.addDelta(100)
        assertEquals(100, elapsedFrame.avg())
        elapsedFrame = elapsedFrame.addDelta(100)
        assertEquals(100, elapsedFrame.avg())
        elapsedFrame = elapsedFrame.addDelta(31)
        assertEquals(77, elapsedFrame.avg())
    }
}