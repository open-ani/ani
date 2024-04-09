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

package me.him188.ani.datasources.dmhy.impl

import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.datasources.api.DownloadSearchQuery
import me.him188.ani.datasources.api.paging.AbstractPageBasedPagedSource
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.datasources.api.titles.toTopicDetails
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.Topic
import me.him188.ani.datasources.api.topic.TopicCategory
import me.him188.ani.datasources.api.topic.matches
import me.him188.ani.datasources.dmhy.impl.protocol.Network

internal class DmhyPagedSourceImpl(
    private val query: DownloadSearchQuery,
    private val network: Network,
) : PagedSource<Topic>, AbstractPageBasedPagedSource<Topic>() {
    override val currentPage: MutableStateFlow<Int> = MutableStateFlow(1)

    override suspend fun nextPageImpl(page: Int): List<Topic> {
        val (_, rawResults) = network.list(
            page = page,
            keyword = query.keywords,
            sortId = getCategoryId(),
            teamId = query.alliance?.id,
            orderId = query.ordering?.id
        )
        val results = rawResults.map { topic ->
            Topic(
                id = topic.id,
                publishedTimeMillis = topic.publishedTimeMillis,
                category = TopicCategory.ANIME,
                rawTitle = topic.rawTitle,
                commentsCount = topic.commentsCount,
                downloadLink = ResourceLocation.MagnetLink(topic.magnetLink),
                size = topic.size,
                alliance = topic.alliance?.name ?: topic.rawTitle.substringBeforeLast(']').substringAfterLast('['),
                author = topic.author,
                details = topic.details?.toTopicDetails(),
                originalLink = topic.link,
            )
        }.filter {
            query.matches(it)
        }
        if (results.none()) {
            noMorePages()
            return emptyList()
        }
        return results
    }

    private fun getCategoryId(): String? {
        return when (query.category) {
            TopicCategory.ANIME -> "2"
            null -> null
        }
    }
}