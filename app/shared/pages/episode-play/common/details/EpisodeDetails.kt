package me.him188.ani.app.ui.subject.episode.details

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.tools.rememberBackgroundMonoTasker
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.subject.episode.EpisodeCollectionActionButton
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel
import me.him188.ani.app.ui.subject.episode.mediaSelectorState
import me.him188.ani.app.ui.theme.slightlyWeaken
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.SubtitleLanguage

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
            EpisodeTitle(viewModel, Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING, vertical = 16.dp))
        }

        HorizontalDivider(Modifier.fillMaxWidth())

        Column(Modifier.padding(vertical = 16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            NowPlayingLabel(viewModel, Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING).fillMaxWidth())

            Row(Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING)) {
                actionRow()
            }

            if (viewModel.mediaSelectorVisible) {
                ModalBottomSheet(onDismissRequest = { viewModel.mediaSelectorVisible = false }) {
                    EpisodePlayMediaSelector(
                        viewModel.mediaSelectorState,
                        onDismissRequest = { viewModel.mediaSelectorVisible = false },
                        Modifier,
                        progressProvider = { viewModel.episodeMediaFetchSession.mediaFetcherProgress },
                    )
                }
            }
        }
    }
}

@Stable
private fun Media.render(): String {
    val playing = this
    return listOfNotNull(
        playing.properties.resolution,
        playing.properties.subtitleLanguageIds.joinToString("/") { renderSubtitleLanguage(it) }
            .takeIf { it.isNotBlank() },
        playing.size.takeIf { it != 0.bytes },
        playing.properties.alliance,
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
private fun NowPlayingLabel(viewModel: EpisodeViewModel, modifier: Modifier = Modifier) {
    Row(modifier) {
        ProvideTextStyle(MaterialTheme.typography.labelMedium) {
            val playing = viewModel.mediaSelectorState.selected
            if (playing != null) {
                Column {
                    Row {
                        Text(
                            "正在播放: ",
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            remember(playing) { playing.render() },
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }

                    SelectionContainer {
                        Text(
                            remember(playing) { playing.originalTitle },
                            Modifier.padding(top = 8.dp),
                            color = LocalContentColor.current.slightlyWeaken(),
                        )
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
 */
@Composable
fun EpisodeTitle(
    viewModel: EpisodeViewModel,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        Column {
            Row(Modifier.placeholder(viewModel.subjectPresentation.isPlaceholder)) {
                Text(
                    viewModel.subjectPresentation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                val ep = viewModel.episodePresentation
                val shape = RoundedCornerShape(8.dp)
                Box(
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = shape)
                        .placeholder(ep.isPlaceholder)
                        .clip(shape)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        ep.sort,
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalContentColor.current.slightlyWeaken(),
                        softWrap = false, maxLines = 1
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

        Spacer(Modifier.weight(1f))

        val tasker = viewModel.rememberBackgroundMonoTasker()
        EpisodeCollectionActionButton(
            viewModel.episodePresentation.collectionType,
            onClick = { target ->
                tasker.launch {
                    viewModel.setEpisodeCollectionType(target)
                }
            },
            Modifier.requiredWidth(IntrinsicSize.Max).align(Alignment.CenterVertically),
            enabled = !tasker.isRunning,
        )
    }
}

