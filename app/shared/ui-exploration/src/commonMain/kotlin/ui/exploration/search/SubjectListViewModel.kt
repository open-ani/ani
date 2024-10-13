/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.search

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.yield
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.datasources.api.paging.PagedSource
import org.koin.core.component.KoinComponent
import kotlin.coroutines.CoroutineContext

@Stable
class SubjectListViewModel(
    private val pagedSource: PagedSource<SubjectInfo>,
    parentCoroutineContext: CoroutineContext,
) : KoinComponent {
    // TODO: refactor SubjectListViewModel
    private val backgroundScope = BackgroundScope(parentCoroutineContext)
    fun close() {
        backgroundScope.backgroundScope.cancel()
    }

    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    private val _list: MutableStateFlow<List<SubjectInfo>> = MutableStateFlow(listOf())
    val list: StateFlow<List<SubjectInfo>> get() = _list

    private val _hasMore: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> get() = _hasMore

    fun loadMore() {
        if (_loading.value) return
        if (!hasMore.value) return

        backgroundScope.launchInBackground(start = CoroutineStart.UNDISPATCHED) {
            _loading.value = true
            yield()

            try {
                val nextPage = pagedSource.nextPageOrNull()
                if (nextPage == null) {
                    _hasMore.value = false
                } else {
                    _list.value += nextPage
                    _hasMore.value = true
                }
            } catch (e: Throwable) {
                _hasMore.value = false
                throw e
            } finally {
                _loading.value = false
            }
        }
    }
}