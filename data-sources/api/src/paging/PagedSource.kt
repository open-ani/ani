/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.datasources.api.paging

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A [SizedSource] that adds pagination support.
 * @see SizedSource
 */
interface PagedSource<out T> : SizedSource<T> {
    /**
     * 全部搜索结果, 以 [Flow] 形式提供, 惰性请求.
     */
    override val results: Flow<T>

    override val finished: StateFlow<Boolean>

    val currentPage: StateFlow<Int>

    override val totalSize: StateFlow<Int?>

    /**
     * 主动查询下一页. 当已经没有下一页时返回 `null`. 注意, 若有使用 [results], 主动操作 [nextPageOrNull] 将导致 [results] 会跳过该页.
     *
     * ### Coroutine Cancellation
     *
     * This function supports coroutine cancellation, and will **always** check for cancellation.
     *
     * When this coroutine is cancelled, [currentPage] is guaranteed to be left intact, i.e. not incremented.
     */
    suspend fun nextPageOrNull(): List<T>?

    /**
     * Update the page counter to the previous page if there is one.
     * Do nothing if there isn't.
     */
    fun backToPrevious()
}

inline fun <T, R> PagedSource<T>.map(crossinline transform: suspend (T) -> R): PagedSource<R> {
    val self = this
    return object : PagedSource<R> {
        override val results: Flow<R> by lazy {
            self.results.map {
                transform(it)
            }
        }
        override val finished: StateFlow<Boolean> get() = self.finished
        override val currentPage: StateFlow<Int> get() = self.currentPage
        override val totalSize: StateFlow<Int?> get() = self.totalSize

        override suspend fun nextPageOrNull(): List<R>? {
            val nextPageOrNull = self.nextPageOrNull()
            return try {
                nextPageOrNull?.map {
                    transform(it)
                }
            } catch (e: CancellationException) {
                self.backToPrevious() // reset page index
                throw e
            }
        }

        override fun backToPrevious() {
            self.backToPrevious()
        }
    }
}

interface PagedSourceContext {
    fun setTotalSize(size: Int)
}


@Suppress("FunctionName")
fun <T> SingleShotPagedSource(getAll: suspend PagedSourceContext.() -> Flow<T>): PagedSource<T> {
    return object : AbstractPageBasedPagedSource<T>() {
        private inline val self get() = this
        private val context = object : PagedSourceContext {
            override fun setTotalSize(size: Int) {
                self.setTotalSize(size)
            }
        }

        override suspend fun nextPageImpl(page: Int): List<T> {
            val paged = getAll(context)
            noMorePages()
            return paged.toList()
        }
    }
}

@Suppress("FunctionName")
fun <T> PageBasedPagedSource(
    initialPage: Int = 0,
    nextPageOrNull: suspend PagedSourceContext.(page: Int) -> Paged<T>?
): PagedSource<T> {
    @Suppress("UnnecessaryVariable", "RedundantSuppression") // two bugs...
    val nextPageOrNullImpl = nextPageOrNull
    return object : AbstractPageBasedPagedSource<T>() {
        override val currentPage: MutableStateFlow<Int> = MutableStateFlow(initialPage)
        private inline val self get() = this
        private val context = object : PagedSourceContext {
            override fun setTotalSize(size: Int) {
                self.setTotalSize(size)
            }
        }

        override suspend fun nextPageImpl(page: Int): List<T>? {
            val paged = nextPageOrNullImpl(context, page)
            if (paged == null) {
                noMorePages()
                return null
            }
            if (!paged.hasMore) {
                noMorePages()
            }
            return paged.page
        }
    }
}

abstract class AbstractPageBasedPagedSource<T>(
    initialPage: Int = 0,
) : PagedSource<T> {
    override val currentPage: MutableStateFlow<Int> = MutableStateFlow(initialPage)

    private val lock = Mutex()
    override val finished = MutableStateFlow(false)

    final override suspend fun nextPageOrNull(): List<T>? = lock.withLock {
        if (finished.value) {
            noMorePages()
            return null
        }
        val result = nextPageImpl(currentPage.value)
        // Impl note: after [nextPageImpl] there must not be any suspension points, 
        // otherwise we risk breaking the coroutine cancellation contract.
        // See comments of [nextPageOrNull] for details.

        if (result == null) {
            noMorePages()
            return null
        }
        if (!finished.value) {
            // CAS loop to increment page
            while (!currentPage.compareAndSet(currentPage.value, currentPage.value + 1)) {
                @Suppress("ControlFlowWithEmptyBody")
                for (i in 0..4) {
                    // some backoff
                }
                // retry
            }
        }
        return result
    }

    protected abstract suspend fun nextPageImpl(page: Int): List<T>?

    protected fun noMorePages() {
        if (finished.value) {
            return
        }
        finished.value = true
    }

    final override fun backToPrevious() {
        // This is actually not thread-safe, but it's fine for now

        if (currentPage.value == 0) {
            return
        }
        if (finished.value) {
            finished.value = false
        }

        while (!currentPage.compareAndSet(currentPage.value, currentPage.value - 1)) {
            @Suppress("ControlFlowWithEmptyBody")
            for (i in 0..4) {
                // some backoff
            }
            // retry
        }
    }

    final override val results: Flow<T> by lazy {
        flow {
            while (true) {
                val result = nextPageOrNull()
                if (result.isNullOrEmpty()) {
                    noMorePages()
                    return@flow
                }
                emitAll(result.asFlow())
                if (finished.value) { // [noMorePages] called within [nextPageOrNull]
                    return@flow
                }
            }
        }
    }

    final override val totalSize: MutableStateFlow<Int?> = MutableStateFlow(null)

    protected fun setTotalSize(size: Int) {
        totalSize.value = size
    }
}