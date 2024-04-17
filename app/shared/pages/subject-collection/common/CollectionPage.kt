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

package me.him188.ani.app.ui.collection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.him188.ani.app.data.subject.SubjectCollectionItem
import me.him188.ani.app.interaction.VibrationStrength
import me.him188.ani.app.interaction.vibrateIfSupported
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.tools.caching.LazyDataCache
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.pagerTabIndicatorOffset
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.isLoggedIn
import me.him188.ani.app.ui.profile.UnauthorizedTips
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle


// 有顺序, https://github.com/Him188/ani/issues/73
@Stable
val COLLECTION_TABS_SORTED = listOf(
    UnifiedCollectionType.DROPPED,
    UnifiedCollectionType.WISH,
    UnifiedCollectionType.DOING,
    UnifiedCollectionType.ON_HOLD,
    UnifiedCollectionType.DONE,
)

/**
 * My collections
 */
@Composable
fun CollectionPage(
    onClickCaches: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val vm = rememberViewModel { MyCollectionsViewModel() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的追番") },
                actions = {
                    IconButton(onClickCaches) {
                        Icon(Icons.Rounded.Download, "缓存管理")
                    }
                }
            )
        },

        ) { topBarPaddings ->
        val isLoggedIn by isLoggedIn()

        val pagerState =
            rememberPagerState(initialPage = COLLECTION_TABS_SORTED.size / 2) { COLLECTION_TABS_SORTED.size }
        val scope = rememberCoroutineScope()

        // Pager with TabRow
        Column(Modifier.padding(topBarPaddings).fillMaxSize()) {
            SecondaryScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = @Composable { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                COLLECTION_TABS_SORTED.forEachIndexed { index, collectionType ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            val type = COLLECTION_TABS_SORTED[index]
                            val cache = vm.collectionsByType(type)
                            val size by cache.totalSize.collectAsStateWithLifecycle(null)
                            if (size == null) {
                                Text(text = collectionType.displayText())
                            } else {
                                Text(text = collectionType.displayText() + " " + size)
                            }
                        }
                    )
                }
            }

            HorizontalPager(state = pagerState, Modifier.fillMaxSize()) { index ->
                val type = COLLECTION_TABS_SORTED[index]
                val cache = vm.collectionsByType(type)

                val pullToRefreshState = rememberPullToRefreshState()
                if (pullToRefreshState.isRefreshing) {
                    LaunchedEffect(true) {
                        try {
                            cache.refresh()
                        } finally {
                            pullToRefreshState.endRefresh()
                        }
                    }
                }

                Box(Modifier.clipToBounds()) {
                    TabContent(
                        cache, vm, type, isLoggedIn, contentPadding,
                        Modifier
                            .nestedScroll(pullToRefreshState.nestedScrollConnection)
                            .fillMaxSize()
                    )
                    PullToRefreshContainer(
                        pullToRefreshState,
                        Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}

/**
 *
 */
@Composable
private fun TabContent(
    cache: LazyDataCache<SubjectCollectionItem>,
    vm: MyCollectionsViewModel,
    type: UnifiedCollectionType,
    isLoggedIn: Boolean?,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val context by rememberUpdatedState(LocalContext.current)
    SubjectCollectionsColumn(
        cache,
        item = { subjectCollection ->
            var showEpisodeProgressDialog by rememberSaveable { mutableStateOf(false) }
            if (showEpisodeProgressDialog) {
                val navigator = LocalNavigator.current
                EpisodeProgressDialog(
                    onDismissRequest = { showEpisodeProgressDialog = false },
                    onClickDetails = { navigator.navigateSubjectDetails(subjectCollection.subjectId) },
                    title = { Text(text = subjectCollection.displayName) },
                    onClickCache = { navigator.navigateSubjectCaches(subjectCollection.subjectId) },
                ) {
                    val progress by remember(vm, subjectCollection) {
                        vm.subjectProgress(subjectCollection)
                    }.collectAsStateWithLifecycle(emptyList())
                    EpisodeProgressRow(
                        episodes = progress,
                        onClickEpisodeState = {
                            navigator.navigateEpisodeDetails(subjectCollection.subjectId, it.episodeId)
                        },
                        onLongClickEpisode = { progressItem ->
                            context.vibrateIfSupported(VibrationStrength.TICK)
                            vm.launchInBackground {
                                setEpisodeWatched(
                                    subjectCollection.subjectId,
                                    progressItem.episodeId,
                                    watched = progressItem.watchStatus != UnifiedCollectionType.DONE
                                )
                            }
                        },
                    )
                }
            }

            val navigator = LocalNavigator.current
            SubjectCollectionItem(
                subjectCollection,
                episodeCacheStatus = { subjectId, episodeId ->
                    remember(vm, subjectId, episodeId) {
                        vm.cacheStatusForEpisode(subjectId, episodeId)
                    }.collectAsStateWithLifecycle(null).value
                },
                onClick = {
                    navigator.navigateSubjectDetails(subjectCollection.subjectId)
                },
                onClickEpisode = {
                    navigator.navigateEpisodeDetails(subjectCollection.subjectId, it.episode.id)
                },
                onClickSelectEpisode = {
                    showEpisodeProgressDialog = true
                },
                onSetAllEpisodesDone = {
                    vm.launchInBackground { setAllEpisodesWatched(subjectCollection.subjectId) }
                },
                onSetCollectionType = {
                    vm.launchInBackground { setCollectionType(subjectCollection.subjectId, it) }
                },
                doneButton = if (type == UnifiedCollectionType.DONE) {
                    null
                } else {
                    {
                        FilledTonalButton(
                            {
                                vm.launchInBackground {
                                    setCollectionType(subjectCollection.subjectId, UnifiedCollectionType.DONE)
                                }
                            },
                        ) {
                            Text("移至\"看过\"")
                        }
                    }
                },
            )
        },
        onEmpty = {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(32.dp))
                if (isLoggedIn == false) {
                    UnauthorizedTips(Modifier.fillMaxSize())
                } else {
                    Text("~ 空空如也 ~", style = MaterialTheme.typography.titleMedium)
                }
            }
        },
        modifier,
        contentPadding = contentPadding,
    )
}

private fun UnifiedCollectionType.displayText(): String {
    return when (this) {
        UnifiedCollectionType.WISH -> "想看"
        UnifiedCollectionType.DOING -> "在看"
        UnifiedCollectionType.DONE -> "看过"
        UnifiedCollectionType.ON_HOLD -> "搁置"
        UnifiedCollectionType.DROPPED -> "抛弃"
        UnifiedCollectionType.NOT_COLLECTED -> "未收藏"
    }
}

@Composable
internal expect fun PreviewCollectionPage()
