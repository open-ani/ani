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
        assertContentEquals(longArrayOf(0, 0, 0, 0, 0), flow.window)
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
    fun `receive first value`() = runTest {
        emitAndRunPass(1)
        assertContentEquals(longArrayOf(1, 0, 0, 0, 0), flow.window)
        assertEquals(0, flow.currentIndex)
        assertEquals(1u, flow.counted)
    }

    @Test
    fun `first value tick`() = runTest {
        emitAndRunPass(100)
        assertEquals(100, tickAndRunPass())
    }

    @Test
    fun `receive second value`() = runTest {
        emitAndRunPass(1)
        emitAndRunPass(2)
        assertContentEquals(longArrayOf(1, 2, 0, 0, 0), flow.window)
        assertEquals(1, flow.currentIndex)
        assertEquals(2u, flow.counted)
    }

    @Test
    fun `two values tick`() = runTest {
        emitAndRunPass(100)
        emitAndRunPass(200)
        assertEquals((200 - 100) / 2, tickAndRunPass())
    }

    @Test
    fun `receive 5 values`() = runTest {
        emitAndRunPass(100)
        emitAndRunPass(200)
        emitAndRunPass(300)
        emitAndRunPass(400)
        emitAndRunPass(500)
        assertContentEquals(longArrayOf(100, 200, 300, 400, 500), flow.window)
        assertEquals(4, flow.currentIndex)
        assertEquals(5u, flow.counted)
    }

    @Test
    fun `5 values tick`() = runTest {
        emitAndRunPass(100)
        emitAndRunPass(200)
        emitAndRunPass(300)
        emitAndRunPass(400)
        emitAndRunPass(500)
        assertEquals(400 / 5, tickAndRunPass())
    }

    @Test
    fun `5 values tick twice`() = runTest {
        emitAndRunPass(100)
        emitAndRunPass(200)
        emitAndRunPass(300)
        emitAndRunPass(400)
        emitAndRunPass(500)
        assertEquals(400 / 5, tickAndRunPass())
        assertEquals(400 / 5, tickAndRunPass())
    }

    @Test
    fun `6 values tick`() = runTest {
        emitAndRunPass(100)
        emitAndRunPass(200)
        emitAndRunPass(300)
        emitAndRunPass(400)
        emitAndRunPass(500)
        emitAndRunPass(600)
        assertEquals((600 - 200) / 5, tickAndRunPass())
        assertEquals((600 - 200) / 5, tickAndRunPass())
    }

    @Test
    fun `5 values tick then one more value`() = runTest {
        emitAndRunPass(100)
        emitAndRunPass(200)
        emitAndRunPass(300)
        emitAndRunPass(400)
        emitAndRunPass(500)
        assertEquals((500 - 100) / 5, tickAndRunPass())
        emitAndRunPass(600)
        assertEquals((600 - 200) / 5, tickAndRunPass())
    }

    @Test
    fun `index rounds on the 5th element`() = runTest {
        emitAndRunPass(1)
        emitAndRunPass(2)
        emitAndRunPass(3)
        emitAndRunPass(4)
        emitAndRunPass(5)
        assertEquals(4, flow.currentIndex)
    }

    @Test
    fun `index on the 6th element`() = runTest {
        emitAndRunPass(1)
        emitAndRunPass(2)
        emitAndRunPass(3)
        emitAndRunPass(4)
        emitAndRunPass(5)
        emitAndRunPass(6)
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
