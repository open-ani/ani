package me.him188.ani.datasources.api.paging

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


interface PagedSourceContext {
    fun setTotalSize(size: Int)
}


@Suppress("FunctionName")
fun <T> SinglePagePagedSource(getAll: suspend PagedSourceContext.() -> Flow<T>): PagedSource<T> {
    return object : AbstractPageBasedPagedSource<T>(initialPage = 0) {
        private inline val self get() = this
        private val context = object : PagedSourceContext {
            override fun setTotalSize(size: Int) {
                self.setTotalSize(size)
            }
        }

        override suspend fun nextPageImpl(page: Int): List<T>? {
            if (currentPage.value == 0) {
                return getAll(context).toList().also {
                    setTotalSize(it.size)
                }
            }
            return null
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

    override fun skipToPage(page: Int) {
        if (finished.value) {
            return
        }
        while (true) {
            val value = currentPage.value
            if (value >= page) {
                return
            }
            if (currentPage.compareAndSet(value, page)) {
                return
            }
        }
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