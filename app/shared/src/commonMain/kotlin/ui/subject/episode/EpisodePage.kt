package me.him188.ani.app.ui.subject.episode

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.him188.ani.app.ui.foundation.AniTopAppBar
import me.him188.ani.app.ui.theme.aniDarkColorTheme
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
                    goBack, Modifier.statusBarsPadding()
                        .fillMaxWidth()
                        .alpha(topAppBarAlpha) // alpha 为 0 时也可以点击, 减少返回失败的概率
                        .background(
                            // 渐变, 靠近视频的区域透明
                            brush = Brush.verticalGradient(
                                0f to darkBackground.copy(alpha = 0.02f),
                                0.612f to darkBackground.copy(alpha = 0.01f),
                                1.00f to Color.Transparent,
                            )
                        )
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
    Column(modifier) {
        // 视频
        Box(Modifier.fillMaxWidth().background(Color.Black).statusBarsPadding()) {
            EpisodeVideo(onClickVideo)
        }

        // 标题
        Surface(Modifier.fillMaxWidth()) {
            EpisodeTitle(viewModel, Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING, vertical = 16.dp))
        }

        Divider(Modifier.fillMaxWidth())

        // 选择播放源

        Surface(Modifier.fillMaxWidth().weight(1f)) {
            Column {
                EpisodePlaySource(viewModel, Modifier.padding(vertical = PAGE_HORIZONTAL_PADDING))
            }
        }
    }
}

@Composable
fun EpisodePlaySource(viewModel: EpisodeViewModel, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        val isPlaySourcesLoading by viewModel.isPlaySourcesLoading.collectAsState()
        val playSourceSelector = viewModel.playSourceSelector

        val resolutions by playSourceSelector.resolutions.collectAsState()
        val subtitleLanguages by playSourceSelector.subtitleLanguages.collectAsState()
        val alliances by playSourceSelector.availableAlliances.collectAsState(null)
        val preferredResolution by playSourceSelector.preferredResolution.collectAsState()
        val preferredLanguage by playSourceSelector.preferredSubtitleLanguage.collectAsState()
        val preferredAlliance by playSourceSelector.preferredAlliance.collectAsState()

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "数据源",
                Modifier.padding(start = PAGE_HORIZONTAL_PADDING),
                style = MaterialTheme.typography.titleLarge,
            )
            if (isPlaySourcesLoading) {
                Box(Modifier.padding(start = 12.dp).height(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            }
        }

        PlaySourceFilterRow(
            resolutions,
            label = { Text("清晰度") },
            key = { it.id },
            eachItem = { item ->
                InputChip(
                    item == preferredResolution,
                    onClick = { playSourceSelector.setPreferredResolution(item) },
                    label = { Text(remember(item) { item.toString() }) }
                )
            },
            Modifier.padding(start = PAGE_HORIZONTAL_PADDING).padding(top = 12.dp).height(32.dp)
        )

        PlaySourceFilterRow(
            subtitleLanguages,
            label = { Text("字幕语言") },
            key = { it },
            eachItem = { item ->
                InputChip(
                    item == preferredLanguage,
                    onClick = { playSourceSelector.setPreferredSubtitleLanguage(item) },
                    label = { Text(item) }
                )
            },
            Modifier.padding(start = PAGE_HORIZONTAL_PADDING).padding(top = 12.dp).height(32.dp)
        )

        PlaySourceFilterRow(
            alliances.orEmpty(),
            label = { Text("字幕组") },
            key = { it.id },
            eachItem = { item ->
                InputChip(
                    item == preferredAlliance,
                    onClick = { playSourceSelector.setPreferredAlliance(item) },
                    label = { Text(item.displayName) }
                )
            },
            Modifier.padding(start = PAGE_HORIZONTAL_PADDING).padding(top = 12.dp).height(32.dp)
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
        ProvideTextStyle(MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)) {
            Box(Modifier.width(70.dp)) {
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
        Row {
            val subjectTitle by viewModel.subjectTitle.collectAsState()
            Text(subjectTitle ?: "", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            val episodeTitle by viewModel.episodeTitle.collectAsState()
            val episodeEp by viewModel.episodeEp.collectAsState()
            val shape = RoundedCornerShape(8.dp)
            Box(
                Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = shape)
                    .clip(shape)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    episodeEp ?: "",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                episodeTitle ?: "",
                Modifier.padding(start = 8.dp),
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