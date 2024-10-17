/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.adaptive.AniTopAppBar
import me.him188.ani.app.ui.foundation.layout.AniListDetailPaneScaffold
import me.him188.ani.app.ui.foundation.navigation.BackHandler
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.app.ui.foundation.widgets.TopAppBarGoBackButton
import me.him188.ani.utils.platform.annotations.TestOnly

@Composable
fun SearchPage(
    state: SearchPageState,
    windowInsets: WindowInsets,
    detailContent: @Composable (subjectId: Int) -> Unit,
    modifier: Modifier = Modifier,
    navigator: ThreePaneScaffoldNavigator<*> = rememberListDetailPaneScaffoldNavigator(),
) {
    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
    }

    SearchPageLayout(
        navigator,
        windowInsets,
        searchBar = {
            SuggestionSearchBar(
                state.suggestionSearchBarState,
                onSearch = {
                    state.onSearch()
                },
            )
        },
        searchResultList = {
            val aniNavigator = LocalNavigator.current
            val scope = rememberCoroutineScope()
            SearchPageResultColumn(
                onViewDetails = {
                    state.selectedItemIndex = it
                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                },
                onPlay = { info ->
                    scope.launch(start = CoroutineStart.UNDISPATCHED) {
                        val playInfo = state.requestPlay(info)
                        playInfo?.let {
                            aniNavigator.navigateEpisodeDetails(it.subjectId, playInfo.episodeId)
                        }
                    }
                },
                state.searchResultItems,
            )
        },
        detailContent = {
            AnimatedContent(
                state.selectedItemIndex,
                transitionSpec = AniThemeDefaults.emphasizedAnimatedContentTransition,
            ) { index ->
                state.searchResultItems.getOrNull(index)?.let {
                    detailContent(it.id)
                }
            }
        },
        modifier,
    )
}

@Composable
fun SearchPageResultColumn(
    onViewDetails: (index: Int) -> Unit,
    onPlay: (info: SubjectPreviewItemInfo) -> Unit,
    list: List<SubjectPreviewItemInfo>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier) {
        itemsIndexed(
            list,
            key = { _, it -> it.id },
            contentType = { _, _ -> 1 },
        ) { index, info ->
            SubjectPreviewItem(
                onViewDetails = { onViewDetails(index) },
                onPlay = { onPlay(info) },
                info = info,
            )
        }
    }
}

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

@Composable
fun SearchPageLayout(
    navigator: ThreePaneScaffoldNavigator<*>,
    windowInsets: WindowInsets,
    searchBar: @Composable () -> Unit,
    searchResultList: @Composable () -> Unit,
    detailContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    AniListDetailPaneScaffold(
        navigator,
        listPaneTopAppBar = {
            AniTopAppBar(
                title = { Text("搜索") },
                windowInsets,
                Modifier.fillMaxWidth(),
                navigationIcon = {
                    TopAppBarGoBackButton()
                },
            )
        },
        listPaneContent = {
            Box(Modifier.paneContentPadding()) {
                // Use TopAppBar as a container for scroll behavior
                Row {
                    searchBar()
                }
//                TopAppBar(
//                    title = { searchBar() },
//                    scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
////                    expandedHeight = with(LocalDensity.current) {
////                        LocalWindowInfo.current.containerSize.height.toDp()
////                    },
//                    colors = AniThemeDefaults.topAppBarColors(),
//                )
                searchResultList()
            }
        },
        detailPaneTopAppBar = {}, // empty because our detailPaneContent already has it
        detailPaneContent = {
            detailContent()
        },
        modifier,
    )
}
