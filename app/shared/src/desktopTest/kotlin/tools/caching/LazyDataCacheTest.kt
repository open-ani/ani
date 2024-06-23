package me.him188.ani.app.tools.caching

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.paging.SinglePagePagedSource
import me.him188.ani.utils.coroutines.cancellableCoroutineScope
import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class LazyDataCacheTest {

    ///////////////////////////////////////////////////////////////////////////
    // Lazy loading behavior
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `source not created on cache query`() = runTest {
        var requested = false
        val cache = LazyDataCache(
            {
                requested = true
                SinglePagePagedSource {
                    flowOf(1, 2, 3)
                }
            },
        )

        assertEquals(false, requested)
        cache.cachedDataFlow
        assertEquals(false, requested)
        val job = launch(start = CoroutineStart.UNDISPATCHED) { cache.cachedDataFlow.collect() }
        assertEquals(false, requested)
        job.cancel()

        cache.allDataFlow
        assertEquals(false, requested)
    }

    @Test
    fun `source not created on mutate`() = runTest {
        var requested = false
        val cache = LazyDataCache(
            {
                requested = true
                SinglePagePagedSource {
                    flowOf(1, 2, 3)
                }
            },
        )

        assertEquals(false, requested)
        cache.mutate {
            update { listOf(1, 2) }
        }
        assertEquals(false, requested)
    }

    @Test
    fun `source created on invalidate`() = runTest {
        var requested = false
        val cache = LazyDataCache(
            {
                requested = true
                SinglePagePagedSource {
                    flowOf(1, 2, 3)
                }
            },
        )

        assertEquals(false, requested)
        cache.invalidate()
        assertEquals(false, requested)
    }

    @Test
    fun `source created on refresh`() = runTest {
        var requested = false
        val cache = LazyDataCache(
            {
                requested = true
                SinglePagePagedSource {
                    flowOf(1, 2, 3)
                }
            },
        )

        assertEquals(false, requested)
        cache.refresh(RefreshOrderPolicy.REPLACE)
        assertEquals(true, requested)
    }

    @Test
    fun `source created on requestMore`() = runTest {
        var requested = false
        val cache = LazyDataCache(
            {
                requested = true
                SinglePagePagedSource {
                    flowOf(1, 2, 3)
                }
            },
        )

        assertEquals(false, requested)
        cache.requestMore()
        assertEquals(true, requested)
    }

    @Disabled
    @Test
    fun `source created on allData`() = runTest {
        var requested = false
        val cache = LazyDataCache(
            {
                requested = true
                SinglePagePagedSource {
                    flowOf(1, 2, 3)
                }
            },
        )

        assertEquals(false, requested)
        cache.allDataFlow.first()
        cache.allDataFlow.first() // avoid fluctuation
        assertEquals(true, requested)
    }

    ///////////////////////////////////////////////////////////////////////////
    // cachedData
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `cachedData updates`() = runTest {
        val cache = LazyDataCache(
            {
                PageBasedPagedSource {
                    when (it) {
                        0 -> Paged(listOf(1, 2))
                        1 -> Paged(listOf(3))
                        else -> null
                    }
                }
            },
        )

        assertEquals(listOf(), cache.cachedDataFlow.first())
        cache.requestMore()
        assertEquals(listOf(1, 2), cache.cachedDataFlow.first())
        cache.requestMore()
        assertEquals(listOf(1, 2, 3), cache.cachedDataFlow.first())
    }

    ///////////////////////////////////////////////////////////////////////////
    // allData
    ///////////////////////////////////////////////////////////////////////////

    @Disabled
    @Test
    fun `allData emits initial empty list`() = runTest {
        val cache = LazyDataCache(
            {
                SinglePagePagedSource {
                    flowOf(1, 2, 3)
                }
            },
        )

        assertEquals(emptyList(), cache.allDataFlow.first())
    }

    @Disabled
    @Test
    fun `allData emits first page`() = runTest {
        val cache = LazyDataCache(
            {
                SinglePagePagedSource {
                    flowOf(1, 2, 3)
                }
            },
        )

        assertEquals(listOf(1, 2, 3), cache.allDataFlow.drop(1).first())
    }

    @Disabled // allDataFlow 是异步的, 有时候 test 会判断错误
    @Test
    fun `allData emits second page`() = runTest {
        val cache = LazyDataCache(
            {
                PageBasedPagedSource {
                    when (it) {
                        0 -> Paged(listOf(1, 2))
                        1 -> Paged(listOf(3))
                        else -> null
                    }
                }
            },
        )

        assertEquals(listOf(), cache.allDataFlow.first())
        cache.allDataFlow.drop(1).first().let {
            assertTrue {
                it == listOf(1, 2) || it == listOf(1, 2, 3) // 'race'
            }
        }
        cache.allDataFlow.first {
            it == listOf(1, 2, 3)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // isCompleted
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `isCompleted has initial false`() = runTest {
        val cache = LazyDataCache(
            {
                SinglePagePagedSource {
                    flowOf(1, 2, 3)
                }
            },
        )

        assertEquals(false, cache.isCompleted.first())
    }

    @Test
    fun `isCompleted is true after source completes`() = runTest {
        val cache = LazyDataCache(
            {
                SinglePagePagedSource {
                    flowOf(1, 2, 3)
                }
            },
        )

        cache.requestMore()
        cache.requestMore() // SinglePagePagedSource lazily completes
        assertEquals(true, cache.isCompleted.first())
    }

    ///////////////////////////////////////////////////////////////////////////
    // Thread safety
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `high contention`() = runTest {
        val cache = LazyDataCache(
            {
                PageBasedPagedSource {
                    if (it < 10000) {
                        Paged(listOf(it))
                    } else null
                }
            },
        )

        // 不断刷新和获取. 由于数据源一定按页数输出, 所以数据一定有序且连续
        fun checkList(ints: List<Int>) {
            if (ints.isEmpty()) {
                return
            }
            var nextExpected = ints.first() + 1 // 我们会定义 set 为 emptyList, 所以第一个元素可能不是 0
            for (int in ints.asSequence().drop(1)) {
                if (int != nextExpected) {
                    fail("Invalid list: $ints")
                }
                nextExpected = int + 1
            }
        }

        cancellableCoroutineScope {
            val count = MutableStateFlow(0)
            suspend fun notify() {
                while (!count.compareAndSet(count.value, count.value + 1)) {
                    yield()
                }
            }
            repeat(3) {
                launch(start = CoroutineStart.UNDISPATCHED) {
                    cache.cachedDataFlow.collect {
                        checkList(it)
                    }
                }
                launch(start = CoroutineStart.UNDISPATCHED) {
                    cache.allDataFlow.collect {
                        checkList(it)
                    }
                }
                launch(start = CoroutineStart.UNDISPATCHED) {
                    cache.mutate {
                        update { listOf() } // 注意, 这跟 invalidate 的实现是不同的, 是有必要的
                    }
                }
            }
            repeat(2) {
                launch {
                    repeat(100000) {
                        cache.requestMore()
                    }
                    notify()
                }
            }
            launch {
                repeat(1000) {
                    yield()
                    cache.invalidate()
                }
                notify()
            }
            count.first { it == 3 }
            cancelScope()
        }
    }

}