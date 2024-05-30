package me.him188.ani.datasources.ikaros

import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.subject.Subject
import me.him188.ani.datasources.api.subject.SubjectProvider
import me.him188.ani.datasources.api.subject.SubjectSearchQuery

class IkarosSubjectProvider (
    private val client: IkarosClient,
) : SubjectProvider {
    companion object {
        val ID = "Bangumi"
    }

    override val id: String get() = ID
    override suspend fun testConnection(): ConnectionStatus {
        return client.testConnection()
    }

    override fun startSearch(query: SubjectSearchQuery): PagedSource<Subject> =
        IkarosPagedSource(client, query)

}