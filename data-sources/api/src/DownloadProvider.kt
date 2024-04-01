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

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import me.him188.ani.datasources.api.topic.Alliance
import me.him188.ani.datasources.api.topic.Topic
import me.him188.ani.datasources.api.topic.TopicCategory

/**
 * 提供下载的数据源
 */
interface DownloadProvider {
    /**
     * Unique ID.
     */
    val id: String

    /**
     * Starts a search session.
     */
    suspend fun startSearch(query: DownloadSearchQuery): PagedSource<Topic>
}

fun CombinedDownloadProvider(
    vararg providers: DownloadProvider,
): CombinedDownloadProvider = CombinedDownloadProvider(providers.toList())

class CombinedDownloadProvider(
    private val providers: List<DownloadProvider>,
) : DownloadProvider {
    override val id: String get() = "combined: ${providers.joinToString { it.id }}"

    override suspend fun startSearch(query: DownloadSearchQuery): PagedSource<Topic> {
        val sessions = providers.mapNotNull { runCatching { it.startSearch(query) }.getOrNull() }
        return PageBasedPagedSource {
            sessions.asFlow()
                .mapNotNull { session ->
                    session.nextPageOrNull()?.takeIf { it.isNotEmpty() }
                }
                .firstOrNull()
                ?.let { r ->
                    Paged(r.size, true, r)
                }
//            var res: List<Topic>? = null
//            sessions.forEach { session ->
//                session?.nextPageOrNull()?.also { res = it }
//            }
//            res?.let { r ->
//                Paged(r.size, true, r)
//            }
        }
    }
}

interface DownloadProviderFactory {
    fun create(): DownloadProvider
}

data class DownloadSearchQuery(
    val keywords: String? = null,
    val category: TopicCategory? = null,
    val alliance: Alliance? = null,
    val ordering: SearchOrdering? = null,
)

interface SearchOrdering {
    val id: String
    val name: String
}
