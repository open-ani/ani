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
    val totalSize: Int?
}

private object EmptyPagedSource : PagedSource<Nothing> {
    override val currentPage: MutableStateFlow<Int> = MutableStateFlow(0)
    override val finished = MutableStateFlow(true)
    override suspend fun nextPageOrNull(): List<Nothing>? = null
    override fun skipToPage(page: Int) = Unit
    override fun backToPrevious() = Unit
    override val results: Flow<Nothing> = flow { }
    override val totalSize: MutableStateFlow<Int?> = MutableStateFlow(null)
}

fun emptyPagedSource(): PagedSource<Nothing> = EmptyPagedSource

@Suppress("FunctionName")
fun <T> SinglePagePagedSource(getAll: suspend PagedSourceContext.() -> Flow<T>): PagedSource<T> {
    return object : AbstractPageBasedPagedSource<T>(initialPage = 0) {
        private inline val self get() = this
        private val context = object : PagedSourceContext {
            override val totalSize: Int?
                get() = self.totalSize.value
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

/**
 * 基于自增页码的 [PagedSource].
 * [nextPageOrNull] 会携带当前请求的页码参数.
 *
 * 示例:
 *
 * ```
 * PageBasedPagedSource { page ->
 *     bangumiClient.episodes.getEpisodes(
 *         subjectId.toLong(),
 *         type,
 *         offset = page * 100,
 *         limit = 100
 *     )
 * }
 * ```
 */
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
            override val totalSize: Int?
                get() = self.totalSize.value
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
            if (paged.page.isEmpty()) { // to get same behavior as [SinglePagePagedSourceTest]
                return null
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