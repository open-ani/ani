package me.him188.ani.datasources.bangumi

import me.him188.ani.datasources.api.paging.AbstractPageBasedPagedSource
import me.him188.ani.datasources.api.subject.Subject
import me.him188.ani.datasources.api.subject.SubjectSearchQuery
import me.him188.ani.datasources.api.subject.SubjectType
import me.him188.ani.datasources.bangumi.models.subjects.BangumiSubjectType
import me.him188.ani.datasources.bangumi.models.subjects.toSubject

class BangumiPagedSource(
    private val client: BangumiClient,
    private val query: SubjectSearchQuery,
    private val pageSize: Int = 25,
) : AbstractPageBasedPagedSource<Subject>() {

    override suspend fun nextPageImpl(page: Int): List<Subject> {
        val paged = client.subjects.searchSubjectByKeywords(
            query.keyword,
            offset = page * pageSize,
            // 才有 rating
            limit = pageSize,
            types = listOf(convertType()),
        )
        if (!paged.hasMore) {
            noMorePages()
        }
        return paged.page.map { it.toSubject() }
    }

    private fun convertType() = when (query.type) {
        SubjectType.ANIME -> BangumiSubjectType.ANIME
    }
}