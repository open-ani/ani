package me.him188.ani.app.ui.subject.episode

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.setRequestFullScreen
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.effects.OnLifecycleEvent
import me.him188.ani.app.ui.foundation.effects.ScreenOnEffect
import me.him188.ani.app.ui.foundation.launchInMain
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.subject.episode.details.EpisodeDetails
import me.him188.ani.app.ui.subject.episode.details.EpisodePlayerTitle
import me.him188.ani.danmaku.ui.DanmakuConfig
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.navigation.BackHandler


private val LocalSnackbar = compositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

/**
 * 番剧详情 (播放) 页面
 */
@Composable
fun EpisodePage(
    viewModel: EpisodeViewModel,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding())
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) {
        CompositionLocalProvider(LocalSnackbar provides snackbarHostState) {
            EpisodePageContent(
                viewModel,
                modifier
            )
        }
    }
}

@Composable
fun EpisodePageContent(
    viewModel: EpisodeViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isFullscreen by viewModel.isFullscreen.collectAsState()

    // 处理当用户点击返回键时, 如果是全屏, 则退出全屏
    val navigator = LocalNavigator.current
    BackHandler {
        viewModel.playerController.pause()
        navigator.navigator.goBack()
    }

    BackHandler(enabled = isFullscreen) {
        context.setRequestFullScreen(false)
        viewModel.setFullscreen(false)
    }

    ScreenOnEffect()

    // 切后台自动暂停
    var pausedVideo by rememberSaveable { mutableStateOf(true) } // live after configuration change
    val isPreviewing by rememberUpdatedState(LocalIsPreviewing.current)
    OnLifecycleEvent {
        if (isPreviewing) return@OnLifecycleEvent
        if (it == Lifecycle.State.InActive || it == Lifecycle.State.Destroyed) {
            if (viewModel.playerController.state.value.isPlaying) {
                pausedVideo = true
                viewModel.playerController.pause() // 正在播放时, 切到后台自动暂停
            } else {
                // 如果不是正在播放, 则不操作暂停, 当下次切回前台时, 也不要恢复播放
                pausedVideo = false
            }
        } else if (it == Lifecycle.State.Active && pausedVideo) {
            viewModel.launchInMain {
                viewModel.playerController.resume() // 切回前台自动恢复, 当且仅当之前是自动暂停的
            }
            pausedVideo = false
        } else {
            pausedVideo = false
        }
    }


    Column(modifier.then(if (isFullscreen) Modifier.fillMaxSize() else Modifier.navigationBarsPadding())) {
        // 视频
        val videoReady by viewModel.isVideoReady.collectAsStateWithLifecycle(false)
        val selected by viewModel.playSourceSelected.collectAsStateWithLifecycle(false)
        val danmakuConfig = viewModel.danmakuConfig.collectAsStateWithLifecycle(DanmakuConfig.Default).value
        Box(
            Modifier.fillMaxWidth().background(Color.Black)
                .then(if (isFullscreen) Modifier.fillMaxSize() else Modifier.statusBarsPadding())
        ) {
            EpisodeVideo(
                selected, videoReady,
                title = {
                    val epTitle by viewModel.episodeTitle.collectAsStateWithLifecycle("")
                    val subjectTitle by viewModel.subjectTitle.collectAsStateWithLifecycle("")
                    val ep by viewModel.episodeEp.collectAsStateWithLifecycle(null)
                    EpisodePlayerTitle(ep, epTitle, subjectTitle)
                },
                viewModel.playerController,
                danmakuConfig = danmakuConfig,
                danmakuHostState = remember(viewModel) { viewModel.danmakuHostState },
                onClickFullScreen = {
                    if (isFullscreen) {
                        context.setRequestFullScreen(false)
                        viewModel.setFullscreen(false)
                    } else {
                        viewModel.setFullscreen(true)
                        context.setRequestFullScreen(true)
                    }
                },
                danmakuEnabled = viewModel.danmakuEnabled.collectAsStateWithLifecycle(false).value,
                setDanmakuEnabled = { viewModel.setDanmakuEnabled(it) },
                isFullscreen = isFullscreen,
            )
        }

        if (isFullscreen) {
            return@Column
        }

//        video?.let { vid ->
//            Row(Modifier.border(1.dp, MaterialTheme.colorScheme.onBackground).padding(all = 16.dp)) {
//                val arr by vid.torrentSource!!.pieces.collectAsStateWithLifecycle(mutableListOf())
//                var refresh by remember { mutableIntStateOf(0) }
//                LaunchedEffect(true) {
//                    while (isActive) {
//                        delay(3.seconds)
//                        refresh++
//                    }
//                }
//                key(refresh) {
//                    (arr.asSequence().take(30) + arr.takeLast(30)).forEach {
//                        Box(
//                            Modifier.weight(1f)
//                                .background(
//                                    color = when (it) {
//                                        PieceState.NOT_AVAILABLE -> Color.Magenta
//                                        PieceState.READY -> Color.Blue
//                                        PieceState.FAILED -> Color.Red
//                                        PieceState.DOWNLOADING -> Color.Yellow
//                                        PieceState.FINISHED -> Color.Green
//                                        else -> Color.Red
//                                    }
//                                )
//                        ) {
//                            Spacer(Modifier.height(16.dp))
//                        }
//                    }
//                }
//            }
//        }


        val pagerState = rememberPagerState(initialPage = 0) { 2 }
        val scope = rememberCoroutineScope()

        Column(Modifier.fillMaxSize()) {
//            TabRow(
//                selectedTabIndex = pagerState.currentPage,
//                indicator = @Composable { tabPositions ->
//                    TabRowDefaults.PrimaryIndicator(
//                        Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
//                    )
//                },
//            ) {
//                Tab(
//                    selected = pagerState.currentPage == 0,
//                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
//                    text = { Text("详情") },
//                )
//                Tab(
//                    selected = pagerState.currentPage == 1,
//                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
//                    text = { Text("评论") },
//                )
//            }
//
//            val episodeId by viewModel.episodeId.collectAsStateWithLifecycle()
//            val commentViewModel = remember(episodeId) {
//                CommentViewModel(episodeId)
//            }

            HorizontalPager(state = pagerState, Modifier.fillMaxSize()) { index ->

                when (index) {
                    0 -> EpisodeDetails(viewModel, LocalSnackbar.current, Modifier.fillMaxSize())
//                    1 -> {
//                        CommentColumn(commentViewModel, Modifier.fillMaxSize())
//                    }
                }
            }
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
        EpisodePage(vm)
    }
}

@Composable
internal expect fun PreviewEpisodePage()