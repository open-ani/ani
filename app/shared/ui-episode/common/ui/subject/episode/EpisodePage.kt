package me.him188.ani.app.ui.subject.episode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.platform.Context
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.setRequestFullScreen
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.LocalSnackbar
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.launchInMain
import me.him188.ani.app.ui.theme.slightlyWeaken
import me.him188.ani.danmaku.ui.rememberDanmakuHostState
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.navigation.BackHandler
import org.openapitools.client.models.EpisodeCollectionType

private val PAGE_HORIZONTAL_PADDING = 16.dp

/**
 * 番剧详情 (播放) 页面
 */
@Composable
fun EpisodePage(
    viewModel: EpisodeViewModel,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp)
    ) {
        EpisodePageContent(
            viewModel,
            onClickGoBack = goBack,
            modifier
        )
    }
}

@Composable
fun EpisodePageContent(
    viewModel: EpisodeViewModel,
    onClickGoBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isFullscreen by viewModel.isFullscreen.collectAsState()

    // 处理当用户点击返回键时, 如果是全屏, 则退出全屏
    val goBack = remember(onClickGoBack) {
        {
            viewModel.playerController.pause()
            if (isFullscreen) {
                context.setRequestFullScreen(false)
                viewModel.setFullscreen(false)
            } else {
                onClickGoBack()
            }
        }
    }

    BackHandler {
        if (isFullscreen) {
            goBack()
        } else {
            onClickGoBack()
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
                selected, videoReady, viewModel.playerController,
                danmakuHostState = rememberDanmakuHostState(viewModel.danmakuFlow),
                onClickGoBack = goBack,
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

            EpisodeActionButtons(viewModel, modifier, context)
        }
    }
}

/**
 * 一行功能按钮.
 *
 * 选择播放源, 复制磁力, 下载, 原始页面.
 */
@Composable
private fun EpisodeActionButtons(
    viewModel: EpisodeViewModel,
    modifier: Modifier,
    context: Context
) {
    Row(
        Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
    ) {
        // 选择播放源
        PlaySourceSelectionAction(viewModel)

        val clipboard by rememberUpdatedState(LocalClipboardManager.current)
        val snackbar by rememberUpdatedState(LocalSnackbar.current)
        ActionButton(
            onClick = {
                viewModel.launchInMain {
                    copyDownloadLink(clipboard, snackbar)
                }
            },
            icon = { Icon(Icons.Default.ContentCopy, null) },
            text = { Text("复制磁力") },
            modifier,
        )

        ActionButton(
            onClick = {
                viewModel.launchInMain {
                    browseDownload(context, snackbar)
                }
            },
            icon = { Icon(Icons.Default.Download, null) },
            text = { Text("下载") },
            modifier,
        )

        ActionButton(
            onClick = {
                viewModel.launchInMain {
                    browsePlaySource(context, snackbar)
                }
            },
            icon = { Icon(Icons.Default.ArrowOutward, null) },
            text = { Text("原始页面") },
            modifier,
        )
    }

    val showPlaySourceSheet by viewModel.isShowPlaySourceSheet.collectAsStateWithLifecycle()
    if (showPlaySourceSheet) {
        PlaySourceSheet(viewModel)
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
 * 选择播放源
 */
@Composable
private fun PlaySourceSelectionAction(
    viewModel: EpisodeViewModel,
    modifier: Modifier = Modifier,
) {
    val isPlaySourcesLoading by viewModel.isPlaySourcesLoading.collectAsStateWithLifecycle()

    ActionButton(
        onClick = {
            viewModel.setShowPlaySourceSheet(true)
        },
        icon = { Icon(Icons.Default.DisplaySettings, null) },
        text = { Text("数据源") },
        modifier,
        isPlaySourcesLoading
    )

//        Box(
//            Modifier.fillMaxWidth().height(80.dp).padding(top = PAGE_HORIZONTAL_PADDING),
//            contentAlignment = Alignment.Center
//        ) {
//            if (!isPlaySourcesLoading && playSources.isNullOrEmpty()) {
//                Text("未找到播放源", style = MaterialTheme.typography.bodyMedium)
//            } else {
//                LazyRow(
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    modifier = Modifier.matchParentSize()
//                ) {
//                    item { }
//                    items(playSources.orEmpty(), { it.id }) { playSource ->
//                        EpisodePlaySourceItem(
//                            playSource,
//                            onClick = { viewModel.setCurrentPlaySource(playSource) },
//                            Modifier.widthIn(min = 60.dp, max = 160.dp)
//                        )
//                    }
//                    item("loading") {
//                        if (isPlaySourcesLoading) {
//                            Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
//                                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
//                            }
//                        }
//                    }
//                    item { }
//                }
//            }
//        }
}

@Composable
private fun PlaySourceSheet(
    viewModel: EpisodeViewModel
) {
    ModalBottomSheet(
        onDismissRequest = { viewModel.setShowPlaySourceSheet(false) },
        Modifier
    ) {
        val playSourceSelector = viewModel.playSourceSelector
        val resolutions by playSourceSelector.resolutions.collectAsStateWithLifecycle()
        val subtitleLanguages by playSourceSelector.subtitleLanguages.collectAsStateWithLifecycle()
        val alliances by playSourceSelector.candidates.collectAsStateWithLifecycle(null)
        val preferredResolution by playSourceSelector.preferredResolution.collectAsStateWithLifecycle()
        val preferredLanguage by playSourceSelector.preferredSubtitleLanguage.collectAsStateWithLifecycle()
        val preferredAlliance by playSourceSelector.finalSelectedAllianceMangled.collectAsStateWithLifecycle(null)

        Column(
            Modifier
                .navigationBarsPadding()
                .padding(vertical = 12.dp, horizontal = PAGE_HORIZONTAL_PADDING)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlaySourceFilterRow(
                resolutions,
                label = { Text("清晰度", overflow = TextOverflow.Visible) },
                key = { it.id },
                eachItem = { item ->
                    InputChip(
                        item == preferredResolution,
                        onClick = { playSourceSelector.setPreferredResolution(item) },
                        label = { Text(remember(item) { item.toString() }) }
                    )
                },
                Modifier.height(32.dp)
            )

            PlaySourceFilterRow(
                subtitleLanguages,
                label = { Text("字幕语言", overflow = TextOverflow.Visible) },
                key = { it },
                eachItem = { item ->
                    InputChip(
                        item == preferredLanguage,
                        onClick = { playSourceSelector.setPreferredSubtitleLanguage(item) },
                        label = { Text(item) }
                    )
                },
                Modifier.height(32.dp)
            )

            PlaySourceFilterFlowRow(
                alliances.orEmpty(),
                label = { Text("字幕组", overflow = TextOverflow.Visible) },
                eachItem = { item ->
                    InputChip(
                        item.allianceMangled == preferredAlliance,
                        onClick = { viewModel.launchInBackground { playSourceSelector.setPreferredCandidate(item) } },
                        label = { Text(item.allianceMangled) },
                        Modifier.height(32.dp)
                    )
                },
            )

            TextButton(
                { viewModel.setShowPlaySourceSheet(false) },
                Modifier.align(Alignment.End).padding(horizontal = 8.dp)
            ) {
                Text("完成")
            }
        }

        Spacer(Modifier.navigationBarsPadding())
    }
}

/**
 * 数据源; 下载; 分享
 */
@Composable
private fun ActionButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = onClick
            )
            .size(64.dp)
            .padding(all = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        icon()
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                text()
            }

            AnimatedVisibility(isLoading) {
                Box(Modifier.padding(start = 8.dp).height(12.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(12.dp), strokeWidth = 2.dp)
                }
            }
        }
    }
}

private val PLAY_SOURCE_LABEL_WIDTH = 68.dp // 正好放得下四个字

@Composable
private fun <T> PlaySourceFilterFlowRow(
    items: List<T>,
    label: @Composable () -> Unit,
    eachItem: @Composable (item: T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.Top) {
        ProvideTextStyle(MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)) {
            Box(Modifier.padding(top = 4.dp).width(PLAY_SOURCE_LABEL_WIDTH)) {
                label()
            }
        }

        Box(
            Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (item in items) {
                    eachItem(item)
                }
            }
        }
    }
}

@Composable
private fun <T> PlaySourceFilterRow(
    items: List<T>,
    label: @Composable () -> Unit,
    key: (item: T) -> Any,
    eachItem: @Composable (item: T) -> Unit,
    modifier: Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        ProvideTextStyle(MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)) {
            Box(Modifier.width(PLAY_SOURCE_LABEL_WIDTH)) {
                label()
            }
        }

        Box(
            Modifier.padding(start = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { }
                items(items, key) { item ->
                    eachItem(item)
                }
                item { }
                item { }
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

@Composable
internal expect fun PreviewEpisodePage()