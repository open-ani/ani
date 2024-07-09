package me.him188.ani.app.ui.subject.episode.details

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.him188.ani.app.platform.currentPlatform
import me.him188.ani.app.platform.isDesktop
import me.him188.ani.app.tools.rememberBackgroundMonoTasker
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.theme.slightlyWeaken
import me.him188.ani.app.ui.subject.episode.EpisodeCollectionActionButton
import me.him188.ani.app.ui.subject.episode.EpisodePresentation
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel
import me.him188.ani.app.ui.subject.episode.SubjectPresentation
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.FileSize.Companion.Unspecified
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.SubtitleLanguage
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

private val PAGE_HORIZONTAL_PADDING = 16.dp


/**
 * 番剧详情内容,
 */
@Composable
fun EpisodeDetails(
    viewModel: EpisodeViewModel,
    snackbar: SnackbarHostState,
    modifier: Modifier = Modifier,
    actionRow: @Composable () -> Unit = {
        EpisodeActionRow(
            viewModel,
            snackbar = snackbar,
        )
    },
) {
    Column(modifier) {
        // 标题
        Surface(Modifier.fillMaxWidth()) {
            EpisodeTitle(
                viewModel.subjectPresentation,
                viewModel.episodePresentation,
                Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING, vertical = 16.dp),
            ) {
                val tasker = viewModel.rememberBackgroundMonoTasker()
                EpisodeCollectionActionButton(
                    viewModel.episodePresentation.collectionType,
                    onClick = { target ->
                        tasker.launch { viewModel.setEpisodeCollectionType(target) }
                    },
                    enabled = !tasker.isRunning,
                )
            }
        }

        HorizontalDivider(Modifier.fillMaxWidth())

        Column(Modifier.padding(vertical = 16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            val data by viewModel.playerState.videoData.collectAsStateWithLifecycle(null)
            NowPlayingLabel(
                viewModel.mediaSelectorPresentation.selected,
                data?.filename,
                Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING).fillMaxWidth(),
            )

            Row(Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING)) {
                actionRow()
            }

            if (viewModel.mediaSelectorVisible) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = currentPlatform.isDesktop())
                val uiScope = rememberCoroutineScope()
                ModalBottomSheet(
                    onDismissRequest = { viewModel.mediaSelectorVisible = false },
                    sheetState = sheetState,
                ) {
                    EpisodePlayMediaSelector(
                        mediaSelector = viewModel.mediaSelectorPresentation,
                        sourceResults = viewModel.mediaSourceResultsPresentation,
                        onDismissRequest = {
                            uiScope.launch {
                                sheetState.hide()
                                viewModel.mediaSelectorVisible = false
                            }
                        },
                        onSelected = {
                            if (viewModel.videoScaffoldConfig.hideSelectorOnSelect) {
                                uiScope.launch {
                                    sheetState.hide()
                                    viewModel.mediaSelectorVisible = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxHeight(), // 防止添加筛选后数量变少导致 bottom sheet 高度变化
                    )
                }
            }
        }
    }
}

@Stable
private fun Media.render(): String {
    val properties = this.properties
    return listOfNotNull(
        properties.resolution,
        properties.subtitleLanguageIds.joinToString("/") { renderSubtitleLanguage(it) }
            .takeIf { it.isNotBlank() },
        properties.size.takeIf { it != 0.bytes && it != Unspecified },
        properties.alliance,
    ).joinToString(" · ")
}

fun renderSubtitleLanguage(id: String): String {
    return when (id) {
        SubtitleLanguage.ChineseCantonese.id -> "粤语"
        SubtitleLanguage.ChineseSimplified.id -> "简中"
        SubtitleLanguage.ChineseTraditional.id -> "繁中"
        SubtitleLanguage.Japanese.id -> "日语"
        SubtitleLanguage.English.id -> "英语"
        else -> id
    }
}

fun renderResolution(id: String): String {
    return Resolution.tryParse(id)?.displayName ?: id
}

/**
 * 显示正在播放的那行字
 */
@Composable
private fun NowPlayingLabel(
    isPlaying: Media?,
    filename: String?,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        ProvideTextStyle(MaterialTheme.typography.labelMedium) {
            if (isPlaying != null) {
                Column {
                    Row {
                        Text(
                            "正在播放: ",
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            remember(isPlaying) { isPlaying.render() },
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }


                    if (filename != null) {
                        SelectionContainer {
                            Text(
                                filename,
                                Modifier.padding(top = 8.dp),
                                color = LocalContentColor.current.slightlyWeaken(),
                            )
                        }
                    }
                }
            } else {
                Text("请选择数据源")
            }
        }
    }
}

/**
 * 剧集标题, 序号
 *
 * @param collectionButton 收藏状态按钮. [EpisodeCollectionActionButton]
 */
@Composable
fun EpisodeTitle(
    subjectPresentation: SubjectPresentation,
    episodePresentation: EpisodePresentation,
    modifier: Modifier = Modifier,
    collectionButton: @Composable () -> Unit,
) {
    Row(modifier) {
        Column(Modifier.weight(1f)) {
            Row(Modifier.placeholder(subjectPresentation.isPlaceholder)) {
                Text(
                    subjectPresentation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                val ep = episodePresentation
                val shape = RoundedCornerShape(8.dp)
                Box(
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = shape)
                        .placeholder(ep.isPlaceholder)
                        .clip(shape)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                ) {
                    Text(
                        ep.ep,
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalContentColor.current.slightlyWeaken(),
                        softWrap = false, maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    ep.title,
                    Modifier.padding(start = 8.dp).placeholder(ep.isPlaceholder),
                    style = MaterialTheme.typography.titleSmall,
                    softWrap = false, maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Box(Modifier.requiredWidth(IntrinsicSize.Max).align(Alignment.CenterVertically)) {
            collectionButton()
        }
    }
}
