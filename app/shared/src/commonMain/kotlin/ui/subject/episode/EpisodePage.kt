package me.him188.ani.app.ui.subject.episode

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.him188.ani.app.data.source.danmaku.protocol.DanmakuInfo
import me.him188.ani.app.data.source.danmaku.protocol.DanmakuLocation
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.setRequestFullScreen
import me.him188.ani.app.platform.window.LocalPlatformWindow
import me.him188.ani.app.tools.rememberUiMonoTasker
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.ImageViewer
import me.him188.ani.app.ui.foundation.LocalImageViewerHandler
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.effects.OnLifecycleEvent
import me.him188.ani.app.ui.foundation.effects.ScreenOnEffect
import me.him188.ani.app.ui.foundation.effects.ScreenRotationEffect
import me.him188.ani.app.ui.foundation.isInDebugMode
import me.him188.ani.app.ui.foundation.layout.LocalLayoutMode
import me.him188.ani.app.ui.foundation.pagerTabIndicatorOffset
import me.him188.ani.app.ui.foundation.rememberImageViewerHandler
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.foundation.theme.weaken
import me.him188.ani.app.ui.subject.episode.comments.EpisodeCommentColumn
import me.him188.ani.app.ui.subject.episode.danmaku.DanmakuEditor
import me.him188.ani.app.ui.subject.episode.danmaku.DummyDanmakuEditor
import me.him188.ani.app.ui.subject.episode.details.EpisodeDetails
import me.him188.ani.app.ui.subject.episode.notif.VideoNotifEffect
import me.him188.ani.app.ui.subject.episode.video.VideoDanmakuState
import me.him188.ani.app.ui.subject.episode.video.sidesheet.EpisodeSelectorSideSheet
import me.him188.ani.app.ui.subject.episode.video.sidesheet.EpisodeVideoMediaSelectorSideSheet
import me.him188.ani.app.ui.subject.episode.video.topbar.EpisodePlayerTitle
import me.him188.ani.app.videoplayer.ui.ControllerVisibility
import me.him188.ani.app.videoplayer.ui.VideoControllerState
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerDefaults
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerDefaults.randomDanmakuPlaceholder
import me.him188.ani.app.videoplayer.ui.progress.rememberMediaProgressSliderState
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.navigation.BackHandler


/**
 * 番剧详情 (播放) 页面
 */
@Composable
fun EpisodeScene(
    viewModel: EpisodeViewModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
        ) {
            EpisodeSceneContent(
                viewModel,
                Modifier,
            )
        }
    }
}

@Composable
private fun EpisodeSceneContent(
    vm: EpisodeViewModel,
    modifier: Modifier = Modifier,
) {
    // 处理当用户点击返回键时, 如果是全屏, 则退出全屏
    val navigator = LocalNavigator.current
    BackHandler {
        vm.stopPlaying()
        navigator.navigator.goBack()
    }

    // 按返回退出全屏
    val context by rememberUpdatedState(LocalContext.current)
    val window = LocalPlatformWindow.current
    BackHandler(enabled = vm.isFullscreen) {
        context.setRequestFullScreen(window, false)
        vm.isFullscreen = false
    }

    // image viewer
    val imageViewer = rememberImageViewerHandler()
    BackHandler(enabled = imageViewer.viewing.value) { imageViewer.clear() }

    val playbackState by vm.playerState.state.collectAsStateWithLifecycle()
    if (playbackState.isPlaying) {
        ScreenOnEffect()
    }

    AutoPauseEffect(vm)

    VideoNotifEffect(vm)

    if (vm.videoScaffoldConfig.autoFullscreenOnLandscapeMode && isInDebugMode()) {
        ScreenRotationEffect {
            vm.isFullscreen = it
        }
    }

    BoxWithConstraints(modifier) {
        val layoutMode by rememberUpdatedState(LocalLayoutMode.current)
        val isVeryWide by remember {
            derivedStateOf { layoutMode.deviceSize.width / layoutMode.deviceSize.height >= 1200f / 770 }
        }
        CompositionLocalProvider(LocalImageViewerHandler provides imageViewer) {
            when {
                isVeryWide || layoutMode.showLandscapeUI -> EpisodeSceneTabletVeryWide(vm, Modifier.fillMaxSize())
                else -> EpisodeSceneContentPhone(vm, Modifier.fillMaxSize())
            }
        }
        ImageViewer(imageViewer) { imageViewer.clear() }
    }

    vm.videoSourceResolver.ComposeContent()
}

@Composable
private fun EpisodeSceneTabletVeryWide(
    vm: EpisodeViewModel,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints {
        val maxWidth = maxWidth
        Row(
            modifier
                .then(
                    if (vm.isFullscreen) Modifier.fillMaxSize()
                    else Modifier.navigationBarsPadding(),
                ),
        ) {
            EpisodeVideo(
                vm,
                expanded = true,
                maintainAspectRatio = false,
                initialControllerVisibility = ControllerVisibility.Visible,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )

            if (vm.isFullscreen) {
                return@Row
            }

            val pagerState = rememberPagerState(initialPage = 0) { 2 }
            val scope = rememberCoroutineScope()

            Column(Modifier.width(width = (maxWidth * 0.25f).coerceIn(340.dp, 460.dp))) {
                TabRow(pagerState, scope, { vm.episodeCommentState.count }, Modifier.fillMaxWidth())
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.weaken())

                HorizontalPager(state = pagerState, Modifier.fillMaxSize()) { index ->
                    when (index) {
                        0 -> Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            EpisodeDetails(
                                vm.episodeDetailsState,
                                vm.episodeCarouselState,
                                vm.editableSubjectCollectionTypeState,
                                vm.danmakuStatistics,
                                vm.videoStatistics,
                                vm.mediaSelectorPresentation,
                                vm.mediaSourceResultsPresentation,
                                vm.authState,
                            )
                        }

                        1 -> EpisodeCommentColumn(vm.episodeCommentState, Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
private fun TabRow(
    pagerState: PagerState,
    scope: CoroutineScope,
    commentCount: () -> Int?,
    modifier: Modifier = Modifier,
) {
    SecondaryScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        modifier,
        indicator = @Composable { tabPositions ->
            TabRowDefaults.PrimaryIndicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        edgePadding = 0.dp,
        divider = {},
    ) {
        Tab(
            selected = pagerState.currentPage == 0,
            onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
            text = { Text("详情", softWrap = false) },
            selectedContentColor = MaterialTheme.colorScheme.primary,
            unselectedContentColor = MaterialTheme.colorScheme.onSurface,
        )
        Tab(
            selected = pagerState.currentPage == 1,
            onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
            text = {
                val text by remember(commentCount) {
                    derivedStateOf {
                        val count = commentCount()
                        if (count == null) "评论" else "评论 $count"
                    }
                }
                Text(text, softWrap = false)
            },
            selectedContentColor = MaterialTheme.colorScheme.primary,
            unselectedContentColor = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun EpisodeSceneContentPhone(
    vm: EpisodeViewModel,
    modifier: Modifier = Modifier,
) {
    var showDanmakuEditor by rememberSaveable { mutableStateOf(false) }
    var didSetPaused by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(true) {
        vm.episodeCommentState.reload()
    }

    EpisodeSceneContentPhoneScaffold(
        videoOnly = vm.isFullscreen,
        commentCount = { vm.episodeCommentState.count },
        video = {
            EpisodeVideo(vm, vm.isFullscreen, Modifier)
        },
        episodeDetails = {
            EpisodeDetails(
                vm.episodeDetailsState,
                vm.episodeCarouselState,
                vm.editableSubjectCollectionTypeState,
                vm.danmakuStatistics,
                vm.videoStatistics,
                vm.mediaSelectorPresentation,
                vm.mediaSourceResultsPresentation,
                vm.authState,
                Modifier.fillMaxSize(),
            )
        },
        commentColumn = {
            EpisodeCommentColumn(vm.episodeCommentState, Modifier.fillMaxSize())
        },
        modifier.then(if (vm.isFullscreen) Modifier.fillMaxSize() else Modifier.navigationBarsPadding()),
        tabRowContent = {
            DummyDanmakuEditor(
                onClick = {
                    showDanmakuEditor = true
                    if (vm.videoScaffoldConfig.pauseVideoOnEditDanmaku && vm.playerState.state.value.isPlaying) {
                        didSetPaused = true
                        vm.playerState.pause()
                    } else {
                        didSetPaused = false
                    }
                },
            )
        },
    )

    if (showDanmakuEditor) {
        val focusRequester = remember { FocusRequester() }
        val dismiss = {
            showDanmakuEditor = false
            if (didSetPaused) {
                didSetPaused = false
                vm.playerState.resume()
            }
        }
        ModalBottomSheet(
            onDismissRequest = dismiss,
        ) {
            DetachedDanmakuEditorLayout(
                vm.danmaku,
                onSend = { text ->
                    vm.danmaku.danmakuEditorText = ""
                    vm.danmaku.sendAsync(
                        DanmakuInfo(
                            vm.playerState.getExactCurrentPositionMillis(),
                            text = text,
                            color = Color.White.toArgb(),
                            location = DanmakuLocation.NORMAL,
                        ),
                    ) {
                        dismiss()
                    }
                },
                focusRequester,
                Modifier.imePadding(),
            )
        }
        LaunchedEffect(true) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
private fun DetachedDanmakuEditorLayout(
    videoDanmakuState: VideoDanmakuState,
    onSend: (text: String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(all = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("发送弹幕", style = MaterialTheme.typography.titleMedium)
        DanmakuEditor(
            text = videoDanmakuState.danmakuEditorText,
            onTextChange = { videoDanmakuState.danmakuEditorText = it },
            isSending = videoDanmakuState.isSending,
            placeholderText = remember { randomDanmakuPlaceholder() },
            onSend = onSend,
            Modifier.fillMaxWidth().focusRequester(focusRequester),
            colors = OutlinedTextFieldDefaults.colors(),
        )
    }
}

@Composable
fun EpisodeSceneContentPhoneScaffold(
    videoOnly: Boolean,
    commentCount: () -> Int?,
    video: @Composable () -> Unit,
    episodeDetails: @Composable () -> Unit,
    commentColumn: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    tabRowContent: @Composable () -> Unit = {},
) {
    Column(modifier) {
        video()

        if (videoOnly) {
            return@Column
        }

        val pagerState = rememberPagerState(initialPage = 0) { 2 }
        val scope = rememberCoroutineScope()

        Column(Modifier.fillMaxSize()) {
            Row {
                TabRow(pagerState, scope, commentCount, Modifier.weight(1f))
                Box(
                    modifier = Modifier.weight(0.618f) // width
                        .height(48.dp)
                        .padding(vertical = 4.dp, horizontal = 16.dp),
                ) {
                    Row(Modifier.align(Alignment.CenterEnd)) {
                        tabRowContent()
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.weaken())

            HorizontalPager(state = pagerState, Modifier.fillMaxSize()) { index ->
                when (index) {
                    0 -> Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        episodeDetails()
                    }

                    1 -> Box(Modifier.fillMaxSize()) {
                        commentColumn()
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeVideo(
    vm: EpisodeViewModel,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    maintainAspectRatio: Boolean = !expanded,
    initialControllerVisibility: ControllerVisibility = ControllerVisibility.Invisible,
) {
    val context by rememberUpdatedState(LocalContext.current)

    // Don't rememberSavable. 刻意让每次切换都是隐藏的
    val videoControllerState = remember { VideoControllerState(initialControllerVisibility) }
    val videoDanmakuState = vm.danmaku
    var isMediaSelectorVisible by remember { mutableStateOf(false) }
    var isEpisodeSelectorVisible by remember { mutableStateOf(false) }


    // Refresh every time on configuration change (i.e. switching theme, entering fullscreen)
    val danmakuTextPlaceholder = remember { randomDanmakuPlaceholder() }
    val window = LocalPlatformWindow.current

    val progressSliderState = rememberMediaProgressSliderState(
        vm.playerState,
        onPreview = {
            // not yet supported
        },
        onPreviewFinished = {
            vm.playerState.seekTo(it)
        },
    )

    EpisodeVideoImpl(
        vm.playerState,
        expanded = expanded,
        hasNextEpisode = vm.episodeSelectorState.hasNextEpisode,
        onClickNextEpisode = { vm.episodeSelectorState.selectNext() },
        videoControllerState = videoControllerState,
        title = {
            val episode = vm.episodePresentation
            val subject = vm.subjectPresentation
            EpisodePlayerTitle(
                episode.ep,
                episode.title,
                subject.title,
                Modifier.placeholder(episode.isPlaceholder || subject.isPlaceholder),
            )
        },
        danmakuHostState = videoDanmakuState.danmakuHostState,
        danmakuEnabled = videoDanmakuState.currentEnabled,
        onToggleDanmaku = { videoDanmakuState.currentEnabled = !videoDanmakuState.currentEnabled },
        videoLoadingState = { vm.videoStatistics.videoLoadingState },
        danmakuConfig = { videoDanmakuState.config },
        onClickFullScreen = {
            if (vm.isFullscreen) {
                context.setRequestFullScreen(window, false)
                vm.isFullscreen = false
            } else {
                vm.isFullscreen = true
                context.setRequestFullScreen(window, true)
            }
        },
        onExitFullscreen = {
            context.setRequestFullScreen(window, false)
            vm.isFullscreen = false
        },
        danmakuEditor = {
            val danmakuEditorRequester = remember { Any() }

            /**
             * 是否设置了暂停
             */
            var didSetPaused by rememberSaveable { mutableStateOf(false) }

            DanmakuEditor(
                text = videoDanmakuState.danmakuEditorText,
                onTextChange = { videoDanmakuState.danmakuEditorText = it },
                isSending = videoDanmakuState.isSending,
                placeholderText = danmakuTextPlaceholder,
                onSend = { text ->
                    videoDanmakuState.danmakuEditorText = ""
                    videoDanmakuState.sendAsync(
                        DanmakuInfo(
                            vm.playerState.getExactCurrentPositionMillis(),
                            text = text,
                            color = Color.White.toArgb(),
                            location = DanmakuLocation.NORMAL,
                        ),
                    )
                },
                modifier = Modifier.onFocusChanged {
                    if (it.isFocused) {
                        if (vm.videoScaffoldConfig.pauseVideoOnEditDanmaku && vm.playerState.state.value.isPlaying) {
                            didSetPaused = true
                            vm.playerState.pause()
                        }
                        videoControllerState.setRequestAlwaysOn(danmakuEditorRequester, true)
                    } else {
                        if (didSetPaused) {
                            didSetPaused = false
                            vm.playerState.resume()
                        }
                        videoControllerState.setRequestAlwaysOn(danmakuEditorRequester, false)
                    }
                }.weight(1f),
            )
        },
        configProvider = remember(vm) { { vm.videoScaffoldConfig } },
        modifier = modifier.fillMaxWidth().background(Color.Black)
            .then(if (expanded) Modifier.fillMaxSize() else Modifier.statusBarsPadding()),
        maintainAspectRatio = maintainAspectRatio,
        sideSheets = {
            if (isMediaSelectorVisible) {
                EpisodeVideoMediaSelectorSideSheet(
                    vm.mediaSelectorPresentation,
                    vm.mediaSourceResultsPresentation,
                    onDismissRequest = { isMediaSelectorVisible = false },
                )
            }
            if (isEpisodeSelectorVisible) {
                EpisodeSelectorSideSheet(
                    vm.episodeSelectorState,
                    onDismissRequest = { isEpisodeSelectorVisible = false },
                )
            }
        },
        onShowMediaSelector = { isMediaSelectorVisible = true },
        onShowSelectEpisode = { isEpisodeSelectorVisible = true },
        onClickScreenshot = {
            val currentPositionMillis = vm.playerState.currentPositionMillis.value
            val min = currentPositionMillis / 60000
            val sec = (currentPositionMillis - (min * 60000)) / 1000
            val ms = currentPositionMillis - (min * 60000) - (sec * 1000)
            val currentPosition = "${min}m${sec}s${ms}ms"
            // 条目ID-剧集序号-视频时间点.png
            val filename = "${vm.subjectId}-${vm.episodePresentation.ep}-${currentPosition}.png"
            vm.playerState.saveScreenshotFile(filename)
        },
        detachedProgressSlider = {
            PlayerControllerDefaults.MediaProgressSlider(
                progressSliderState,
                vm.playerState,
                Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
                enabled = false,
            )
        },
        progressSliderState = progressSliderState,
    )
}


/**
 * 切后台自动暂停
 */
@Composable
private fun AutoPauseEffect(viewModel: EpisodeViewModel) {
    var pausedVideo by rememberSaveable { mutableStateOf(true) } // live after configuration change
    val isPreviewing by rememberUpdatedState(LocalIsPreviewing.current)

    val autoPauseTasker = rememberUiMonoTasker()
    OnLifecycleEvent {
        if (isPreviewing) return@OnLifecycleEvent
        if (it == Lifecycle.State.InActive || it == Lifecycle.State.Destroyed) {
            if (viewModel.playerState.state.value.isPlaying) {
                pausedVideo = true
                autoPauseTasker.launch {
                    // #160, 切换全屏时视频会暂停半秒
                    // > 这其实是之前写切后台自动暂停导致的，检测了 lifecycle 事件，切全屏和切后台是一样的事件。延迟一下就可以了
                    viewModel.playerState.pause() // 正在播放时, 切到后台自动暂停
                }
            } else {
                // 如果不是正在播放, 则不操作暂停, 当下次切回前台时, 也不要恢复播放
                pausedVideo = false
            }
        } else if (it == Lifecycle.State.Active && pausedVideo) {
            autoPauseTasker.launch {
                viewModel.playerState.resume() // 切回前台自动恢复, 当且仅当之前是自动暂停的
            }
            pausedVideo = false
        } else {
            pausedVideo = false
        }
    }
}

@Preview
@Composable
private fun PreviewEpisodePageDesktop() {
    ProvideCompositionLocalsForPreview {
        val context = LocalContext.current
        val vm = rememberViewModel<EpisodeViewModel> {
            EpisodeViewModel(
                initialSubjectId = 0,
                initialEpisodeId = 0,
                initialIsFullscreen = false,
                context,
            )
        }
        EpisodeScene(vm)
    }
}
