package me.him188.ani.datasources.ikaros

import me.him188.ani.datasources.api.paging.AbstractPageBasedPagedSource
import me.him188.ani.datasources.api.subject.Subject
import me.him188.ani.datasources.api.subject.SubjectSearchQuery
import me.him188.ani.datasources.api.subject.SubjectType
import me.him188.ani.datasources.ikaros.models.IkarosSubjectType

class IkarosPagedSource(
    private val client: IkarosClient,
    private val query: SubjectSearchQuery,
    private val pageSize: Int = 25,
) : AbstractPageBasedPagedSource<Subject>() {

    override suspend fun nextPageImpl(page: Int): List<Subject> {
        TODO("Not yet implemented")
    }

    private fun convertType() = when (query.type) {
        SubjectType.ANIME -> IkarosSubjectType.ANIME
    }
}