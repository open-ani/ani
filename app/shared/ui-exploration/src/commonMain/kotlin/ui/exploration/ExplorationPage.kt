/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.repository.TrendsRepository
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.ui.adaptive.AniTopAppBar
import me.him188.ani.app.ui.adaptive.AniTopAppBarDefaults
import me.him188.ani.app.ui.adaptive.NavTitleHeader
import me.him188.ani.app.ui.exploration.search.SearchViewModel
import me.him188.ani.app.ui.exploration.search.SubjectPreviewColumn
import me.him188.ani.app.ui.exploration.search.SubjectSearchBar
import me.him188.ani.app.ui.exploration.trending.TrendingSubjectsCarousel
import me.him188.ani.app.ui.exploration.trending.TrendingSubjectsState
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.layout.paneHorizontalPadding
import me.him188.ani.app.ui.foundation.navigation.BackHandler
import me.him188.ani.app.ui.foundation.session.SelfAvatar
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.utils.coroutines.retryUntilSuccess
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ExplorationPageViewModel : AbstractViewModel(), KoinComponent {
    private val trendsRepository: TrendsRepository by inject()

    val state: ExplorationPageState = ExplorationPageState(
        TrendingSubjectsState(
            suspend { trendsRepository.getTrending() }
                .asFlow()
                .map { it.getOrNull() }
                .retryUntilSuccess()
                .map { it?.subjects }
                .produceState(null),
        ),
    )
}

@Composable
fun ExplorationPage(
    modifier: Modifier = Modifier,
    vm: ExplorationPageViewModel = viewModel<ExplorationPageViewModel> { ExplorationPageViewModel() },
    searchBarFocusRequester: FocusRequester = remember { FocusRequester() },
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
) {
    ExplorationPage(vm.state, modifier, searchBarFocusRequester, contentWindowInsets)
}

@Composable
fun ExplorationPage(
    state: ExplorationPageState,
    modifier: Modifier = Modifier,
    searchBarFocusRequester: FocusRequester = remember { FocusRequester() },
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
) {
    val searchViewModel = viewModel { SearchViewModel() } // TODO remove
    val snackBarHostState = remember { SnackbarHostState() }
    val layoutDirection = LocalLayoutDirection.current

    val searchTag by searchViewModel.searchTags.collectAsStateWithLifecycle()
    val showDeleteTagTip = searchViewModel.oneshotActionConfig.deleteSearchTagTip
    val searchHistory by searchViewModel.searchHistories.collectAsStateWithLifecycle()
    val nsfw by searchViewModel.nsfw
    val airDate by searchViewModel.airDate.collectAsStateWithLifecycle()
    val rating by searchViewModel.rating.collectAsStateWithLifecycle()

    var isEditingSearchTags by remember { mutableStateOf(false) }

    BackHandler(isEditingSearchTags) {
        isEditingSearchTags = false
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        containerColor = AniThemeDefaults.pageContentBackgroundColor,
        topBar = {
            AniTopAppBar(
                title = { AniTopAppBarDefaults.Title("探索") },
                windowInsets = contentWindowInsets,
                searchIconButton = {
                    IconButton(
                        {
                            //TODO
                        },
                    ) {
                        Icon(Icons.Rounded.Search, "搜索")
                    }
                },
                searchBar = {
                    SubjectSearchBar(
                        initialActive = searchViewModel.searchActive,
                        initialSearchText = searchViewModel.editingQuery,
                        editingTagMode = isEditingSearchTags,
                        searchTag = searchTag,
                        showDeleteTagTip = showDeleteTagTip,
                        searchHistory = searchHistory,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.fillMaxWidth().focusRequester(searchBarFocusRequester),
                        onActiveChange = { active -> searchViewModel.searchActive = active },
                        onSearchFilterEvent = { event -> searchViewModel.handleSearchFilterEvent(event) },
                        onDeleteHistory = { historyId -> searchViewModel.deleteSearchHistory(historyId) },
                        onStartEditingTagMode = { isEditingSearchTags = true },
                        onSearch = { query, fromHistory ->
                            searchViewModel.editingQuery = query
                            if (!fromHistory && searchHistory.none { it.content == query }) {
                                searchViewModel.pushSearchHistory(query)
                            }
                            searchViewModel.search(query)
                        },
                        windowInsets = contentWindowInsets,
                    )
                },
                avatar = {
                    SelfAvatar(searchViewModel.authState, searchViewModel.selfInfo)
                },
            )
        },
        contentWindowInsets = contentWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    ) { topBarPadding ->
        val horizontalPadding = currentWindowAdaptiveInfo().windowSizeClass.paneHorizontalPadding
        val horizontalContentPadding =
            PaddingValues(horizontal = horizontalPadding)

        if (searchViewModel.searchActive && searchViewModel.editingQuery.isNotBlank()) {
            SubjectPreviewColumn(
                searchViewModel.previewListState,
                contentPadding = topBarPadding,
            )
        } else {
            Column(Modifier.padding(topBarPadding)) {
                NavTitleHeader(
                    title = { Text("最高热度") },
                    contentPadding = horizontalContentPadding,
                )

                val navigator = LocalNavigator.current
                TrendingSubjectsCarousel(
                    state.trendingSubjectsState,
                    onClick = {
                        navigator.navigateSubjectDetails(it.bangumiId)
                    },
                    contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp),
                )
            }
        }
    }
}

@Stable
class ExplorationPageState(
    val trendingSubjectsState: TrendingSubjectsState
)


//@Composable
//internal expect fun PreviewHomePage()