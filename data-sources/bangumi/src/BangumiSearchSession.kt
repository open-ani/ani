package me.him188.animationgarden.datasources.bangumi

import me.him188.animationgarden.datasources.api.AbstractPageBasedSearchSession
import me.him188.animationgarden.datasources.api.Subject
import me.him188.animationgarden.datasources.api.SubjectSearchQuery
import me.him188.animationgarden.datasources.api.SubjectType

class BangumiSearchSession(
    private val client: BangumiClient,
    private val query: SubjectSearchQuery,
    private val pageSize: Int = 25,
) : AbstractPageBasedSearchSession<Subject>() {

    override suspend fun nextPageImpl(page: Int): List<Subject>? {
        return client.subjects.searchSubjectByKeywords(
            query.keyword,
            offset = page * pageSize,
            // 才有 rating
            limit = pageSize,
            types = listOf(convertType()),
        )?.map { subject ->
            subject.toSubject()
        }
    }

    private fun convertType() = when (query.type) {
        SubjectType.ANIME -> BangumiSubjectType.ANIME
    }
}