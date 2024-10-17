/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.search

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.utils.platform.annotations.TestOnly

@Stable
class SearchPageState(
    searchHistoryState: State<List<String>>,
    suggestionsState: State<List<String>>,
    searchResultItemsState: State<List<SubjectPreviewItemInfo>>,
    val onSearch: () -> Unit,
    val onRequestPlay: suspend (SubjectPreviewItemInfo) -> EpisodeTarget?,
    queryState: MutableState<String> = mutableStateOf(""),
    backgroundScope: CoroutineScope,
) {
    // to navigate to episode page
    data class EpisodeTarget(
        val subjectId: Int,
        val episodeId: Int,
    )

    val query by queryState
    val suggestionSearchBarState = SuggestionSearchBarState(
        historyState = searchHistoryState,
        suggestionsState = suggestionsState,
        queryState = queryState,
    )

    val searchResultItems: List<SubjectPreviewItemInfo> by searchResultItemsState
    var selectedItemIndex: Int by mutableIntStateOf(0)
    val selectedItem by derivedStateOf { searchResultItems.getOrNull(selectedItemIndex) }

    val playTasker = MonoTasker(backgroundScope)
    var playingItem: SubjectPreviewItemInfo? by mutableStateOf(null)
        private set

    suspend fun requestPlay(info: SubjectPreviewItemInfo): EpisodeTarget? {
        playingItem = info
        return playTasker.async {
            onRequestPlay(info)
        }.await()
    }
}

@TestOnly
fun createTestSearchPageState(backgroundScope: CoroutineScope): SearchPageState {
    val results = mutableStateOf<List<SubjectPreviewItemInfo>>(emptyList())
    return SearchPageState(
        searchHistoryState = mutableStateOf(emptyList()),
        suggestionsState = mutableStateOf(emptyList()),
        searchResultItemsState = results,
        onSearch = {
            backgroundScope.launch {
                delay(1500)
                results.value = listOf()
            }
        },
        onRequestPlay = { info ->
            delay(3000)
            SearchPageState.EpisodeTarget(1, 2)
        },
        queryState = mutableStateOf(""),
        backgroundScope = backgroundScope,
    )
}
