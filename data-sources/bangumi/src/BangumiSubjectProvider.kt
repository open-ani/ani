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

package me.him188.animationgarden.datasources.bangumi

import me.him188.animationgarden.datasources.api.AbstractPageBasedSearchSession
import me.him188.animationgarden.datasources.api.NameIndexSearchType
import me.him188.animationgarden.datasources.api.SearchSession
import me.him188.animationgarden.datasources.api.Subject
import me.him188.animationgarden.datasources.api.SubjectImages
import me.him188.animationgarden.datasources.api.SubjectProvider
import me.him188.animationgarden.datasources.api.SubjectSearchQuery

class BangumiSearchSession(
    private val query: SubjectSearchQuery,
    private val pageSize: Int = 25,
) : AbstractPageBasedSearchSession<Subject>() {
    private val client = BangumiClient.create()

    override suspend fun nextPageImpl(page: Int): List<Subject>? {
        return client.subjects.searchSubjectByKeywords(
            query.keyword,
            type = convertType(),
            responseGroup = BangumiResponseGroup.LARGE, // 才有 rating
            start = page * pageSize,
            maxResults = pageSize,
        )?.map { subject ->
            Subject(
                officialName = subject.name,
                chineseName = subject.nameCN,
                images = object : SubjectImages {
                    override fun landscapeCommon(): String = subject.images.common
                    override fun largePoster(): String = subject.images.large
                },
                episodeCount = subject.epsCount,
                ratingScore = subject.rating?.score ?: 0.0,
                ratingCount = subject.rating?.total ?: 0,
                rank = subject.rank,
                sourceUrl = subject.url,
            )
        }
    }

    private fun convertType() = when (query.type) {
        NameIndexSearchType.ANIME -> BangumiSubjectType.ANIME
    }
}

class BangumiSubjectProvider : SubjectProvider {
    override val id: String get() = "Bangumi"

    override fun startSearch(query: SubjectSearchQuery): SearchSession<Subject> =
        BangumiSearchSession(query)
}