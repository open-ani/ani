package me.him188.ani.datasources.api.paging

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SinglePagePagedSourceTest {
    @Test
    fun `initial page is 0`() {
        val source = SinglePagePagedSource {
            flowOf(1, 2, 3)
        }

        assertEquals(0, source.currentPage.value)
    }

    @Test
    fun `initial finished is false`() {
        val source = SinglePagePagedSource {
            flowOf(1, 2, 3)
        }

        assertEquals(false, source.finished.value)
    }

    @Test
    fun `get first page`() = runTest {
        val source = SinglePagePagedSource {
            flowOf(1, 2, 3)
        }

        assertEquals(listOf(1, 2, 3), source.nextPageOrNull())
    }

    @Test
    fun `get second page returns null`() = runTest {
        val source = SinglePagePagedSource {
            flowOf(1, 2, 3)
        }

        source.nextPageOrNull()
        assertEquals(null, source.nextPageOrNull())
    }

    @Test
    fun `don't finish after first page`() = runTest {
        val source = SinglePagePagedSource {
            flowOf(1, 2, 3)
        }

        assertEquals(listOf(1, 2, 3), source.nextPageOrNull())
        assertEquals(false, source.finished.value)
    }

    @Test
    fun `finish after second nextPageOrNull`() = runTest {
        val source = SinglePagePagedSource {
            flowOf(1, 2, 3)
        }

        source.nextPageOrNull()
        source.nextPageOrNull()

        assertEquals(true, source.finished.value)
    }

    @Test
    fun `collect results`() = runTest {
        val source = SinglePagePagedSource {
            flowOf(1, 2, 3)
        }

        assertEquals(listOf(1, 2, 3), source.results.toList())
    }

    @Test
    fun `finished after results completed`() = runTest {
        val source = SinglePagePagedSource {
            flowOf(1, 2, 3)
        }

        assertEquals(listOf(1, 2, 3), source.results.toList())
        assertEquals(true, source.finished.value)
    }

    @Test
    fun `size not known initially`() = runTest {
        val source = SinglePagePagedSource {
            flowOf(1, 2, 3)
        }

        assertEquals(null, source.totalSize.value)
    }

    @Test
    fun `size known after first page`() = runTest {
        val source = SinglePagePagedSource {
            flowOf(1, 2, 3)
        }

        source.nextPageOrNull()
        assertEquals(3, source.totalSize.value)
    }

    @Test
    fun `skip to page 1 then empty`() = runTest {
        val source = SinglePagePagedSource {
            flowOf(1, 2, 3)
        }

        source.skipToPage(1)
        assertEquals(null, source.nextPageOrNull())
        assertEquals(null, source.totalSize.value)
        assertEquals(true, source.finished.value)
    }

    @Test
    fun `back to previous`() = runTest {
        val source = SinglePagePagedSource {
            flowOf(1, 2, 3)
        }

        assertEquals(listOf(1, 2, 3), source.nextPageOrNull())
        source.backToPrevious()
        assertEquals(0, source.currentPage.value)
        assertEquals(3, source.totalSize.value)
        assertEquals(false, source.finished.value)

        assertEquals(listOf(1, 2, 3), source.nextPageOrNull())
        assertEquals(1, source.currentPage.value)
        assertEquals(false, source.finished.value)

        assertEquals(null, source.nextPageOrNull())
        assertEquals(1, source.currentPage.value)
        assertEquals(true, source.finished.value)
    }

    @Test
    fun `back to previous after finish`() = runTest {
        val source = SinglePagePagedSource {
            flowOf(1, 2, 3)
        }

        assertEquals(listOf(1, 2, 3), source.nextPageOrNull())
        assertEquals(null, source.nextPageOrNull())
        source.backToPrevious()
        assertEquals(0, source.currentPage.value)
        assertEquals(3, source.totalSize.value)
        assertEquals(false, source.finished.value)

        assertEquals(listOf(1, 2, 3), source.nextPageOrNull())
        assertEquals(1, source.currentPage.value)
        assertEquals(false, source.finished.value)

        assertEquals(null, source.nextPageOrNull())
        assertEquals(1, source.currentPage.value)
        assertEquals(true, source.finished.value)
    }
}