/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.search

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import me.him188.ani.app.data.models.ApiResponse
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.tools.ldc.LazyDataCache
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import kotlin.coroutines.CoroutineContext

interface SubjectSearcher {
    /**
     * 唯一 ID, 每次调用 [search] 时增加
     */
    val searchId: StateFlow<Int>

    val list: Flow<List<SubjectInfo>>
    val hasMore: Flow<Boolean>

    suspend fun requestMore(): Boolean?
    fun clear()
    fun search(query: SubjectSearchQuery)
}

class SubjectSearcherImpl(
    private val subjectProvider: SubjectProvider,
    parentCoroutineContext: CoroutineContext,
) : SubjectSearcher, HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    private val currentQuery: MutableStateFlow<SubjectSearchQuery?> = MutableStateFlow(null)

    private val ldc = currentQuery.map { query ->
        query ?: return@map null
        LazyDataCache(
            createSource = { ApiResponse.success(subjectProvider.startSearch(query)) },
            getKey = { it.id },
            debugName = "SubjectSearcher.ldc",
        )
    }.shareInBackground(started = SharingStarted.Lazily)
    override val searchId: MutableStateFlow<Int> = MutableStateFlow(0)

    override val list: Flow<List<SubjectInfo>> = ldc.flatMapLatest { it?.cachedDataFlow ?: flowOf(emptyList()) }
    override val hasMore: Flow<Boolean> = ldc.flatMapLatest { it?.isCompleted ?: flowOf(true) }
        .map { !it }

    override suspend fun requestMore() = ldc.first()?.requestMore()

    override fun clear() {
        currentQuery.value = null
    }

    override fun search(query: SubjectSearchQuery) {
        searchId.update { it + 1 }
        currentQuery.value = query
    }
}