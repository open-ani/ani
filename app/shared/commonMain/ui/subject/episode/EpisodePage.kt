package me.him188.ani.app.ui.subject.episode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.AniTopAppBar
import me.him188.ani.app.ui.foundation.LocalSnackbar
import me.him188.ani.app.ui.foundation.TopAppBarGoBackButton
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.theme.aniDarkColorTheme
import me.him188.ani.app.ui.theme.slightlyWeaken
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import kotlin.time.Duration.Companion.seconds

private val PAGE_HORIZONTAL_PADDING = 16.dp

@Composable
fun EpisodePage(
    viewModel: EpisodeViewModel,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var topAppBarVisible by remember { mutableStateOf(false) }
    val topAppBarAlpha by animateFloatAsState(if (topAppBarVisible) 1f else 0f)
    LaunchedEffect(topAppBarVisible) {
        // 2 秒后隐藏 TopAppBar
        if (topAppBarVisible) {
            launch {
                delay(2.seconds)
                topAppBarVisible = false
            }
        }
    }

    Scaffold(
        topBar = {
            val darkBackground = aniDarkColorTheme().onBackground
            CompositionLocalProvider(LocalContentColor provides darkBackground) {
                AniTopAppBar(
                    Modifier.statusBarsPadding()
                        .fillMaxWidth()
                        .alpha(topAppBarAlpha) // alpha 为 0 时也可以点击, 减少返回失败的概率
                        .background(
                            // 渐变, 靠近视频的区域透明
                            brush = Brush.verticalGradient(
                                0f to darkBackground.copy(alpha = 0.02f),
                                0.612f to darkBackground.copy(alpha = 0.01f),
                                1.00f to Color.Transparent,
                            )
                        ),
                    actions = {
                        TopAppBarGoBackButton(goBack)
                    },
                    containerColor = Color.Transparent
                )
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) {
        EpisodePageContent(
            viewModel,
            onClickVideo = {
                topAppBarVisible = !topAppBarVisible
            },
            modifier
        )
    }
}

@Composable
fun EpisodePageContent(
    viewModel: EpisodeViewModel,
    onClickVideo: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier.navigationBarsPadding()) {
        // 视频
        Box(Modifier.fillMaxWidth().background(Color.Black).statusBarsPadding()) {
            EpisodeVideo(onClickVideo)
        }

        // 标题
        Surface(Modifier.fillMaxWidth()) {
            EpisodeTitle(viewModel, Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING, vertical = 16.dp))
        }

        Divider(Modifier.fillMaxWidth())

        Row(
            Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING, vertical = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top,
        ) {
            // 选择播放源
            PlaySourceSelectionAction(viewModel)

            val clipboard by rememberUpdatedState(LocalClipboardManager.current)
            val snackbar by rememberUpdatedState(LocalSnackbar.current)
            ActionButton(
                onClick = { viewModel.launchInBackground { copyDownloadLink(clipboard, snackbar) } },
                icon = { Icon(Icons.Default.ContentCopy, null) },
                text = { Text("复制磁力") },
                modifier,
            )

            val context = LocalContext.current

            ActionButton(
                onClick = { viewModel.launchInBackground { browseDownload(context, snackbar) } },
                icon = { Icon(Icons.Default.Download, null) },
                text = { Text("下载") },
                modifier,
            )

            ActionButton(
                onClick = { viewModel.launchInBackground { browsePlaySource(context, snackbar) } },
                icon = { Icon(Icons.Default.ArrowOutward, null) },
                text = { Text("原始页面") },
                modifier,
            )
        }
    }
}

/**
 * 选择播放源
 */
@Composable
private fun PlaySourceSelectionAction(
    viewModel: EpisodeViewModel,
    modifier: Modifier = Modifier
) {
    val isPlaySourcesLoading by viewModel.isPlaySourcesLoading.collectAsStateWithLifecycle()
    val playSourceSelector = viewModel.playSourceSelector

    val resolutions by playSourceSelector.resolutions.collectAsStateWithLifecycle()
    val subtitleLanguages by playSourceSelector.subtitleLanguages.collectAsStateWithLifecycle()
    val alliances by playSourceSelector.availableAlliances.collectAsStateWithLifecycle(null)
    val preferredResolution by playSourceSelector.preferredResolution.collectAsStateWithLifecycle()
    val preferredLanguage by playSourceSelector.preferredSubtitleLanguage.collectAsStateWithLifecycle()
    val preferredAlliance by playSourceSelector.preferredAlliance.collectAsStateWithLifecycle()


    ActionButton(
        onClick = {
            viewModel.showPlaySourceSheet = true
        },
        icon = { Icon(Icons.Default.DisplaySettings, null) },
        text = { Text("数据源") },
        modifier,
        isPlaySourcesLoading
    )

    if (viewModel.showPlaySourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.showPlaySourceSheet = false },
            Modifier
        ) {
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
                            item == preferredAlliance,
                            onClick = { playSourceSelector.setPreferredAlliance(item) },
                            label = { Text(item.displayName) },
                            Modifier.height(32.dp)
                        )
                    },
                )

                TextButton(
                    { viewModel.showPlaySourceSheet = false },
                    Modifier.align(Alignment.End).padding(horizontal = 8.dp)
                ) {
                    Text("完成")
                }
            }

            Spacer(Modifier.navigationBarsPadding())
        }
    }

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

@Composable
fun EpisodePlaySourceItem(
    playSource: PlaySource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)
    ElevatedCard(
        onClick,
        modifier.clip(shape),
        shape = shape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            Arrangement.spacedBy(8.dp),
        ) {
            Row(
                Modifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(playSource.subtitleLanguage, style = MaterialTheme.typography.bodyMedium)
                Text(
                    playSource.resolution.toString(),
                    Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Row(
                Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(playSource.alliance, style = MaterialTheme.typography.bodySmall)
//                Icon(
//                    Icons.Outlined.ChatBubbleOutline,
//                    null,
//                    Modifier.size(16.dp)
//                )
//                Text(
//                    remember { "${episode.comment}" },
//                    Modifier.offset(y = (-1).dp).padding(start = 4.dp),
//                    style = MaterialTheme.typography.bodySmall
//                )
            }
        }
    }

}

@Composable
fun EpisodeTitle(viewModel: EpisodeViewModel, modifier: Modifier = Modifier) {
    Column(modifier) {
        val subjectTitle by viewModel.subjectTitle.collectAsStateWithLifecycle()
        Row(Modifier.placeholder(subjectTitle == null)) {
            Text(
                subjectTitle ?: "placeholder",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            val episodeTitle by viewModel.episodeTitle.collectAsStateWithLifecycle()
            val episodeEp by viewModel.episodeEp.collectAsStateWithLifecycle()
            val shape = RoundedCornerShape(8.dp)
            Box(
                Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = shape)
                    .placeholder(episodeEp == null)
                    .clip(shape)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    episodeEp ?: "01",
                    style = MaterialTheme.typography.labelLarge,
                    color = LocalContentColor.current.slightlyWeaken(),
                )
            }

            Text(
                episodeTitle ?: "placeholder",
                Modifier.padding(start = 8.dp).placeholder(episodeEp == null),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun EpisodeVideo(
    onClickVideo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        Box(
            Modifier.fillMaxWidth().height(maxWidth * 9 / 16)
                .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onClickVideo)
        ) { // 16:9 box
            // TODO: video 
        }
    }
}

@Composable
internal expect fun PreviewEpisodePage()