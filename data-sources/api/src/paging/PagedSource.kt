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
import kotlinx.coroutines.flow.map

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

fun <T> Collection<T>.asSinglePageSource(): PagedSource<T> {
    return object : PagedSource<T> {
        override val results: Flow<T> = asFlow()
        override val finished: StateFlow<Boolean> = MutableStateFlow(true)
        override val currentPage: MutableStateFlow<Int> = MutableStateFlow(0)
        override val totalSize: StateFlow<Int?> = MutableStateFlow(size)
        override suspend fun nextPageOrNull(): List<T>? {
            if (!currentPage.compareAndSet(0, 1)) return null
            return this@asSinglePageSource.toList()
        }

        override fun backToPrevious() {
            if (currentPage.value == 0) return
            currentPage.compareAndSet(1, 0)
        }
    }
}