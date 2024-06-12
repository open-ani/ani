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

package me.him188.ani.app.ui.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.him188.ani.app.data.database.eneity.SearchHistoryEntity
import me.him188.ani.app.data.models.SearchSettings
import me.him188.ani.app.data.repositories.SearchHistoryRepository
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.subject.SubjectListViewModel
import me.him188.ani.datasources.api.subject.SubjectProvider
import me.him188.ani.datasources.api.subject.SubjectSearchQuery
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SearchViewModel(
    keyword: String? = "",
) : AbstractViewModel(), KoinComponent {
    private val subjectProvider: SubjectProvider by inject()
    private val settings: SettingsRepository by inject()
    private val _searchHistory: SearchHistoryRepository by inject()

    private val _result: MutableStateFlow<SubjectListViewModel?> = MutableStateFlow(null)


    var searchActive: MutableState<Boolean> = mutableStateOf(false)
    var editingQuery: MutableState<String> = mutableStateOf(keyword ?: "")
    val searchHistory: StateFlow<List<SearchHistory>> = _searchHistory
        .getFlow()
        .distinctUntilChanged()
        .map { it.map { entity -> entity.toData() } }
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf())

    val result: StateFlow<SubjectListViewModel?> = _result

    private val searchSettings: SearchSettings by settings.uiSettings.flow.map { it.searchSettings }
        .produceState(SearchSettings.Default)

    init {
        keyword?.let { search(it) }
    }

    fun pushSearchHistory(content: String) {
        _searchHistory.add(SearchHistoryEntity(content = content))
    }

    fun deleteSearchHistory(id: Int) {
        _searchHistory.deleteBySeq(id)
    }

    fun search(keywords: String) {
        if (keywords.isBlank()) {
            _result.value = null
            return
        }
        _result.value?.close()
        _result.value =
            SubjectListViewModel(
                subjectProvider.startSearch(
                    SubjectSearchQuery(
                        keywords.trim(),
                        useOldSearchApi = !searchSettings.enableNewSearchSubjectApi
                    )
                )
            )
    }
}

data class SearchHistory(
    val id: Int,
    val content: String
)

fun SearchHistoryEntity.toData(): SearchHistory {
    return SearchHistory(sequence, content)
}