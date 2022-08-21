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

package me.him188.animationgarden.api.impl

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import me.him188.animationgarden.api.impl.model.CacheImpl
import me.him188.animationgarden.api.impl.protocol.Network
import me.him188.animationgarden.api.model.SearchQuery
import me.him188.animationgarden.api.model.SearchSession
import me.him188.animationgarden.api.model.Topic

internal class SearchSessionImpl(
    override val query: SearchQuery,
    private val network: Network,
) : SearchSession {
    private val page = atomic(1)

    private val cache = CacheImpl()

    override val results: Flow<Topic> by lazy {
        flow {
            while (true) {
                val result = nextPage()
                if (result.isNullOrEmpty()) {
                    return@flow
                }
                emitAll(result.asFlow())
            }
        }
    }

    override suspend fun nextPage(): List<Topic>? {
        val currentPage = page.value
        val (context, result) = network.list(
            page = currentPage,
            keyword = query.keywords,
            sortId = query.category?.id,
            teamId = query.alliance?.id,
            orderId = query.ordering?.id
        )
        cache.mergeFrom(context)
        page.compareAndSet(currentPage, currentPage + 1)
        return result
    }
}