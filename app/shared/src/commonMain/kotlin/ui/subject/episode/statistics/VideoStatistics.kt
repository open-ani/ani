package me.him188.ani.app.ui.subject.episode.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.UnfoldLess
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.source.media.fetch.MediaFetcher
import me.him188.ani.app.data.source.media.selector.MediaSelector
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.app.ui.subject.episode.details.renderSubtitleLanguage
import me.him188.ani.danmaku.api.DanmakuMatchInfo
import me.him188.ani.danmaku.api.DanmakuMatchMethod
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.FileSize.Companion.Unspecified
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes

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
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
) {
    Row(
        modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = toggleExpanded,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProvideTextStyle(textStyle) {
            when (danmakuLoadingState) {
                is DanmakuLoadingState.Failed -> {
                    Text("加载失败")
                }

                is DanmakuLoadingState.Success -> {
                    Text(
                        remember(danmakuLoadingState) {
                            "${danmakuLoadingState.matchInfos.size} 个弹幕源, 共计 ${danmakuLoadingState.matchInfos.sumOf { it.count }} 条弹幕"
                        },
                        Modifier.weight(1f),
                    )

                    IconButton(toggleExpanded) {
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

}

@Composable
fun DanmakuMatchInfoGrid(
    matchInfos: List<DanmakuMatchInfo>,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 16.dp,
) {
    Column(modifier) {
        ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                verticalArrangement = Arrangement.spacedBy(itemSpacing),
                maxItemsInEachRow = 2,
            ) {
                for (info in matchInfos) {
                    DanmakuMatchInfoView(info, expanded, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun VideoStatistics(
    state: VideoStatistics,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "视频统计",
            style = MaterialTheme.typography.titleMedium,
        )

        ProvideTextStyleContentColor(MaterialTheme.typography.bodyMedium, MaterialTheme.colorScheme.primary) {
            when (state.videoLoadingState) {
                VideoLoadingState.DecodingData -> Text("解码中")
                VideoLoadingState.NoMatchingFile -> Failed { Text("未匹配到文件") }
                VideoLoadingState.ResolutionTimedOut -> Failed { Text("解析超时") }
                VideoLoadingState.UnsupportedMedia -> Failed { Text("不支持的视频类型") }

                VideoLoadingState.Initial -> Text("等待数据源")
                VideoLoadingState.ResolvingSource -> Text("解析中")
                is VideoLoadingState.Succeed -> {}

                is VideoLoadingState.UnknownError -> Failed()
            }
        }
    }

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

@Stable
internal fun Media.renderProperties(): String {
    val properties = this.properties
    return listOfNotNull(
        properties.resolution,
        properties.subtitleLanguageIds.joinToString("/") { renderSubtitleLanguage(it) }
            .takeIf { it.isNotBlank() },
        properties.size.takeIf { it != 0.bytes && it != Unspecified },
        properties.alliance,
    ).joinToString(" · ")
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
private fun DanmakuMatchInfoView(
    info: DanmakuMatchInfo,
    showDetails: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
) {
    Card(modifier) {
        Column(
            Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SelectionContainer {
                Text(info.providerId, style = MaterialTheme.typography.titleMedium)
            }

            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Outlined.Subtitles, "弹幕数量")
                    Text(remember(info.count) { "${info.count}" })
                }
            }

            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DanmakuMatchMethodView(info.method, showDetails)
                }
            }
        }
    }
}

@Composable
private fun DanmakuMatchMethodView(
    method: DanmakuMatchMethod,
    showDetails: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (method) {
            is DanmakuMatchMethod.Exact -> {
                ExactMatch()
                if (showDetails) {
                    SelectionContainer {
                        Text(method.subjectTitle)
                    }
                    SelectionContainer {
                        Text(method.episodeTitle)
                    }
                }
            }

            is DanmakuMatchMethod.ExactSubjectFuzzyEpisode -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.tertiary) {
                        Icon(Icons.Outlined.QuestionMark, null)
                        Text("半模糊匹配")
                    }
                }
                if (showDetails) {
                    SelectionContainer {
                        Text(method.subjectTitle)
                    }
                    SelectionContainer {
                        Text(method.episodeTitle)
                    }
                }
            }

            is DanmakuMatchMethod.Fuzzy -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.tertiary) {
                        Icon(Icons.Outlined.QuestionMark, null)
                        Text("模糊匹配")
                    }
                }
                if (showDetails) {
                    SelectionContainer {
                        Text(method.subjectTitle)
                    }
                    SelectionContainer {
                        Text(method.episodeTitle)
                    }
                }
            }

            is DanmakuMatchMethod.ExactId -> {
                ExactMatch()
                if (showDetails) {
                    SelectionContainer {
                        Text(method.subjectId.toString())
                    }
                    SelectionContainer {
                        Text(method.episodeId.toString())
                    }
                }
            }

            is DanmakuMatchMethod.NoMatch -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.secondary) {
                        Icon(Icons.Outlined.Close, null)
                        Text("无匹配")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExactMatch() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
            Icon(Icons.Outlined.WorkspacePremium, null)
            Text("精确匹配")
        }
    }
}

@Composable
private fun Failed(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.error) {
        Row(
            modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Rounded.ErrorOutline, null)
            content()
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
//        OutlinedCard(
//            modifier = modifier,
//            shape = MaterialTheme.shapes.medium,
//        ) {
//            Row(
//                Modifier.padding(all = 16.dp).heightIn(max = 120.dp).verticalScroll(rememberScrollState()),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                Row(Modifier.weight(1f, fill = false)) {
//                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
//                        text()
//                    }
//                }
//                TextButton(onClick = onCopy) {
//                    Text("复制")
//                }
//            }
//        }
}
