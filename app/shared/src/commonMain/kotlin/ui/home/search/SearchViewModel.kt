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

package me.him188.ani.app.ui.home.search

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.preference.OneshotActionConfig
import me.him188.ani.app.data.models.preference.SearchSettings
import me.him188.ani.app.data.persistent.database.eneity.SearchHistoryEntity
import me.him188.ani.app.data.persistent.database.eneity.SearchTagEntity
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.data.repository.SubjectSearchRepository
import me.him188.ani.app.tools.search.SubjectSearcher
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.datasources.api.subject.SubjectProvider
import me.him188.ani.datasources.api.subject.SubjectSearchQuery
import me.him188.ani.utils.coroutines.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class SearchViewModel(
    keyword: String? = "",
) : AbstractViewModel(), KoinComponent {
    private val subjectProvider: SubjectProvider by inject()
    private val settings: SettingsRepository by inject()
    private val _search: SubjectSearchRepository by inject()

    // search bar state
    var searchActive by mutableStateOf(false)
    var editingQuery by mutableStateOf(keyword ?: "")

    // search result
    private val searcher = SubjectSearcher(subjectProvider, backgroundScope.coroutineContext)
    val previewListState: SubjectPreviewListState = SubjectPreviewListState(
        items = searcher.list.produceState(emptyList()),
        hasMore = searcher.hasMore.produceState(true),
        onRequestMore = { searcher.requestMore() },
        backgroundScope = backgroundScope,
    )

    // search settings
    private val searchSettings: SearchSettings by settings.uiSettings.flow.map { it.searchSettings }
        .produceState(SearchSettings.Default)
    val oneshotActionConfig by settings.oneshotActionConfig.flow.produceState(OneshotActionConfig.Default)

    // search filters
    private val checkedTag = MutableStateFlow<MutableList<Int>>(mutableListOf())
    val searchTags: StateFlow<List<SearchTag>> = _search
        .getTagFlow()
        .distinctUntilChanged()
        .combine(checkedTag) { tags, checked ->
            tags.map { entity -> entity.toData(checked.contains(entity.id)) }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf())

    private val _airDate = MutableStateFlow(AirDate(null, false, null, false))
    val airDate: StateFlow<AirDate> = _airDate
    private val _rating = MutableStateFlow(Rating(null, false, null, false))
    val rating: StateFlow<Rating> = _rating
    private val _nsfw: MutableState<Boolean?> = mutableStateOf(null)
    val nsfw: State<Boolean?> = _nsfw


    val searchHistories: StateFlow<List<SearchHistory>> = _search
        .getHistoryFlow()
        .distinctUntilChanged()
        .map { it.map { entity -> entity.toData() } }
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf())


    init {
        keyword?.let { search(it) }
    }

    // TODO: 把更多 event 加进来
    // TODO: 把这种处理方式添加到 AbstractViewModel 中
    fun handleSearchFilterEvent(event: SearchFilterEvent) = backgroundScope.launch {
        when (event) {
            is SearchFilterEvent.AddTag -> _search.addTag(SearchTagEntity(content = event.value))
            is SearchFilterEvent.DeleteTag -> _search.deleteTagById(event.id)
            is SearchFilterEvent.UpdateTag -> {
                val listCopied = checkedTag.value.toMutableList()

                val tagIndex = listCopied.indexOf(event.id)
                if (event.selected && tagIndex == -1) {
                    listCopied.add(event.id)
                } else if (tagIndex != -1) {
                    listCopied.removeAt(tagIndex)
                }
                checkedTag.update { listCopied }
            }

            SearchFilterEvent.UnselectAllTag -> {}
            is SearchFilterEvent.UpdateAirDateLeft ->
                _airDate.emit(_airDate.value.copy(start = event.value, startInclusive = event.inclusive))

            is SearchFilterEvent.UpdateAirDateRight ->
                _airDate.emit(_airDate.value.copy(end = event.value, endInclusive = event.inclusive))

            is SearchFilterEvent.UpdateRatingLeft ->
                _rating.emit(_rating.value.copy(start = event.value, startInclusive = event.inclusive))

            is SearchFilterEvent.UpdateRatingRight ->
                _airDate.emit(_airDate.value.copy(end = event.value, endInclusive = event.inclusive))

            is SearchFilterEvent.UpdateNsfw -> _nsfw.value = event.value
            SearchFilterEvent.DismissTagTip ->
                settings.oneshotActionConfig.set(oneshotActionConfig.copy(deleteSearchTagTip = false))
        }
    }

    fun pushSearchHistory(content: String) {
        backgroundScope.launch {
            _search.addHistory(SearchHistoryEntity(content = content))
        }
    }

    fun deleteSearchHistory(id: Int) {
        backgroundScope.launch {
            _search.deleteHistoryBySeq(id)
        }
    }

    fun search(keywords: String) {
        if (keywords.isBlank()) {
            searcher.clear()
            return
        }
        searcher.search(
            SubjectSearchQuery(
                keywords.trim(),
                useOldSearchApi = !searchSettings.enableNewSearchSubjectApi,
            ),
        )
    }
}


sealed interface SearchFilterEvent {
    data class AddTag(val value: String) : SearchFilterEvent
    data class DeleteTag(val id: Int) : SearchFilterEvent
    data class UpdateTag(val id: Int, val selected: Boolean) : SearchFilterEvent
    data object UnselectAllTag : SearchFilterEvent
    data class UpdateAirDateLeft(val value: Int?, val inclusive: Boolean) : SearchFilterEvent
    data class UpdateAirDateRight(val value: Int?, val inclusive: Boolean) : SearchFilterEvent
    data class UpdateRatingLeft(val value: Int?, val inclusive: Boolean) : SearchFilterEvent
    data class UpdateRatingRight(val value: Int?, val inclusive: Boolean) : SearchFilterEvent
    data class UpdateNsfw(val value: Boolean?) : SearchFilterEvent
    data object DismissTagTip : SearchFilterEvent
}

data class AirDate(
    val start: Int?, // year * 12 + month
    val startInclusive: Boolean,
    val end: Int?,
    val endInclusive: Boolean
)

data class Rating(
    val start: Int?,
    val startInclusive: Boolean,
    val end: Int?,
    val endInclusive: Boolean
)

@Immutable
data class SearchTag(
    val id: Int,
    val content: String,
    val checked: Boolean
)

fun SearchTagEntity.toData(checked: Boolean): SearchTag {
    return SearchTag(id, content, checked)
}

@Immutable
data class SearchHistory(
    val id: Int,
    val content: String
)

fun SearchHistoryEntity.toData(): SearchHistory {
    return SearchHistory(sequence, content)
}