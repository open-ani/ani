package me.him188.ani.app.ui.subject.episode.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.UnfoldLess
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.source.media.fetch.MediaFetcher
import me.him188.ani.app.data.source.media.selector.MediaSelector
import me.him188.ani.app.ui.foundation.text.ProvideContentColor
import me.him188.ani.app.ui.subject.episode.details.renderProperties
import me.him188.ani.datasources.api.Media

/**
 * 视频统计信息, 用于获取当前播放器正在播放的视频的来源 [Media] 和文件名, 以及弹幕信息.
 */
@Stable
abstract class VideoStatistics {
    /**
     * [MediaFetcher] 的所有数据源是否都已经加载完成.
     */
    abstract val mediaSourceLoading: Boolean

    /**
     * 从 [MediaSelector] 选择后就有
     */
    abstract val playingMedia: Media?

    /**
     * 要播放器获取到视频文件后才有
     */
    abstract val playingFilename: String?
    abstract val videoLoadingState: VideoLoadingState
}

@Stable
class DelegateVideoStatistics(
    playingMedia: State<Media?>,
    playingFilename: State<String?>,
    mediaSourceLoading: State<Boolean>,
    videoLoadingState: State<VideoLoadingState>,
) : VideoStatistics() {
    override val mediaSourceLoading by mediaSourceLoading
    override val playingMedia by playingMedia
    override val playingFilename by playingFilename
    override val videoLoadingState by videoLoadingState
}

@Composable
fun DanmakuMatchInfoSummaryRow(
    danmakuLoadingState: DanmakuLoadingState,
    expanded: Boolean,
    toggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    if (showDialog) {
        val text = remember(danmakuLoadingState) {
            when (danmakuLoadingState) {
                is DanmakuLoadingState.Failed -> danmakuLoadingState.cause.stackTraceToString()
                else -> danmakuLoadingState.toString()
            }
        }
        SimpleErrorDialog(
            { text },
            onDismissRequest = { showDialog = false },
        )
    }
    Row(
        modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                if (danmakuLoadingState is DanmakuLoadingState.Failed) {
                    showDialog = true
                } else {
                    toggleExpanded()
                }
            },
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (danmakuLoadingState) {
            is DanmakuLoadingState.Failed -> {
                ProvideContentColor(MaterialTheme.colorScheme.error) {
                    IconButton({ showDialog = true }) {
                        Icon(Icons.Rounded.ErrorOutline, null)
                    }
                    Text(
                        "弹幕加载失败，点击查看",
                        Modifier.weight(1f),
                    )
                }
            }

            is DanmakuLoadingState.Success -> {
                Text(
                    remember(danmakuLoadingState) {
                        "${danmakuLoadingState.matchInfos.size} 个弹幕源，共计 ${danmakuLoadingState.matchInfos.sumOf { it.count }} 条弹幕"
                    },
                    Modifier.weight(1f),
                )

                IconButton(toggleExpanded, Modifier.padding(start = 16.dp)) {
                    if (expanded) {
                        Icon(Icons.Rounded.UnfoldLess, "展示更少")
                    } else {
                        Icon(Icons.Rounded.UnfoldMore, "展示更多")
                    }
                }
            }

            DanmakuLoadingState.Idle -> {

            }

            DanmakuLoadingState.Loading -> {}
        }
    }

}


@Composable
fun VideoStatistics(
    state: VideoStatistics,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
            when (val loadingState = state.videoLoadingState) {
                is VideoLoadingState.Succeed -> {
                    val mediaPropertiesText by remember {
                        derivedStateOf {
                            state.playingMedia?.renderProperties()
                        }
                    }
                    NowPlayingLabel(mediaPropertiesText, state.playingFilename)
                }

                is VideoLoadingState.UnknownError -> {
                    ErrorTextBox(
                        remember(loadingState) { loadingState.cause.toString() },
                        { clipboard.setText(AnnotatedString(loadingState.cause.stackTraceToString())) },
                        Modifier.padding(top = 8.dp).fillMaxWidth(),
                    )
                }

                else -> {}
            }
        }
    }
}

/**
 * 显示正在播放的那行字
 */
@Composable
private fun NowPlayingLabel(
    playingMedia: String?,
    filename: String?,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        ProvideTextStyle(MaterialTheme.typography.titleMedium) {
            if (playingMedia != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row {
                        Text(
                            "正在播放: ",
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            playingMedia,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }

                    if (filename != null) {
                        SelectionContainer {
                            Text(
                                filename,
                                color = MaterialTheme.colorScheme.secondary,
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

@Composable
private fun ErrorTextBox(
    text: String,
    onCopy: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        text,
        onValueChange = {},
        modifier,
        label = { Text("错误信息") },
        shape = MaterialTheme.shapes.medium,
        readOnly = true,
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = onCopy) {
                Icon(Icons.Rounded.ContentCopy, null)
            }
        },
    )
}
