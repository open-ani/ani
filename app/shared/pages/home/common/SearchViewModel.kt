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

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.him188.ani.app.data.database.eneity.SearchHistoryEntity
import me.him188.ani.app.data.database.eneity.SearchTagEntity
import me.him188.ani.app.data.models.OneshotActionConfig
import me.him188.ani.app.data.models.SearchSettings
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.data.repositories.SubjectSearchRepository
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.subject.SubjectListViewModel
import me.him188.ani.datasources.api.subject.SubjectProvider
import me.him188.ani.datasources.api.subject.SubjectSearchQuery
import me.him188.ani.utils.coroutines.update
import me.him188.ani.utils.logging.info
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class SearchViewModel(
    keyword: String? = "",
) : AbstractViewModel(), KoinComponent {
    private val subjectProvider: SubjectProvider by inject()
    private val settings: SettingsRepository by inject()
    private val _search: SubjectSearchRepository by inject()

    /**
     * search bar state
     */
    var searchActive by mutableStateOf(false)
    var editingQuery by mutableStateOf(keyword ?: "")

    /**
     * search result
     */
    private val _result: MutableStateFlow<SubjectListViewModel?> = MutableStateFlow(null)
    val result: StateFlow<SubjectListViewModel?> = _result

    /**
     * search settings
     */
    private val searchSettings: SearchSettings by settings.uiSettings.flow.map { it.searchSettings }
        .produceState(SearchSettings.Default)
    val oneshotActionConfig by settings.oneshotActionConfig.flow.produceState(OneshotActionConfig.Default)

    /**
     * search options
     */
    private val checkedTag = MutableStateFlow<MutableList<Int>>(mutableListOf())
    val searchTags: StateFlow<List<SearchTag>> = _search
        .getTagFlow()
        .distinctUntilChanged()
        .combine(checkedTag) { tags, checked ->
            logger.info { "combine flow" }
            tags.map { entity -> entity.toData(checked.contains(entity.id)) }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf())
    val searchHistories: StateFlow<List<SearchHistory>> = _search
        .getHistoryFlow()
        .distinctUntilChanged()
        .map { it.map { entity -> entity.toData() } }
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf())
    

    
    init {
        keyword?.let { search(it) }
        viewModelScope.launch {
            settings.oneshotActionConfig.set(oneshotActionConfig.copy(deleteSearchTagTip = true))
        }
    }

    fun pushSearchHistory(content: String) {
        viewModelScope.launch {
            _search.addHistory(SearchHistoryEntity(content = content))
        }
    }

    fun deleteSearchHistory(id: Int) {
        viewModelScope.launch {
            _search.deleteHistoryBySeq(id)
        }
    }

    fun pushSearchTag(content: String) {
        viewModelScope.launch {
            _search.addTag(SearchTagEntity(content = content))
        }
    }

    fun deleteSearchTag(id: Int) {
        viewModelScope.launch {
            _search.deleteTagById(id)
        }
    }

    fun markSearchTag(id: Int, checked: Boolean) {
        val listCopied = checkedTag.value.toMutableList()

        val tagIndex = listCopied.indexOf(id)
        if (checked && tagIndex == -1) {
            listCopied.add(id)
        } else if (tagIndex != -1) {
            listCopied.removeAt(tagIndex)
        }
        checkedTag.update { listCopied }
    }

    fun disableTagTip() {
        viewModelScope.launch {
            settings.oneshotActionConfig.set(oneshotActionConfig.copy(deleteSearchTagTip = false))
        }
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

@Stable
data class SearchTag(
    val id: Int,
    val content: String,
    val checked: Boolean
)

fun SearchTagEntity.toData(checked: Boolean): SearchTag {
    return SearchTag(id, content, checked)
}

@Stable
data class SearchHistory(
    val id: Int,
    val content: String
)

fun SearchHistoryEntity.toData(): SearchHistory {
    return SearchHistory(sequence, content)
}