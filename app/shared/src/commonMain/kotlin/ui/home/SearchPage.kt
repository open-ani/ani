/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.him188.ani.app.ui.foundation.navigation.BackHandler
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.app.ui.home.search.SearchViewModel
import me.him188.ani.app.ui.home.search.SubjectPreviewColumn
import me.him188.ani.app.ui.home.search.SubjectSearchBar

@Composable
fun SearchPage(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    searchBarFocusRequester: FocusRequester = remember { FocusRequester() },
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
) {
    val searchViewModel = viewModel { SearchViewModel() }
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
        containerColor = Color.Unspecified,
        topBar = {
            Box {
                SubjectSearchBar(
                    initialActive = searchViewModel.searchActive,
                    initialSearchText = searchViewModel.editingQuery,
                    editingTagMode = isEditingSearchTags,
                    searchTag = searchTag,
                    showDeleteTagTip = showDeleteTagTip,
                    searchHistory = searchHistory,
                    contentPadding = contentPadding,
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
                    windowInsets = contentWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                )

                Crossfade(
                    targetState = isEditingSearchTags,
                    modifier = Modifier.zIndex(1.1f),
                ) {
                    if (it) {
                        TopAppBar(
                            title = { Text("删除标签") },
                            navigationIcon = {
                                IconButton({ isEditingSearchTags = false }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                        contentDescription = "exit delete search tag mode",
                                    )
                                }
                            },
                            colors = AniThemeDefaults.topAppBarColors(),
                            windowInsets = contentWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                        )
                    }
                }
            }
        },
        contentWindowInsets = contentWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    ) { topBarPadding ->
        Column(Modifier.fillMaxSize()) {
            SubjectPreviewColumn(
                searchViewModel.previewListState,
                contentPadding = PaddingValues(
                    top = topBarPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding(),
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    end = contentPadding.calculateEndPadding(layoutDirection),
                ),
            )
        }
    }
}


//@Composable
//internal expect fun PreviewHomePage()