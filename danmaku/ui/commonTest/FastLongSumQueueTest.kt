import me.him188.ani.danmaku.ui.FastLongSumQueue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FastLongSumQueueTest {
    @Test
    fun `test init calculate`() {
        val queue = FastLongSumQueue(1)
        assertEquals(0, queue.avg())
    }

    @Test
    fun `test calculate with queue size = 1`() {
        val queue = FastLongSumQueue(1)
        
        queue += 10L
        assertEquals(10L, queue.avg())
        
        queue += 11L
        assertEquals(11L, queue.avg())
    }

    @Test
    fun `test calculate with queue size = 2`() {
        val queue = FastLongSumQueue(2)
        
        queue += 10L
        assertEquals(10L, queue.avg())

        queue += 12L
        assertEquals(11L, queue.avg())
        
        queue += 14L
        assertEquals(13L, queue.avg())
    }

    @Test
    fun `test calculate`() {
        val queue = FastLongSumQueue(6)
        
        queue += 10L
        assertEquals(10L, queue.avg())
        
        queue += 20L
        assertEquals(15L, queue.avg())
        
        queue += 30L
        assertEquals(20L, queue.avg())

        queue += 40L
        assertEquals(25L, queue.avg())

        queue += 50L
        assertEquals(30L, queue.avg())

        queue += 30L
        assertEquals(30L, queue.avg())

        queue += -30L
        assertEquals(23L, queue.avg())
    }
    
    @Test
    fun `test fail`() {
        assertFailsWith<IllegalArgumentException> { FastLongSumQueue(0) }
    }
}