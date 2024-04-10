package me.him188.ani.app.ui.collection

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.him188.ani.app.data.media.EpisodeCacheStatus
import me.him188.ani.app.ui.theme.stronglyWeaken
import me.him188.ani.app.ui.theme.weaken
import me.him188.ani.datasources.api.topic.UnifiedCollectionType


@Composable
fun EpisodeProgressDialog(
    onDismissRequest: () -> Unit,
    onClickDetails: () -> Unit,
    title: @Composable () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest, properties) {
        Card {
            Column(Modifier.padding(16.dp)) {
                Row {
                    Text("选集播放", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.weight(1f))
                }

                Row(Modifier.padding(top = 8.dp)) {
                    ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
                        title()
                    }
                }

                Row(Modifier.padding(top = 16.dp)) {
                    content()
                }

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lightbulb, null)

                    Text("长按还可以标记为已看", Modifier.padding(start = 4.dp))
                }

                Row(Modifier.padding(top = 16.dp).align(Alignment.End)) {
                    OutlinedButton(onClickDetails) {
                        Text("条目详情")
                    }

                    FilledTonalButton(onDismissRequest, Modifier.padding(start = 8.dp)) {
                        Text("取消")
                    }
                }
            }
        }
    }
}

/**
 * Describes the progress of an episode (of a subject)
 */
@Stable
class EpisodeProgressItem(
    val episodeId: Int,
    val episodeSort: String,
    val watchStatus: UnifiedCollectionType,
    val isOnAir: Boolean?,
    val cacheStatus: EpisodeCacheStatus?
)


@Composable
fun EpisodeProgressRow(
    episodes: List<EpisodeProgressItem>,
    onClickEpisodeState: (episode: EpisodeProgressItem) -> Unit,
    onLongClickEpisode: (episode: EpisodeProgressItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (it in episodes) {
            SmallEpisodeButton(
                episodeSort = it.episodeSort,
                watchStatus = it.watchStatus,
                isOnAir = it.isOnAir,
                onClick = { onClickEpisodeState(it) },
                onLongClick = { onLongClickEpisode(it) },
                cacheStatus = it.cacheStatus,
            )
        }
    }
//
//    // 初始化时滚动到上次观看的那一集
//    val density by rememberUpdatedState(LocalDensity.current)
//    LaunchedEffect(true) {
//        if (item.episodes.isNotEmpty()) {
//            item.lastWatchedEpIndex?.let {
//                val index = it.minus(1).coerceAtLeast(0)
//                if (index != 0) {
//                    // 左边留出来半个按钮的宽度, 让用户知道还有前面的
//                    state.scrollToItem(
//                        index,
//                        density.run { 16.dp.toPx().toInt() }
//                    )
//                }
//            }
//        }
//    }
}

@Composable
private fun SmallEpisodeButton(
    episodeSort: String,
    watchStatus: UnifiedCollectionType,
    isOnAir: Boolean?, // null means unknown
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    cacheStatus: EpisodeCacheStatus? = null,
) {
    val hasBadge = cacheStatus == EpisodeCacheStatus.CACHED || cacheStatus == EpisodeCacheStatus.CACHING
    Box(
        modifier//.padding(end = if (hasBadge) 12.dp else 0.dp)
    ) {
        me.him188.ani.app.ui.foundation.FilledTonalCombinedClickButton(
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = Modifier.combinedClickable(onLongClick = onLongClick, onClick = onClick)
                .heightIn(min = 48.dp)
                .widthIn(min = 48.dp),
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            colors = ButtonDefaults.elevatedButtonColors(
//            containerColor = MaterialTheme.colorScheme.onSurface.copy(0.12f),
//            contentColor = MaterialTheme.colorScheme.onSurface.copy(0.38f),
                containerColor = when {
                    watchStatus == UnifiedCollectionType.DONE || watchStatus == UnifiedCollectionType.DROPPED ->
                        MaterialTheme.colorScheme.primary.weaken()

                    isOnAir != false ->  // 未开播
                        MaterialTheme.colorScheme.onSurface.stronglyWeaken()

                    // 还没看
                    else -> MaterialTheme.colorScheme.primary
                },
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(episodeSort, style = MaterialTheme.typography.bodyMedium)

            if (hasBadge) {
                Box(Modifier.padding(start = 8.dp)) {
                    when (cacheStatus) {
                        EpisodeCacheStatus.CACHED -> Icon(Icons.Rounded.DownloadDone, null)
                        EpisodeCacheStatus.CACHING -> Icon(Icons.Rounded.Downloading, null)
                        else -> {}
                    }
                }
            }
        }

//        if (hasBadge) {
//            Surface(
//                Modifier.align(Alignment.BottomEnd),
//                color = Color.Transparent,
//                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
//            ) {
//               
//            }
//        }
    }
}
