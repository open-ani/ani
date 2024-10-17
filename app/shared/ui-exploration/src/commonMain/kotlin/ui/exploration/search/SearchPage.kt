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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.ui.adaptive.AniTopAppBar
import me.him188.ani.app.ui.foundation.layout.AniListDetailPaneScaffold
import me.him188.ani.app.ui.foundation.layout.paneVerticalPadding
import me.him188.ani.app.ui.foundation.navigation.BackHandler
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.app.ui.foundation.widgets.TopAppBarGoBackButton
import me.him188.ani.app.ui.search.SearchDefaults
import me.him188.ani.app.ui.search.SearchState

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
                Modifier.fillMaxWidth(),
                placeholder = { Text("搜索") },
            )
        },
        searchResultList = {
            val aniNavigator = LocalNavigator.current
            val scope = rememberCoroutineScope()
            SearchPageResultColumn(
                state.searchState,
                selectedItemIndex = { state.selectedItemIndex },
                onClick = { index ->
                    state.selectedItemIndex = index
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
            )
        },
        detailContent = {
            AnimatedContent(
                state.selectedItemIndex,
                transitionSpec = AniThemeDefaults.emphasizedAnimatedContentTransition,
            ) { index ->
                state.searchState.items.getOrNull(index)?.let {
                    detailContent(it.id)
                }
            }
        },
        modifier,
    )
}

@Composable
internal fun SearchPageResultColumn(
    state: SearchState<SubjectPreviewItemInfo>,
    selectedItemIndex: () -> Int,
    onClick: (index: Int) -> Unit,
    onPlay: (info: SubjectPreviewItemInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchDefaults.ResultColumn(
        state,
        modifier,
    ) {
        itemsIndexed(
            state.items,
            key = { _, it -> it.id },
            contentType = { _, _ -> 1 },
        ) { index, info ->
            SubjectPreviewItem(
                selected = index == selectedItemIndex(),
                onClick = { onClick(index) },
                onPlay = { onPlay(info) },
                info = info,
                Modifier.padding(vertical = currentWindowAdaptiveInfo().windowSizeClass.paneVerticalPadding / 2),
            )
        }
    }
}

@Composable
internal fun SearchPageLayout(
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
            Column(Modifier.preferredWidth(440.dp).paneContentPadding()) {
                // Use TopAppBar as a container for scroll behavior
                Row(Modifier.fillMaxWidth()) {
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
