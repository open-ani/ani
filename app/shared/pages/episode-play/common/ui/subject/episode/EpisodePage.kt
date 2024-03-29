package me.him188.ani.app.ui.subject.episode

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.setRequestFullScreen
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.effects.OnLifecycleEvent
import me.him188.ani.app.ui.foundation.effects.ScreenOnEffect
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.launchInMain
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.subject.episode.components.EpisodeActionRow
import me.him188.ani.app.ui.theme.slightlyWeaken
import me.him188.ani.danmaku.ui.rememberDanmakuHostState
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.navigation.BackHandler
import org.openapitools.client.models.EpisodeCollectionType

private val PAGE_HORIZONTAL_PADDING = 16.dp

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
    OnLifecycleEvent {
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
        Box(
            Modifier.fillMaxWidth().background(Color.Black)
                .then(if (isFullscreen) Modifier.fillMaxSize() else Modifier.statusBarsPadding())
        ) {
            EpisodeVideo(
                selected, videoReady,
                title = {
                    val title by viewModel.episodeTitle.collectAsStateWithLifecycle("")
                    val ep by viewModel.episodeEp.collectAsStateWithLifecycle(null)
                    EpisodePlayerTopBarTitle(ep, title)
                    // 过长时隐藏标题, 保留序号
                },
                viewModel.playerController,
                danmakuHostState = rememberDanmakuHostState(viewModel.danmakuFlow),
                onClickFullScreen = {
                    if (isFullscreen) {
                        context.setRequestFullScreen(false)
                        viewModel.setFullscreen(false)
                    } else {
                        viewModel.setFullscreen(true)
                        context.setRequestFullScreen(true)
                    }
                },
                isFullscreen = isFullscreen
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


        // 标题
        Surface(Modifier.fillMaxWidth()) {
            EpisodeTitle(viewModel, Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING, vertical = 16.dp))
        }

        HorizontalDivider(Modifier.fillMaxWidth())

        Column(Modifier.padding(vertical = 16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            NowPlayingLabel(viewModel, Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING).fillMaxWidth())

            EpisodeActionRow(
                viewModel,
                snackbar = LocalSnackbar.current,
                modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING),
            )
        }
    }
}


/**
 * 显示正在播放的那行字
 */
@Composable
private fun NowPlayingLabel(viewModel: EpisodeViewModel, modifier: Modifier = Modifier) {
    Row(modifier) {
        val playing by viewModel.playSourceSelector.targetPlaySourceCandidate.collectAsStateWithLifecycle()
        ProvideTextStyle(MaterialTheme.typography.labelMedium) {
            if (playing != null) {
                Column {
                    Row {
                        Text(
                            "正在播放: ",
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            playing?.render() ?: "",
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }

                    Text(
                        remember(playing) { playing?.playSource?.originalTitle ?: "" },
                        Modifier.padding(top = 8.dp),
                        color = LocalContentColor.current.slightlyWeaken(),
                    )
                }
            } else {
                Text("请选择数据源")
            }
        }
    }
}

/**
 * 剧集标题, 序号
 */
@Composable
fun EpisodeTitle(
    viewModel: EpisodeViewModel,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        Column {
            val subjectTitle by viewModel.subjectTitle.collectAsStateWithLifecycle(null)
            Row(Modifier.placeholder(subjectTitle == null)) {
                Text(
                    subjectTitle ?: "placeholder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                val episodeTitle by viewModel.episodeTitle.collectAsStateWithLifecycle(null)
                val episodeEp by viewModel.episodeEp.collectAsStateWithLifecycle(null)
                val shape = RoundedCornerShape(8.dp)
                Box(
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = shape)
                        .placeholder(episodeEp == null)
                        .clip(shape)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        episodeEp ?: "01",
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalContentColor.current.slightlyWeaken(),
                    )
                }

                Text(
                    episodeTitle ?: "placeholder",
                    Modifier.padding(start = 8.dp).placeholder(episodeEp == null),
                    style = MaterialTheme.typography.titleSmall,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.weight(1f))

        val collectionType by viewModel.episodeCollectionType.collectAsStateWithLifecycle(EpisodeCollectionType.WATCHLIST)

        EpisodeCollectionActionButton(
            collectionType,
            onClick = { target ->
                viewModel.launchInBackground {
                    setEpisodeCollectionType(target)
                }
            },
            Modifier.requiredWidth(IntrinsicSize.Max).align(Alignment.CenterVertically)
        )
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