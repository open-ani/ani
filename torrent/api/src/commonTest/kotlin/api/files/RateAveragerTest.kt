package me.him188.ani.app.torrent.api.files

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class RateAveragerTest {
    private val downloadedBytes = Channel<Long>(capacity = 1)
    private val ticker = Channel<Unit>(capacity = 1)
    private val flow = RateAverager(downloadedBytes, ticker)

    @Test
    fun `initial state`() = runTest {
        assertContentEquals(longArrayOf(0, 0, 0, 0, 0), flow.getWindowContent())
        assertEquals(-1, flow.currentIndex)
        assertEquals(0u, flow.counted)
    }

    @Test
    fun `initial state tick`() = runTest {
        assertEquals(0, tickAndRunPass())
    }

    @Test
    fun `initial state tick twice`() = runTest {
        assertEquals(0, tickAndRunPass())
        assertEquals(0, tickAndRunPass())
    }

    @Test
    fun `emit does not take effect unless tick`() = runTest {
        emitAndRunPass(1)
        assertEquals(1, flow.latestValue)
        assertContentEquals(longArrayOf(0, 0, 0, 0, 0), flow.getWindowContent())
        assertEquals(-1, flow.currentIndex)
        assertEquals(0u, flow.counted)

        tickAndRunPass()
        assertEquals(-1, flow.latestValue)
        assertContentEquals(longArrayOf(1, 0, 0, 0, 0), flow.getWindowContent())
        assertEquals(0, flow.currentIndex)
        assertEquals(1u, flow.counted)
    }

    @Test
    fun `swallow duplicated emit`() = runTest {
        emitAndRunPass(2)
        emitAndRunPass(1)
        assertEquals(1, flow.latestValue)
        assertContentEquals(longArrayOf(0, 0, 0, 0, 0), flow.getWindowContent())
        assertEquals(-1, flow.currentIndex)
        assertEquals(0u, flow.counted)

        emitAndRunPass(3)
        tickAndRunPass()
        assertEquals(-1, flow.latestValue)
        assertContentEquals(longArrayOf(3, 0, 0, 0, 0), flow.getWindowContent())
        assertEquals(0, flow.currentIndex)
        assertEquals(1u, flow.counted)
    }

    @Test
    fun `receive first value`() = runTest {
        emitAndRunPass(1)
        tickAndRunPass()
        assertContentEquals(longArrayOf(1, 0, 0, 0, 0), flow.getWindowContent())
        assertEquals(0, flow.currentIndex)
        assertEquals(1u, flow.counted)
    }

    @Test
    fun `first value tick`() = runTest {
        emitAndRunPass(100)
        tickAndRunPass()
        assertEquals(0, tickAndRunPass())
    }

    @Test
    fun `receive second value`() = runTest {
        emitAndRunPass(1)
        tickAndRunPass()
        emitAndRunPass(2)
        tickAndRunPass()
        assertContentEquals(longArrayOf(1, 2, 0, 0, 0), flow.getWindowContent())
        assertEquals(1, flow.currentIndex)
        assertEquals(2u, flow.counted)
    }

    @Test
    fun `two values tick`() = runTest {
        emitAndRunPass(100)
        tickAndRunPass()
        emitAndRunPass(200)
        assertEquals((200 - 100) / 2, tickAndRunPass())
    }

    @Test
    fun `two values tick then tick again`() = runTest {
        emitAndRunPass(100)
        tickAndRunPass()
        emitAndRunPass(200)
        assertEquals((200 - 100) / 2, tickAndRunPass())
        assertEquals((200 - 100) / 2, tickAndRunPass())
        assertEquals((200 - 100) / 3, tickAndRunPass())
    }

    @Test
    fun `receive 5 values`() = runTest {
        emitAndRunPass(100)
        tickAndRunPass()
        emitAndRunPass(200)
        tickAndRunPass()
        emitAndRunPass(300)
        tickAndRunPass()
        emitAndRunPass(400)
        tickAndRunPass()
        emitAndRunPass(500)
        tickAndRunPass()
        assertContentEquals(longArrayOf(100, 200, 300, 400, 500), flow.getWindowContent())
        assertEquals(4, flow.currentIndex)
        assertEquals(5u, flow.counted)
    }

    @Test
    fun `5 values tick`() = runTest {
        emitAndRunPass(100)
        tickAndRunPass()
        emitAndRunPass(200)
        tickAndRunPass()
        emitAndRunPass(300)
        tickAndRunPass()
        emitAndRunPass(400)
        tickAndRunPass()
        emitAndRunPass(500)
        assertEquals(400 / 5, tickAndRunPass())
    }

    @Test
    fun `5 values tick twice`() = runTest {
        emitAndRunPass(100)
        tickAndRunPass()
        emitAndRunPass(200)
        tickAndRunPass()
        emitAndRunPass(300)
        tickAndRunPass()
        emitAndRunPass(400)
        tickAndRunPass()
        emitAndRunPass(500)
        assertEquals((500 - 100) / 5, tickAndRunPass())
        assertEquals((500 - 100) / 5, tickAndRunPass())
        assertEquals((500 - 200) / 5, tickAndRunPass())
    }

    @Test
    fun `6 values tick`() = runTest {
        emitAndRunPass(100)
        tickAndRunPass()
        emitAndRunPass(200)
        tickAndRunPass()
        emitAndRunPass(300)
        tickAndRunPass()
        emitAndRunPass(400)
        tickAndRunPass()
        emitAndRunPass(500)
        tickAndRunPass()
        emitAndRunPass(600)
        assertEquals((600 - 200) / 5, tickAndRunPass())
        assertEquals((600 - 200) / 5, tickAndRunPass())
        assertEquals((600 - 300) / 5, tickAndRunPass())
    }

    @Test
    fun `5 values tick then one more value`() = runTest {
        emitAndRunPass(100)
        tickAndRunPass()
        emitAndRunPass(200)
        tickAndRunPass()
        emitAndRunPass(300)
        tickAndRunPass()
        emitAndRunPass(400)
        tickAndRunPass()
        emitAndRunPass(500)
        assertEquals((500 - 100) / 5, tickAndRunPass())
        emitAndRunPass(600)
        assertEquals((600 - 200) / 5, tickAndRunPass())
    }

    @Test
    fun `index rounds on the 5th element`() = runTest {
        emitAndRunPass(1)
        tickAndRunPass()
        emitAndRunPass(2)
        tickAndRunPass()
        emitAndRunPass(3)
        tickAndRunPass()
        emitAndRunPass(4)
        tickAndRunPass()
        emitAndRunPass(5)
        tickAndRunPass()
        assertEquals(4, flow.currentIndex)
    }

    @Test
    fun `index on the 6th element`() = runTest {
        emitAndRunPass(1)
        tickAndRunPass()
        emitAndRunPass(2)
        tickAndRunPass()
        emitAndRunPass(3)
        tickAndRunPass()
        emitAndRunPass(4)
        tickAndRunPass()
        emitAndRunPass(5)
        tickAndRunPass()
        emitAndRunPass(6)
        tickAndRunPass()
        assertEquals(0, flow.currentIndex)
    }

    private suspend fun TestScope.emitAndRunPass(value: Long) {
        emit(value)
        assertEquals(null, flow.runPass())
    }

    private suspend fun TestScope.emit(value: Long) {
        downloadedBytes.send(value)
        runCurrent()
    }

    private suspend fun TestScope.tickAndRunPass(): Long? {
        ticker.send(Unit)
        runCurrent()
        return flow.runPass()
    }
}
