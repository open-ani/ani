package me.him188.ani.danmaku.api

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/**
 * @see DanmakuSessionAlgorithmTest
 */
internal class TimeBasedDanmakuSessionTest {
    suspend fun create(
        sequence: Sequence<Danmaku>,
    ): DanmakuCollection = TimeBasedDanmakuSession.create(
        sequence, coroutineContext = currentCoroutineContext()[ContinuationInterceptor] ?: EmptyCoroutineContext,
    )

    @Test
    fun empty() = runTest {
        create(emptySequence())
    }

    @Test
    fun `before all`() = runTest {
        val instance = create(
            sequenceOf(
                dummyDanmaku(1.0),
                dummyDanmaku(2.0),
            ),
        )
        val list = instance.at(
            flowOf(0.seconds),
            danmakuRegexFilterList = flowOf(emptyList()),
        ).events.toList()
        assertEquals(0, list.size)
    }

    @Test
    fun `test filter`() = runTest {
        val danmakuList = listOf(
            dummyDanmaku(1.0, "1"),
            dummyDanmaku(2.0, "2"),
            dummyDanmaku(3.0, "3"),
        )
        val filteredList = TimeBasedDanmakuSession.filterList(danmakuList, listOf(".*"))
        assertEquals(emptyList(), filteredList)
    }

    private fun dummyDanmaku(timeSecs: Double, text: String = "$timeSecs") =
        dummyDanmaku((timeSecs * 1000).toLong(), text)

    private fun dummyDanmaku(timeMillis: Long, text: String = "$timeMillis") =
        Danmaku(text, "dummy", timeMillis, text, DanmakuLocation.NORMAL, text, 0)
}