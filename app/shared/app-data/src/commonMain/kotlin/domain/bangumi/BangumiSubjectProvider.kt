/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.bangumi

import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.domain.search.SubjectProvider
import me.him188.ani.app.domain.search.SubjectSearchQuery
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.bangumi.BangumiClient

class BangumiSubjectProvider(
    private val client: BangumiClient,
) : SubjectProvider {
    companion object {
        val ID = "Bangumi"
    }

    override val id: String get() = ID
    override suspend fun testConnection(): ConnectionStatus {
        return client.testConnection()
    }

    override fun startSearch(query: SubjectSearchQuery): PagedSource<SubjectInfo> =
        BangumiPagedSource(client, query)

//    override suspend fun getSubjectDetails(id: String): SubjectDetails? {
//        return client.subjects.getSubjectById(id.toLong())?.toSubjectDetails()
//    }
}