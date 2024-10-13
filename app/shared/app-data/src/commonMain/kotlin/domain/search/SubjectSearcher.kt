/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.search

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.models.ApiResponse
import me.him188.ani.app.tools.ldc.LazyDataCache
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import kotlin.coroutines.CoroutineContext

class SubjectSearcher(
    private val subjectProvider: SubjectProvider,
    parentCoroutineContext: CoroutineContext,
) : HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    private val currentQuery: MutableStateFlow<SubjectSearchQuery?> = MutableStateFlow(null)

    private val ldc = currentQuery.map { query ->
        query ?: return@map null
        LazyDataCache(
            createSource = { ApiResponse.success(subjectProvider.startSearch(query)) },
            getKey = { it.id },
            debugName = "SubjectSearcher.ldc",
        )
    }.shareInBackground(started = SharingStarted.Lazily)

    val list = ldc.flatMapLatest { it?.cachedDataFlow ?: flowOf(emptyList()) }
    val hasMore = ldc.flatMapLatest { it?.isCompleted ?: flowOf(true) }
        .map { !it }

    suspend fun requestMore() = ldc.first()?.requestMore()

    fun clear() {
        currentQuery.value = null
    }

    fun search(query: SubjectSearchQuery) {
        currentQuery.value = query
    }
}