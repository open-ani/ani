package me.him188.ani.app.ui.subject.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.BottomSheetDefaults
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import me.him188.ani.app.navigation.LocalBrowserNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.currentPlatform
import me.him188.ani.app.platform.isDesktop
import me.him188.ani.app.platform.isMobile
import me.him188.ani.app.platform.navigation.BackHandler
import me.him188.ani.app.ui.foundation.ImageViewer
import me.him188.ani.app.ui.foundation.LocalImageViewerHandler
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.interaction.nestedScrollWorkaround
import me.him188.ani.app.ui.foundation.layout.ConnectedScrollState
import me.him188.ani.app.ui.foundation.layout.connectedScrollContainer
import me.him188.ani.app.ui.foundation.layout.connectedScrollTarget
import me.him188.ani.app.ui.foundation.layout.rememberConnectedScrollState
import me.him188.ani.app.ui.foundation.pagerTabIndicatorOffset
import me.him188.ani.app.ui.foundation.rememberImageViewerHandler
import me.him188.ani.app.ui.foundation.richtext.RichTextDefaults
import me.him188.ani.app.ui.foundation.widgets.FastLinearProgressIndicator
import me.him188.ani.app.ui.foundation.widgets.FastLinearProgressState
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.TopAppBarGoBackButton
import me.him188.ani.app.ui.subject.collection.EditableSubjectCollectionTypeButton
import me.him188.ani.app.ui.subject.details.components.CollectionData
import me.him188.ani.app.ui.subject.details.components.DetailsTab
import me.him188.ani.app.ui.subject.details.components.SelectEpisodeButtons
import me.him188.ani.app.ui.subject.details.components.SubjectBlurredBackground
import me.him188.ani.app.ui.subject.details.components.SubjectCommentColumn
import me.him188.ani.app.ui.subject.details.components.SubjectDetailsDefaults
import me.him188.ani.app.ui.subject.details.components.SubjectDetailsHeader
import me.him188.ani.app.ui.subject.episode.list.EpisodeListDialog
import me.him188.ani.app.ui.subject.rating.EditableRating

@Composable
fun SubjectDetailsScene(
    vm: SubjectDetailsViewModel,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
    showBlurredBackground: Boolean = true,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
    val context = LocalContext.current
    val toaster = LocalToaster.current
    val browserNavigator = LocalBrowserNavigator.current

    var showSelectEpisode by rememberSaveable { mutableStateOf(false) }
    if (showSelectEpisode) {
        EpisodeListDialog(
            vm.episodeListState,
            title = {
                Text(vm.subjectDetailsState.info.displayName)
            },
            onDismissRequest = { showSelectEpisode = false },
        )
    }
    val connectedScrollState = rememberConnectedScrollState()

    val commentPullToRefreshState = rememberPullToRefreshState()
    // image viewer
    val imageViewer = rememberImageViewerHandler()
    BackHandler(enabled = imageViewer.viewing.value) { imageViewer.clear() }

    SubjectDetailsPage(
        vm.subjectDetailsState,
        onClickOpenExternal = { vm.browseSubjectBangumi(context) },
        collectionData = {
            SubjectDetailsDefaults.CollectionData(
                collectionStats = vm.subjectDetailsState.info.collection,
            )
        },
        collectionActions = {
            if (vm.authState.isKnownExpired) {
                val navigator = LocalNavigator.current
                OutlinedButton({ vm.authState.launchAuthorize(navigator) }) {
                    Text("登录后可收藏")
                }
            } else {
                EditableSubjectCollectionTypeButton(vm.editableSubjectCollectionTypeState)
            }
        },
        rating = {
            EditableRating(vm.editableRatingState)
        },
        selectEpisodeButton = {
            SubjectDetailsDefaults.SelectEpisodeButtons(
                vm.subjectProgressState,
                onShowEpisodeList = { showSelectEpisode = true },
            )
        },
        connectedScrollState = connectedScrollState,
        detailsTab = {
            SubjectDetailsDefaults.DetailsTab(
                info = vm.subjectDetailsState.info,
                staff = vm.subjectDetailsState.persons,
                characters = vm.subjectDetailsState.characters,
                relatedSubjects = vm.subjectDetailsState.relatedSubjects,
                Modifier
                    .ifThen(currentPlatform.isDesktop()) {
                        nestedScrollWorkaround(vm.detailsTabLazyListState, connectedScrollState.nestedScrollConnection)
                    }
                    .nestedScroll(connectedScrollState.nestedScrollConnection),
                vm.detailsTabLazyListState,
            )
        },
        commentsTab = {
            CompositionLocalProvider(LocalImageViewerHandler provides imageViewer) {
                SubjectDetailsDefaults.SubjectCommentColumn(
                    state = vm.subjectCommentState,
                    listState = vm.commentTabLazyListState,
                    pullToRefreshState = commentPullToRefreshState,
                    modifier = Modifier
                        .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                        .ifThen(currentPlatform.isDesktop()) {
                            nestedScrollWorkaround(
                                vm.commentTabLazyListState,
                                connectedScrollState.nestedScrollConnection,
                            )
                        }
                        .nestedScroll(connectedScrollState.nestedScrollConnection),
                    onClickUrl = {
                        RichTextDefaults.checkSanityAndOpen(it, context, browserNavigator, toaster)
                    },
                )
            }
        },
        discussionsTab = {
            LazyColumn(Modifier.fillMaxSize().nestedScroll(connectedScrollState.nestedScrollConnection)) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("即将上线, 敬请期待", Modifier.padding(16.dp))
                    }
                }
            }
        },
        modifier,
        showTopBar = showTopBar,
        showBlurredBackground = showBlurredBackground,
        windowInsets = windowInsets,
    )

    ImageViewer(imageViewer) { imageViewer.clear() }
}

@Immutable
enum class SubjectDetailsTab {
    DETAILS,
    COMMENTS,
    DISCUSSIONS,
}

/**
 * 一部番的详情页
 */
@Composable
fun SubjectDetailsPage(
    state: SubjectDetailsState,
    onClickOpenExternal: () -> Unit,
    collectionData: @Composable () -> Unit,
    collectionActions: @Composable () -> Unit,
    rating: @Composable () -> Unit,
    selectEpisodeButton: @Composable BoxScope.() -> Unit,
    connectedScrollState: ConnectedScrollState,
    detailsTab: @Composable () -> Unit,
    commentsTab: @Composable () -> Unit,
    discussionsTab: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
    showBlurredBackground: Boolean = true,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
    val scope = rememberCoroutineScope()
    val indicatorState = remember(scope) { FastLinearProgressState(scope) }

    indicatorState.setVisible(state.isLoading, 0, 300)

    // 出场动画
    var isContentReady by remember { mutableStateOf(!state.isLoading) }
    LaunchedEffect(indicatorState) {
        indicatorState.awaitCompletion()
        isContentReady = true
    }

    Scaffold(
        topBar = {
            if (showTopBar) {
                Box {
                    // 透明背景的, 总是显示
                    TopAppBar(
                        title = {},
                        navigationIcon = { TopAppBarGoBackButton() },
                        actions = {
                            IconButton(onClickOpenExternal) {
                                Icon(Icons.AutoMirrored.Outlined.OpenInNew, null)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        windowInsets = windowInsets,
                    )

                    // 有背景, 仅在滚动一段距离后使用
                    AnimatedVisibility(connectedScrollState.isScrolledTop, enter = fadeIn(), exit = fadeOut()) {
                        TopAppBar(
                            title = { Text(state.info.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            navigationIcon = { TopAppBarGoBackButton() },
                            actions = {
                                IconButton(onClickOpenExternal) {
                                    Icon(Icons.AutoMirrored.Outlined.OpenInNew, null)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(),
                            windowInsets = windowInsets,
                        )
                    }
                }
            }
        },
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp),
    ) { scaffoldPadding ->
        FastLinearProgressIndicator(
            indicatorState,
            Modifier.zIndex(2f)
                .ifThen(!showTopBar) { padding(top = 4.dp) }
                .padding(scaffoldPadding).padding(horizontal = 4.dp).fillMaxWidth(),
        )

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            AnimatedVisibility(
                isContentReady,
                Modifier.wrapContentSize(),
                // 从中间往上滑
                enter = fadeIn(tween(500)) + slideInVertically(
                    tween(600),
                    initialOffsetY = { 150.coerceAtMost(it) },
                ),
            ) {
                Column(Modifier.widthIn(max = 1300.dp).fillMaxHeight()) {
                    Box(Modifier.connectedScrollContainer(connectedScrollState)) {
                        // 虚化渐变背景
                        if (showBlurredBackground) {
                            SubjectBlurredBackground(
                                coverImageUrl = if (isContentReady) state.coverImageUrl else null,
                                Modifier.matchParentSize(),
                            )
                        }

                        // 标题和封面, 以及收藏数据, 可向上滑动
                        Column(
                            Modifier
                                .padding(scaffoldPadding)
                                .connectedScrollTarget(connectedScrollState),
                        ) {
                            SubjectDetailsHeader(
                                state.info,
                                state.coverImageUrl,
                                airingLabelState = state.airingLabelState,
                                collectionData = collectionData,
                                collectionAction = collectionActions,
                                selectEpisodeButton = selectEpisodeButton,
                                rating = rating,
                                Modifier.fillMaxWidth()
                                    .ifThen(!showTopBar) { padding(top = 16.dp) }
                                    .padding(horizontal = 16.dp),
                            )
                        }
                    }
                    val pagerState = rememberPagerState(
                        initialPage = SubjectDetailsTab.DETAILS.ordinal,
                        pageCount = { 3 },
                    )

                    // Pager with TabRow
                    Column(
                        Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxHeight(),
                    ) {
                        val tabContainerColor by animateColorAsState(
                            if (connectedScrollState.isScrolledTop) TabRowDefaults.secondaryContainerColor else MaterialTheme.colorScheme.background,
                            tween(),
                        )
                        SecondaryScrollableTabRow(
                            selectedTabIndex = pagerState.currentPage,
                            indicator = @Composable { tabPositions ->
                                TabRowDefaults.PrimaryIndicator(
                                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                                )
                            },
                            containerColor = tabContainerColor,
                            divider = {},
                            modifier = Modifier,
                        ) {
                            SubjectDetailsTab.entries.forEachIndexed { index, tabId ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                    text = {
                                        Text(text = renderSubjectDetailsTab(tabId))
                                    },
                                )
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            Modifier.fillMaxHeight(),
                            userScrollEnabled = Platform.currentPlatform.isMobile(),
                            verticalAlignment = Alignment.Top,
                        ) { index ->
                            val type = SubjectDetailsTab.entries[index]
                            Column(Modifier.padding()) {
                                when (type) {
                                    SubjectDetailsTab.DETAILS -> detailsTab()
                                    SubjectDetailsTab.COMMENTS -> commentsTab()
                                    SubjectDetailsTab.DISCUSSIONS -> discussionsTab()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Stable
private fun renderSubjectDetailsTab(tab: SubjectDetailsTab): String {
    return when (tab) {
        SubjectDetailsTab.DETAILS -> "详情"
        SubjectDetailsTab.COMMENTS -> "评论"
        SubjectDetailsTab.DISCUSSIONS -> "讨论"
    }
}
