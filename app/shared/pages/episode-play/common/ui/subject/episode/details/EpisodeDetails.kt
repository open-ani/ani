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
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.subject.episode.EpisodeCollectionActionButton
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel
import me.him188.ani.app.ui.subject.episode.components.EpisodeActionRow
import me.him188.ani.app.ui.subject.episode.render
import me.him188.ani.app.ui.theme.slightlyWeaken
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.openapitools.client.models.EpisodeCollectionType

private val PAGE_HORIZONTAL_PADDING = 16.dp


/**
 * 番剧详情内容,
 */
@Composable
fun EpisodeDetails(
    viewModel: EpisodeViewModel,
    snackbar: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        // 标题
        Surface(Modifier.fillMaxWidth()) {
            EpisodeTitle(viewModel, Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING, vertical = 16.dp))
        }

        HorizontalDivider(Modifier.fillMaxWidth())

        Column(Modifier.padding(vertical = 16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            NowPlayingLabel(viewModel, Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING).fillMaxWidth())

            EpisodeActionRow(
                viewModel,
                snackbar = snackbar,
                Modifier.padding(horizontal = PAGE_HORIZONTAL_PADDING),
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

                    SelectionContainer {
                        Text(
                            remember(playing) { playing?.playSource?.originalTitle ?: "" },
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
                        softWrap = false, maxLines = 1
                    )
                }

                Text(
                    episodeTitle ?: "placeholder",
                    Modifier.padding(start = 8.dp).placeholder(episodeEp == null),
                    style = MaterialTheme.typography.titleSmall,
                    softWrap = false, maxLines = 1,
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

