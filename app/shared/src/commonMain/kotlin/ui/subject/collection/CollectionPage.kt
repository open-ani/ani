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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.HowToReg
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
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
import androidx.compose.runtime.derivedStateOf
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.him188.ani.app.data.source.session.AuthState
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.currentPlatform
import me.him188.ani.app.platform.isDesktop
import me.him188.ani.app.platform.isMobile
import me.him188.ani.app.tools.caching.RefreshOrderPolicy
import me.him188.ani.app.tools.rememberUiMonoTasker
import me.him188.ani.app.ui.foundation.layout.isShowLandscapeUI
import me.him188.ani.app.ui.foundation.pagerTabIndicatorOffset
import me.him188.ani.app.ui.subject.collection.components.SessionTipsArea
import me.him188.ani.app.ui.subject.collection.components.SessionTipsIcon
import me.him188.ani.app.ui.subject.collection.progress.SubjectProgressButton
import me.him188.ani.app.ui.subject.collection.progress.rememberEpisodeListState
import me.him188.ani.app.ui.subject.collection.progress.rememberSubjectProgressState
import me.him188.ani.app.ui.subject.episode.list.EpisodeListDialog
import me.him188.ani.app.ui.update.TextButtonUpdateLogo
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.currentTimeMillis
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
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
) {
    val vm = viewModel { MyCollectionsViewModel() }
    vm.navigator = LocalNavigator.current

    val pagerState =
        rememberPagerState(initialPage = COLLECTION_TABS_SORTED.size / 2) { COLLECTION_TABS_SORTED.size }
    val scope = rememberCoroutineScope()

    // 如果有缓存, 列表区域要展示缓存, 错误就用图标放在角落
    val showSessionErrorInList by remember(vm) {
        derivedStateOf {
            val collection = vm.collectionsByType(COLLECTION_TABS_SORTED[pagerState.currentPage])
            collection.subjectCollectionColumnState.isKnownEmpty
        }
    }

    Scaffold(
        modifier,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = { Text("我的追番") },
                    modifier = Modifier.alpha(0.97f),
                    actions = {
                        if (!showSessionErrorInList) {
                            SessionTipsIcon(vm.authState)
                        }

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
        contentWindowInsets = contentWindowInsets,
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
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                autoUpdateScope.launch {
                    val lastUpdated = collection.cache.lastUpdated.first()
                    if (currentTimeMillis() - lastUpdated > 60.minutes.inWholeMilliseconds) {
                        collection.isAutoRefreshing = true
                        pullToRefreshState.startRefresh()
                    }
                }
            }

            val tabContentPadding = PaddingValues(
                top = topBarPaddings.calculateTopPadding() + contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
                start = 0.dp,
                end = 0.dp,
            )
            if (vm.authState.isKnownLoggedOut && showSessionErrorInList) {
                SessionTipsArea(
                    vm.authState,
                    guest = { GuestTips(vm.authState) },
                    Modifier.padding(top = 32.dp)
                        .padding(horizontal = 16.dp)
                        .padding(tabContentPadding),
                )
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
    SubjectCollectionsColumn(
        state = state,
        item = { subjectCollection ->
            var showEpisodeProgressDialog by rememberSaveable { mutableStateOf(false) }

            // 即使对话框不显示也加载, 避免打开对话框要等待一秒才能看到进度
            val episodeProgressState = vm.episodeListStateFactory
                .rememberEpisodeListState(subjectCollection.subjectId)

            val navigator = LocalNavigator.current
            if (showEpisodeProgressDialog) {
                EpisodeListDialog(
                    episodeProgressState,
                    title = {
                        Text(subjectCollection.displayName)
                    },
                    onDismissRequest = { showEpisodeProgressDialog = false },
                    actions = {
                        OutlinedButton({ navigator.navigateSubjectDetails(subjectCollection.subjectId) }) {
                            Text("条目详情")
                        }
                    },
                )
            }

            val subjectProgressState = vm.subjectProgressStateFactory
                .rememberSubjectProgressState(subjectCollection)

            val editableSubjectCollectionTypeState = remember(vm) {
                vm.createEditableSubjectCollectionTypeState(subjectCollection)
            }

            SubjectCollectionItem(
                subjectCollection,
                editableSubjectCollectionTypeState = editableSubjectCollectionTypeState,
                onClick = {
                    navigator.navigateSubjectDetails(subjectCollection.subjectId)
                },
                onShowEpisodeList = {
                    showEpisodeProgressDialog = true
                },
                playButton = {
                    if (type != UnifiedCollectionType.DONE) {
                        if (subjectProgressState.isDone) {
                            FilledTonalButton(
                                {
                                    editableSubjectCollectionTypeState.setSelfCollectionType(UnifiedCollectionType.DONE)
                                },
                                enabled = !editableSubjectCollectionTypeState.isSetSelfCollectionTypeWorking,
                            ) {
                                Text("移至\"看过\"", Modifier.requiredWidth(IntrinsicSize.Max), softWrap = false)
                            }
                        } else {
                            SubjectProgressButton(
                                subjectProgressState,
                            )
                        }
                    }
                },
            )
        },
        onEmpty = {
            Column(
                Modifier.padding(top = 32.dp).padding(contentPadding).padding(horizontal = 16.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SideEffect {
                    state.requestMore()
                }

                Text("~ 空空如也 ~", style = MaterialTheme.typography.titleMedium)

                val navigator = LocalNavigator.current
                Button({ navigator.navigateSearch() }, Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Search, null)
                    Text("搜索", Modifier.padding(start = 8.dp))
                }
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


@Composable
private fun GuestTips(
    authState: AuthState,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        val navigator = LocalNavigator.current
        Text("游客模式下请搜索后观看，或登录后使用收藏功能")

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton({ authState.launchAuthorize(navigator) }, Modifier.weight(1f)) {
                Icon(Icons.Rounded.HowToReg, null)
                Text("登录", Modifier.padding(start = 8.dp))
            }

            Button({ navigator.navigateSearch() }, Modifier.weight(1f)) {
                Icon(Icons.Rounded.Search, null)
                Text("搜索", Modifier.padding(start = 8.dp))
            }
        }
    }
}
