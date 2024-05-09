package me.him188.ani.danmaku.api

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.jupiter.api.Test
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


private class TimeBasedDanmakuSessionTest {
    fun create(
        sequence: Sequence<Danmaku>,
        shiftMillis: Long = 0,
    ): DanmakuSession = TimeBasedDanmakuSession.create(
        sequence, shiftMillis,
        samplePeriod = 0.milliseconds,
        coroutineContext = EmptyCoroutineContext,
    )

    @Test
    fun empty() {
        create(emptySequence())
    }

    @Test
    fun `before all`() = runTest {
        val instance = create(
            sequenceOf(
                dummyDanmaku(1.0),
                dummyDanmaku(2.0),
            )
        )
        val list = instance.at(flowOf(0.seconds)).toList()
        assertEquals(0, list.size)
    }

    @Test
    fun `match one`() = runTest {
        val instance = create(
            sequenceOf(
                dummyDanmaku(1.0),
                dummyDanmaku(2.0),
            )
        )
        val list = instance.at(flowOf(1.2.seconds)).toList()
        assertEquals(1, list.size)
    }

    @Test
    fun `match one equal`() = runTest {
        val instance = create(
            sequenceOf(
                dummyDanmaku(1.0),
                dummyDanmaku(2.0),
            )
        )
        val list = instance.at(flowOf(1.seconds)).toList()
        assertEquals(1, list.size)
    }

    @Test
    fun `match one again`() = runTest {
        val instance = create(
            sequenceOf(
                dummyDanmaku(1.0),
                dummyDanmaku(1.8),
                dummyDanmaku(2.0),
            )
        )
        val progress = MutableSharedFlow<Duration>()
        val danmakuFlow = instance.at(progress)
        val res = mutableListOf<Danmaku>()

        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            danmakuFlow.collect {
                res.add(it)
            }
        }

        assertEquals(0, res.size)

        progress.emit(0.seconds)
        yield()
        assertEquals(0, res.size)

        progress.emit(1.seconds)
        yield()
        assertEquals(1, res.size)

        // progress 1 again
        progress.emit(1.seconds)
        yield()
        assertEquals(1, res.size) // no more item

        job.cancel()
    }

    @Test
    fun `match two`() = runTest {
        val instance = create(
            sequenceOf(
                dummyDanmaku(1.0),
                dummyDanmaku(2.0),
            )
        )
        val list = instance.at(flowOf(2.seconds)).toList()
        assertEquals(2, list.size)
    }

    @Test
    fun `match continued`() = runTest {
        val instance = create(
            sequenceOf(
                dummyDanmaku(1.0),
                dummyDanmaku(1.8),
                dummyDanmaku(2.0),
            )
        )
        val progress = MutableSharedFlow<Duration>()
        val danmakuFlow = instance.at(progress)
        val res = mutableListOf<Danmaku>()

        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            danmakuFlow.collect {
                res.add(it)
            }
        }

        assertEquals(0, res.size)

        progress.emit(0.seconds)
        yield()
        assertEquals(0, res.size)

        progress.emit(1.seconds)
        yield()
        assertEquals(1, res.size)

        progress.emit(2.seconds)
        yield()
        assertEquals(3, res.size)

        job.cancel()
    }

    @Test
    fun `match seek back`() = runTest {
        val instance = create(
            sequenceOf(
                dummyDanmaku(1.0),
                dummyDanmaku(1.8),
                dummyDanmaku(2.0),
            )
        )
        val progress = MutableSharedFlow<Duration>()
        val danmakuFlow = instance.at(progress)
        val res = mutableListOf<Danmaku>()

        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            danmakuFlow.collect {
                res.add(it)
            }
        }

        assertEquals(0, res.size)

        progress.emit(0.seconds)
        yield()
        assertEquals(0, res.size)

        progress.emit(1.seconds)
        yield()
        assertEquals(1, res.size)

        progress.emit(2.seconds)
        yield()
        assertEquals(3, res.size)

        // let's seek back

        res.clear()

        progress.emit(1.seconds)
        yield()
        assertEquals(1, res.size)

        job.cancel()
    }

    private fun dummyDanmaku(timeSecs: Double, text: String = "$timeSecs") =
        dummyDanmaku((timeSecs * 1000).toLong(), text)

    private fun dummyDanmaku(timeMillis: Long, text: String = "$timeMillis") =
        Danmaku(text, "dummy", timeMillis, text, DanmakuLocation.NORMAL, text, 0)


}