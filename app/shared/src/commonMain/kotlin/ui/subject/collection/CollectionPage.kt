/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.him188.ani.app.domain.session.AuthState
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.tools.rememberUiMonoTasker
import me.him188.ani.app.ui.adaptive.AniTopAppBar
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.interaction.WindowDragArea
import me.him188.ani.app.ui.foundation.layout.paneHorizontalPadding
import me.him188.ani.app.ui.foundation.pagerTabIndicatorOffset
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.app.ui.foundation.widgets.PullToRefreshBox
import me.him188.ani.app.ui.profile.SelfAvatar
import me.him188.ani.app.ui.subject.collection.components.SessionTipsArea
import me.him188.ani.app.ui.subject.collection.components.SessionTipsIcon
import me.him188.ani.app.ui.subject.collection.progress.SubjectProgressButton
import me.him188.ani.app.ui.subject.collection.progress.rememberEpisodeListState
import me.him188.ani.app.ui.subject.collection.progress.rememberSubjectProgressState
import me.him188.ani.app.ui.subject.episode.list.EpisodeListDialog
import me.him188.ani.app.ui.update.TextButtonUpdateLogo
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.isDesktop
import me.him188.ani.utils.platform.isMobile


// 有顺序, https://github.com/Him188/ani/issues/73
@Stable
val COLLECTION_TABS_SORTED = listOf(
    UnifiedCollectionType.DROPPED,
    UnifiedCollectionType.WISH,
    UnifiedCollectionType.DOING,
    UnifiedCollectionType.ON_HOLD,
    UnifiedCollectionType.DONE,
)

@Composable
fun CollectionPage(
    windowInsets: WindowInsets,
    modifier: Modifier = Modifier,
) {
    val vm = viewModel { MyCollectionsViewModel() } // TODO: remove vm
    vm.navigator = LocalNavigator.current

    val pagerState =
        rememberPagerState(initialPage = COLLECTION_TABS_SORTED.size / 2) { COLLECTION_TABS_SORTED.size }

    // 如果有缓存, 列表区域要展示缓存, 错误就用图标放在角落
    val showSessionErrorInList by remember(vm) {
        derivedStateOf {
            val collection = vm.collectionsByType(COLLECTION_TABS_SORTED[pagerState.currentPage])
            collection.subjectCollectionColumnState.isKnownAuthorizedAndEmpty
        }
    }
    CollectionPageLayout(
        windowInsets,
        pagerState,
        sessionError = {
            if (!showSessionErrorInList) {
                SessionTipsIcon(vm.authState)
            }
        },
        avatar = {
            SelfAvatar(vm.authState, vm.selfInfo)
        },
        filters = {
            CollectionTypeScrollableTabRow(
                pagerState,
                Modifier.padding(horizontal = currentWindowAdaptiveInfo().windowSizeClass.paneHorizontalPadding),
            ) { type ->
                val cache = vm.collectionsByType(type).cache
                val size by cache.totalSize.collectAsStateWithLifecycle(null)
                if (size == null) {
                    Text(
                        text = type.displayText(),
                        Modifier.width(IntrinsicSize.Max),
                        softWrap = false,
                    )
                } else {
                    Text(
                        text = remember(type, size) {
                            type.displayText() + " " + size
                        },
                        Modifier.width(IntrinsicSize.Max),
                        softWrap = false,
                    )
                }
            }
        },
        onRefresh = { type ->
            val collection = vm.collectionsByType(type)
            collection.subjectCollectionColumnState.manualRefresh()
        },
        modifier,
    ) { type ->
        val collection = vm.collectionsByType(type)

        val gridState = rememberLazyGridState()

        val autoUpdateScope = rememberUiMonoTasker()
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
            autoUpdateScope.launch {
                if (collection.shouldDoAutoRefresh()) {
                    collection.subjectCollectionColumnState.manualRefresh()
                    gridState.animateScrollToItem(0) // 手动刷新完成回到顶部
                }
            }
        }

        when {
            // 假设没登录, 但是有缓存, 需要展示缓存
            vm.authState.isKnownGuest && showSessionErrorInList -> {
                SessionTipsArea(
                    vm.authState,
                    guest = { GuestTips(vm.authState) },
                    Modifier.padding(top = 32.dp)
                        .padding(horizontal = 16.dp),
                )
            }

            collection.subjectCollectionColumnState.isKnownAuthorizedAndEmpty -> {
                Column(
                    Modifier.padding(top = 32.dp).padding(horizontal = 16.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SideEffect {
                        collection.subjectCollectionColumnState.requestMore()
                    }

                    Text("~ 空空如也 ~", style = MaterialTheme.typography.titleMedium)

                    val navigator = LocalNavigator.current
                    Button({ navigator.navigateSearch() }, Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.Search, null)
                        Text("搜索", Modifier.padding(start = 8.dp))
                    }
                }
            }

            else -> {
                PullToRefreshBox(
                    collection.subjectCollectionColumnState.isRefreshing,
                    onRefresh = {
                        collection.subjectCollectionColumnState.startAutoRefresh()
                    },
                    state = collection.pullToRefreshState,
                    enabled = LocalPlatform.current.isMobile(),
                    indicator = {
                        Indicator(
                            modifier = Modifier.align(Alignment.TopCenter),
                            isRefreshing = collection.subjectCollectionColumnState.isRefreshing,
                            state = collection.pullToRefreshState,
                        )
                    },
                ) {
                    TabContent(
                        collection.subjectCollectionColumnState,
                        vm = vm,
                        type = type,
                        enableAnimation = vm.myCollectionsSettings.enableListAnimation,
                        allowProgressIndicator = vm.authState.isKnownLoggedIn,
                    )
                }
            }
        }

    }
}

/**
 * @param filters see [CollectionPageFilters]
 */
@Composable
private fun CollectionPageLayout(
    windowInsets: WindowInsets,
    pagerState: PagerState,
    sessionError: @Composable () -> Unit,
    avatar: @Composable () -> Unit,
    filters: @Composable CollectionPageFilters.() -> Unit,
    onRefresh: suspend (UnifiedCollectionType) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (type: UnifiedCollectionType) -> Unit,
) {
    Scaffold(
        modifier,
        topBar = {
            val topAppBarColors = AniThemeDefaults.topAppBarColors()
            Column(modifier = Modifier.fillMaxWidth()) {
                WindowDragArea {
                    AniTopAppBar(
                        title = { Text("追番") },
                        windowInsets = windowInsets.only(WindowInsetsSides.Top),
                        modifier = Modifier,
                        actions = {
                            sessionError()

                            TextButtonUpdateLogo()

                            if (LocalPlatform.current.isDesktop()) {
                                // PC 无法下拉刷新
                                val refreshTasker = rememberUiMonoTasker()
                                IconButton(
                                    {
                                        val type = COLLECTION_TABS_SORTED[pagerState.currentPage]
                                        if (!refreshTasker.isRunning) {
                                            refreshTasker.launch {
                                                onRefresh(type)
                                            }
                                        }
                                    },
                                ) {
                                    Icon(Icons.Rounded.Refresh, null)
                                }
                            }
                        },
                        avatar = avatar,
                        colors = topAppBarColors,
                    )
                }

                filters(CollectionPageFilters)
            }
        },
        contentWindowInsets = windowInsets.only(WindowInsetsSides.Top),
        containerColor = AniThemeDefaults.pageContentBackgroundColor,
    ) { topBarPaddings ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(topBarPaddings),
            userScrollEnabled = LocalPlatform.current.isMobile(),
            verticalAlignment = Alignment.Top,
        ) { index ->
            val type = COLLECTION_TABS_SORTED[index]
            Box(modifier = Modifier.fillMaxSize()) {
                content(type)
            }
        }
    }
}

@Stable
object CollectionPageFilters {
    @Composable
    fun CollectionTypeFilterButtons(
        pagerState: PagerState,
        modifier: Modifier = Modifier,
        itemLabel: @Composable (UnifiedCollectionType) -> Unit = { type ->
            Text(type.displayText(), softWrap = false)
        },
    ) {
        val uiScope = rememberCoroutineScope()
        SingleChoiceSegmentedButtonRow(modifier) {
            COLLECTION_TABS_SORTED.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = pagerState.currentPage == index,
                    onClick = { uiScope.launch { pagerState.scrollToPage(index) } },
                    shape = SegmentedButtonDefaults.itemShape(index, COLLECTION_TABS_SORTED.size),
                    Modifier.wrapContentWidth(),
                ) {
                    itemLabel(type)
                }
            }
        }
    }

    @Composable
    fun CollectionTypeScrollableTabRow(
        pagerState: PagerState,
        modifier: Modifier = Modifier,
        itemLabel: @Composable (UnifiedCollectionType) -> Unit = { type ->
            Text(type.displayText(), softWrap = false)
        },
    ) {
        val uiScope = rememberCoroutineScope()
        val widths = rememberSaveable { mutableStateListOf(*COLLECTION_TABS_SORTED.map { 24.dp }.toTypedArray()) }
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = @Composable { tabPositions ->
                TabRowDefaults.PrimaryIndicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                    width = widths[pagerState.currentPage],
                )
            },
            containerColor = Color.Unspecified,
            contentColor = MaterialTheme.colorScheme.onSurface,
            divider = {},
            modifier = modifier.fillMaxWidth(),
        ) {
            COLLECTION_TABS_SORTED.forEachIndexed { index, collectionType ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { uiScope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        val density = LocalDensity.current
                        Box(Modifier.onPlaced { widths[index] = with(density) { it.size.width.toDp() } }) {
                            itemLabel(collectionType)
                        }
                    },
                )
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
                colors = AniThemeDefaults.primaryCardColors(),
            )
        },
        modifier,
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
