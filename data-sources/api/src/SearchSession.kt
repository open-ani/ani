/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
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

package me.him188.animationgarden.datasources.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 一个搜索请求.
 *
 * **Stateful.** [SearchSession] 会持有当前查询状态信息, 例如当前页码.
 */
interface SearchSession<out T> {
    /**
     * 全部搜索结果, 以 [Flow] 形式提供, 惰性请求.
     */
    val results: Flow<T>

    /**
     * 主动查询下一页. 当已经没有下一页时返回 `null`. 注意, 若有使用 [results], 主动操作 [nextPageOrNull] 将导致 [results] 会跳过该页.
     */
    suspend fun nextPageOrNull(): List<T>?
}

abstract class AbstractPageBasedSearchSession<T> : SearchSession<T> {
    private var page = 0
    private val lock = Mutex()

    final override suspend fun nextPageOrNull(): List<T>? = lock.withLock {
        val result = nextPageImpl(page)
        if (result != null) {
            page++
        }
        return result
    }

    protected abstract suspend fun nextPageImpl(page: Int): List<T>?

    final override val results: Flow<T> by lazy {
        flow {
            while (true) {
                val result = nextPageOrNull()
                if (result.isNullOrEmpty()) {
                    return@flow
                }
                emitAll(result.asFlow())
            }
        }
    }
}