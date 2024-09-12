package me.him188.ani.app.ui.subject.episode.details.components

import androidx.compose.foundation.basicMarquee
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
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.danmaku.api.DanmakuMatchInfo
import me.him188.ani.danmaku.api.DanmakuMatchMethod

@Composable
fun DanmakuMatchInfoGrid(
    matchInfos: List<DanmakuMatchInfo>,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 16.dp,
    cardColors: CardColors = CardDefaults.cardColors(),
) {
    Column(modifier) {
        ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
            // infinite measurement
//            LazyVerticalGrid(
//                GridCells.Adaptive(minSize = 120.dp),
//                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
//                verticalArrangement = Arrangement.spacedBy(itemSpacing),
//            ) {
//                items(matchInfos) { info ->
//                    DanmakuMatchInfoView(
//                        info, expanded,
//                        Modifier.weight(1f), // 填满宽度并且让两个卡片有相同高度
//                    )
//                }
//            }
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                verticalArrangement = Arrangement.spacedBy(itemSpacing),
                maxItemsInEachRow = 2,
            ) {
                for (info in matchInfos) {
                    DanmakuMatchInfoView(info, expanded, Modifier.weight(1f), colors = cardColors)
                }
            }
        }
    }
}

@Composable
private fun DanmakuMatchInfoView(
    info: DanmakuMatchInfo,
    showDetails: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    colors: CardColors = CardDefaults.cardColors(),
) {
    Card(modifier, colors = colors) {
        Column(
            Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SelectionContainer {
                Text(
                    info.providerId,
                    Modifier.basicMarquee(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
            }

            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Outlined.Subtitles, "弹幕数量")
                    Text(remember(info.count) { "${info.count}" }, softWrap = false)
                }
            }

            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
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
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (method) {
            is DanmakuMatchMethod.Exact -> {
                ExactMatch()
                if (showDetails) {
                    SelectionContainer {
                        Text(method.subjectTitle, Modifier.basicMarquee(), softWrap = false)
                    }
                    SelectionContainer {
                        Text(method.episodeTitle, Modifier.basicMarquee(), softWrap = false)
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
                        Text("半模糊匹配", softWrap = false)
                    }
                }
                if (showDetails) {
                    SelectionContainer {
                        Text(method.subjectTitle, Modifier.basicMarquee(), softWrap = false)
                    }
                    SelectionContainer {
                        Text(method.episodeTitle, Modifier.basicMarquee(), softWrap = false)
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
                        Text("模糊匹配", softWrap = false)
                    }
                }
                if (showDetails) {
                    SelectionContainer {
                        Text(method.subjectTitle, Modifier.basicMarquee(), softWrap = false)
                    }
                    SelectionContainer {
                        Text(method.episodeTitle, Modifier.basicMarquee(), softWrap = false)
                    }
                }
            }

            is DanmakuMatchMethod.ExactId -> {
                ExactMatch()
                if (showDetails) {
                    SelectionContainer {
                        Text(method.subjectId.toString(), Modifier.basicMarquee(), softWrap = false)
                    }
                    SelectionContainer {
                        Text(method.episodeId.toString(), Modifier.basicMarquee(), softWrap = false)
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
                        Text("无匹配", softWrap = false)
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
