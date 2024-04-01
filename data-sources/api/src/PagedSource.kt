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

package me.him188.ani.datasources.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 一个搜索请求.
 *
 * **Stateful.** [PagedSource] 会持有当前查询状态信息, 例如当前页码.
 */
interface PagedSource<out T> {
    /**
     * 全部搜索结果, 以 [Flow] 形式提供, 惰性请求.
     */
    val results: Flow<T>

    val finished: StateFlow<Boolean>

    /**
     * 主动查询下一页. 当已经没有下一页时返回 `null`. 注意, 若有使用 [results], 主动操作 [nextPageOrNull] 将导致 [results] 会跳过该页.
     */
    suspend fun nextPageOrNull(): List<T>?
}

suspend inline fun PagedSource<*>.awaitFinished() {
    this.finished.filter { it }.first()
}

inline fun <T, R> PagedSource<T>.map(crossinline transform: suspend (T) -> R): PagedSource<R> {
    val self = this
    return object : PagedSource<R> {
        override val results: Flow<R> = self.results.map {
            transform(it)
        }
        override val finished: StateFlow<Boolean> get() = self.finished
        override suspend fun nextPageOrNull(): List<R>? {
            return self.nextPageOrNull()?.map {
                transform(it)
            }
        }
    }
}


@Suppress("FunctionName")
fun <T> SingleShotPagedSource(getAll: suspend () -> Flow<T>): PagedSource<T> {
    return object : AbstractPageBasedPagedSource<T>() {
        override suspend fun nextPageImpl(page: Int): List<T> {
            val paged = getAll()
            noMorePages()
            return paged.toList()
        }
    }
}

@Suppress("FunctionName")
fun <T> PageBasedPagedSource(
    initialPage: Int = 0,
    nextPageOrNull: suspend (page: Int) -> Paged<T>?
): PagedSource<T> {
    val initialPage1 = initialPage

    @Suppress("UnnecessaryVariable", "RedundantSuppression") // two bugs...
    val nextPageOrNullImpl = nextPageOrNull
    return object : AbstractPageBasedPagedSource<T>() {
        override val initialPage: Int get() = initialPage1
        override suspend fun nextPageImpl(page: Int): List<T>? {
            val paged = nextPageOrNullImpl(page)
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

abstract class AbstractPageBasedPagedSource<T> : PagedSource<T> {
    @Suppress("LeakingThis")
    private var page = initialPage
    private val lock = Mutex()
    override val finished = MutableStateFlow(false)

    protected open val initialPage: Int get() = 0

    final override suspend fun nextPageOrNull(): List<T>? = lock.withLock {
        if (page == Int.MAX_VALUE) {
            noMorePages()
            return null
        }
        val result = nextPageImpl(page)
        if (result == null) {
            noMorePages()
            return null
        }
        if (page != Int.MAX_VALUE) {
            page++
        }
        return result
    }

    protected abstract suspend fun nextPageImpl(page: Int): List<T>?

    protected fun noMorePages() {
        if (page == Int.MAX_VALUE) {
            return // already finished
        }
        page = Int.MAX_VALUE
        finished.value = true
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
            }
        }
    }
}