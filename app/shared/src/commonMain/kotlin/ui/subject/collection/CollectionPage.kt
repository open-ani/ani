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

package me.him188.ani.app.ui.subject.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.episode.episode
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.currentPlatform
import me.him188.ani.app.platform.isDesktop
import me.him188.ani.app.platform.isMobile
import me.him188.ani.app.session.AuthState
import me.him188.ani.app.tools.caching.RefreshOrderPolicy
import me.him188.ani.app.tools.rememberUiMonoTasker
import me.him188.ani.app.ui.foundation.effects.OnLifecycleEvent
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.layout.isShowLandscapeUI
import me.him188.ani.app.ui.foundation.pagerTabIndicatorOffset
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.subject.collection.progress.EpisodeProgressDialog
import me.him188.ani.app.ui.subject.collection.progress.rememberEpisodeProgressState
import me.him188.ani.app.ui.update.TextButtonUpdateLogo
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.lifecycle.Lifecycle
import kotlin.time.Duration.Companion.minutes


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
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val vm = rememberViewModel { MyCollectionsViewModel() }

    val pagerState =
        rememberPagerState(initialPage = COLLECTION_TABS_SORTED.size / 2) { COLLECTION_TABS_SORTED.size }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = { Text("我的追番") },
                    modifier = Modifier.alpha(0.97f),
                    actions = {
                        if (!isShowLandscapeUI()) {
                            TextButtonUpdateLogo()

                            IconButton(onClickCaches) {
                                Icon(Icons.Rounded.Download, "缓存管理")
                            }
                        }

                        if (currentPlatform.isDesktop()) {
                            // PC 无法下拉刷新
                            val refreshTasker = rememberUiMonoTasker()
                            IconButton(
                                {
                                    val type = COLLECTION_TABS_SORTED[pagerState.currentPage]
                                    val collection = vm.collectionsByType(type)
                                    collection.isAutoRefreshing = false
                                    collection.pullToRefreshState?.startRefresh()
//                                val cache = collection.cache
//                                
//                                refreshTasker.launch {
//                                    cache.refresh(RefreshOrderPolicy.REPLACE)
//                                }
                                },
                            ) {
                                Icon(Icons.Rounded.Refresh, null)
                            }
                        }
                    },
                )

                SecondaryScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    indicator = @Composable { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                        )
                    },
                    modifier = Modifier.fillMaxWidth().alpha(0.97f),
                ) {
                    COLLECTION_TABS_SORTED.forEachIndexed { index, collectionType ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = {
                                val type = COLLECTION_TABS_SORTED[index]
                                val cache = vm.collectionsByType(type).cache
                                val size by cache.totalSize.collectAsStateWithLifecycle(null)
                                if (size == null) {
                                    Text(text = collectionType.displayText())
                                } else {
                                    Text(
                                        text = remember(collectionType, size) {
                                            collectionType.displayText() + " " + size
                                        },
                                    )
                                }
                            },
                        )
                    }
                }
            }
        },

        ) { topBarPaddings ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = Platform.currentPlatform.isMobile(),
            verticalAlignment = Alignment.Top,
        ) { index ->
            val type = COLLECTION_TABS_SORTED[index]
            val collection = vm.collectionsByType(type)

            val pullToRefreshState = rememberPullToRefreshState()
            SideEffect {
                collection.pullToRefreshState = pullToRefreshState
            }
            val gridState = rememberLazyGridState()
            val uiScope = rememberCoroutineScope()
            LaunchedEffect(true) {
                snapshotFlow { pullToRefreshState.isRefreshing }.collectLatest {
                    if (!it) return@collectLatest

                    val isAutoRefreshing = collection.isAutoRefreshing
                    try {
                        val policy = if (collection.isAutoRefreshing) {
                            RefreshOrderPolicy.KEEP_ORDER_APPEND_LAST
                        } else {
                            RefreshOrderPolicy.REPLACE
                        }
                        collection.isAutoRefreshing = false
                        collection.cache.refresh(policy)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Throwable) {
                    } finally {
                        pullToRefreshState.endRefresh()
                        if (!isAutoRefreshing) {
                            uiScope.launch {
                                gridState.animateScrollToItem(0)
                            }
                        }
                    }
                }
            }

            val autoUpdateScope = rememberUiMonoTasker()
            OnLifecycleEvent {
                if (it == Lifecycle.State.Active) {
                    autoUpdateScope.launch {
                        val lastUpdated = collection.cache.lastUpdated.first()
                        if (System.currentTimeMillis() - lastUpdated > 60.minutes.inWholeMilliseconds) {
                            collection.isAutoRefreshing = true
                            pullToRefreshState.startRefresh()
                        }
                    }
                }
            }

            val tabContentPadding = PaddingValues(
                top = topBarPaddings.calculateTopPadding() + contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
                start = 0.dp,
                end = 0.dp,
            )
            if (vm.authState.isKnownLoggedOut) {
                CollectionPageUnauthorizedTips(vm.authState, Modifier.padding(tabContentPadding))
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TabContent(
                        collection.subjectCollectionColumnState,
                        vm = vm,
                        type = type,
                        contentPadding = tabContentPadding,
                        modifier = Modifier
                            .nestedScroll(pullToRefreshState.nestedScrollConnection)
                            .fillMaxSize(),
                        enableAnimation = vm.myCollectionsSettings.enableListAnimation,
                        allowProgressIndicator = vm.authState.isKnownLoggedIn,
                    )
                    PullToRefreshContainer(
                        pullToRefreshState,
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(topBarPaddings.calculateTopPadding()),
                    )

                }
            }
        }
    }
}

@Composable
private fun CollectionPageUnauthorizedTips(
    authState: AuthState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.padding(top = 32.dp).padding(horizontal = 16.dp).fillMaxWidth().widthIn(max = 400.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val navigator = LocalNavigator.current
        Text("游客模式下请搜索后观看，或登录后使用收藏功能")

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton({ authState.launchAuthorize(navigator) }, Modifier.weight(1f)) {
                Text("登录")
            }

            Button({ navigator.navigateSearch() }, Modifier.weight(1f)) {
                Text("搜索")
            }
        }
    }
}

/**
 * @param contentPadding overall content padding
 */
@Composable
private fun TabContent(
    state: SubjectCollectionColumnState,
    vm: MyCollectionsViewModel,
    type: UnifiedCollectionType,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    enableAnimation: Boolean = true,
    allowProgressIndicator: Boolean = true,
) {
    // 同时设置所有剧集为看过
    var currentSetAllEpisodesDialogSubjectId by rememberSaveable { mutableStateOf<Int?>(null) }
    if (currentSetAllEpisodesDialogSubjectId != null) {
        SetAllEpisodeDoneDialog(
            onDismissRequest = { currentSetAllEpisodesDialogSubjectId = null },
            onConfirm = {
                currentSetAllEpisodesDialogSubjectId?.let {
                    vm.launchInBackground {
                        setAllEpisodesWatched(it)
                    }
                }
                currentSetAllEpisodesDialogSubjectId = null
            },
        )
    }

    SubjectCollectionsColumn(
        state = state,
        item = { subjectCollection ->
            var showEpisodeProgressDialog by rememberSaveable { mutableStateOf(false) }

            // 即使对话框不显示也加载, 避免打开对话框要等待一秒才能看到进度
            val episodeProgressState = rememberEpisodeProgressState(subjectCollection.subjectId)

            val navigator = LocalNavigator.current
            if (showEpisodeProgressDialog) {
                EpisodeProgressDialog(
                    episodeProgressState,
                    onDismissRequest = { showEpisodeProgressDialog = false },
                    actions = {
                        OutlinedButton({ navigator.navigateSubjectDetails(episodeProgressState.subjectId) }) {
                            Text("条目详情")
                        }
                    },
                )
            }

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
                onSetCollectionType = {
                    vm.launchInBackground { setCollectionType(subjectCollection.subjectId, it) }
                    if (it == UnifiedCollectionType.DONE) {
                        currentSetAllEpisodesDialogSubjectId = subjectCollection.subjectId
                    }
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
            Column(Modifier.padding(contentPadding).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("~ 空空如也 ~\n请点击 \"找番\" 收藏条目", style = MaterialTheme.typography.titleMedium)
            }
        },
        modifier,
        contentPadding = contentPadding,
        enableAnimation = enableAnimation,
        allowProgressIndicator = allowProgressIndicator,
    )
}

@Stable
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
