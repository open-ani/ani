package me.him188.ani.app.ui.subject.episode.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.UnfoldLess
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.app.ui.subject.episode.VideoLoadingState
import me.him188.ani.danmaku.api.DanmakuMatchInfo
import me.him188.ani.danmaku.api.DanmakuMatchMethod
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Stable
class PlayerStatisticsState {
    val videoLoadingState: MutableStateFlow<VideoLoadingState> = MutableStateFlow(VideoLoadingState.Initial)

    val danmakuLoadingState: MutableStateFlow<DanmakuLoadingState> = MutableStateFlow(DanmakuLoadingState.Idle)

    val isDanmakuLoading = danmakuLoadingState.map { it is DanmakuLoadingState.Loading }
}

@Immutable
sealed class DanmakuLoadingState {
    @Immutable
    data object Idle : DanmakuLoadingState()

    @Immutable
    data object Loading : DanmakuLoadingState()

    @Immutable
    data class Success(
        val matchInfos: List<DanmakuMatchInfo>
    ) : DanmakuLoadingState()

    @Immutable
    data class Failed(
        val cause: Throwable,
    ) : DanmakuLoadingState()
}

@Composable
fun PlayerStatistics(
    state: PlayerStatisticsState,
    modifier: Modifier = Modifier
) {
    Surface {
        Column(
            modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val danmakuLoadingState by state.danmakuLoadingState.collectAsStateWithLifecycle()

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "弹幕统计",
                    style = MaterialTheme.typography.titleLarge,
                )

                ProvideTextStyleContentColor(MaterialTheme.typography.labelLarge, MaterialTheme.colorScheme.primary) {
                    when (danmakuLoadingState) {
                        is DanmakuLoadingState.Failed -> Failed { }
                        DanmakuLoadingState.Idle -> Text("等待视频")
                        DanmakuLoadingState.Loading -> Text("加载中")
                        is DanmakuLoadingState.Success -> {}// Succeed { Text("加载成功") }
                    }
                }
            }
            val clipboard = LocalClipboardManager.current


            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                    when (val loadingState = danmakuLoadingState) {
                        is DanmakuLoadingState.Failed -> {
                            ErrorTextBox(
                                remember(loadingState) {
                                    loadingState.cause.toString()
                                },
                                { clipboard.setText(AnnotatedString(loadingState.cause.stackTraceToString())) },
                                Modifier.padding(top = 8.dp).fillMaxWidth(),
                            )
                        }

                        is DanmakuLoadingState.Success -> {
                            var isShowDetails by remember { mutableStateOf(false) }

                            Row(
                                Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { isShowDetails = !isShowDetails },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    remember(loadingState) {
                                        "${loadingState.matchInfos.size} 个弹幕源, 共计 ${loadingState.matchInfos.sumOf { it.count }} 条弹幕"
                                    },
                                    Modifier.weight(1f),
                                    style = MaterialTheme.typography.labelLarge,
                                )

                                IconButton({ isShowDetails = !isShowDetails }) {
                                    if (isShowDetails) {
                                        Icon(Icons.Rounded.UnfoldLess, "展示更少")
                                    } else {
                                        Icon(Icons.Rounded.UnfoldMore, "展示更多")
                                    }
                                }
                            }

                            LazyVerticalGrid(
                                GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                items(loadingState.matchInfos, { it }) {
                                    DanmakuMatchInfoView(it, { isShowDetails })
                                }
                            }
//                            FlowRow(
//                                horizontalArrangement = Arrangement.spacedBy(16.dp),
//                                verticalArrangement = Arrangement.spacedBy(16.dp)
//                            ) {
//                                for (info in loadingState.matchInfos) {
//                                    DanmakuMatchInfoView(info, { isShowDetails })
//                                }
//                            }
                        }

                        else -> {}
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            val videoLoadingState = state.videoLoadingState.collectAsStateWithLifecycle()
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "视频统计",
                    style = MaterialTheme.typography.titleLarge,
                )

                ProvideTextStyleContentColor(MaterialTheme.typography.bodyMedium, MaterialTheme.colorScheme.primary) {
                    when (videoLoadingState.value) {
                        VideoLoadingState.DecodingData -> Text("解码中")
                        VideoLoadingState.NoMatchingFile -> Failed { Text("未匹配到文件") }
                        VideoLoadingState.ResolutionTimedOut -> Failed { Text("解析超时") }
                        VideoLoadingState.UnsupportedMedia -> Failed { Text("不支持的视频类型") }

                        VideoLoadingState.Initial -> Text("等待数据源")
                        VideoLoadingState.ResolvingSource -> Text("解析中")
                        VideoLoadingState.Succeed -> {}

                        is VideoLoadingState.UnknownError -> Failed()
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                    when (val loadingState = videoLoadingState.value) {
                        is VideoLoadingState.Succeed -> {
                            Text("正常播放", style = MaterialTheme.typography.labelLarge)
                        }

                        is VideoLoadingState.UnknownError -> {
                            ErrorTextBox(
                                remember(loadingState) { loadingState.cause.toString() },
                                { clipboard.setText(AnnotatedString(loadingState.cause.stackTraceToString())) },
                                Modifier.padding(top = 8.dp).fillMaxWidth()
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun DanmakuMatchInfoView(
    info: DanmakuMatchInfo,
    showDetails: () -> Boolean,
    modifier: Modifier = Modifier,
) {
//    var showDialog by remember { mutableStateOf(false) }
//    if (showDialog) {
//        Dialog(onDismissRequest = { showDialog = false }) {
//            RichDialogLayout(
//                title = { Text(info.providerId) },
//                buttons = { TextButton({ showDialog = false }) { Text("关闭") } }
//            ) {
//                DanmakuMatchInfoDetails()
//            }
//        }
//    }
    OutlinedCard(
//        { showDialog = true },
        modifier,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            Modifier.padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(info.providerId, style = MaterialTheme.typography.titleMedium)

            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Subtitles, "弹幕数量")
                    Text(remember(info.count) { "${info.count}" })
                }
            }

            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DanmakuMatchMethodView(info.method, showDetails())
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (method) {
            is DanmakuMatchMethod.Exact -> {
                ExactMatch()
                if (showDetails) {
                    Text(method.subjectTitle)
                    Text(method.episodeTitle)
                }
            }

            is DanmakuMatchMethod.ExactSubjectFuzzyEpisode -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.tertiary) {
                        Icon(Icons.Outlined.QuestionMark, null)
                        Text("半模糊匹配")
                    }
                }
                if (showDetails) {
                    Text(method.subjectTitle)
                    Text(method.episodeTitle)
                }
            }

            is DanmakuMatchMethod.Fuzzy -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.tertiary) {
                        Icon(Icons.Outlined.QuestionMark, null)
                        Text("模糊匹配")
                    }
                }
                if (showDetails) {
                    Text(method.subjectTitle)
                    Text(method.episodeTitle)
                }
            }

            is DanmakuMatchMethod.ExactId -> {
                ExactMatch()
                if (showDetails) {
                    Text(method.subjectId.toString())
                    Text(method.episodeId.toString())
                }
            }

            is DanmakuMatchMethod.NoMatch -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
            Icon(Icons.Outlined.WorkspacePremium, null)
            Text("精确匹配")
        }
    }
}

@Composable
private fun Succeed(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
        Row(
            modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Rounded.Done, null)
            content()
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
        }
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
