package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.Res
import me.him188.ani.app.acg_rip
import me.him188.ani.app.bangumi
import me.him188.ani.app.dmhy
import me.him188.ani.app.mikan
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.datasources.acgrip.AcgRipMediaSource
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.bangumi.BangumiSubjectProvider
import me.him188.ani.datasources.dmhy.DmhyMediaSource
import me.him188.ani.datasources.mikan.MikanMediaSource
import org.jetbrains.compose.resources.painterResource

@Composable
fun MediaSelector(
    state: MediaSelectorState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    progress: @Composable (RowScope.() -> Unit)? = null,
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
//        Text(
//            remember(state.candidates.size) { "选择资源" },
//            style = MaterialTheme.typography.titleMedium,
//        )

        if (progress != null) {
            Row(Modifier.fillMaxWidth()) {
                progress()
            }
        }

        LazyColumn(
            Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Column(
                    Modifier.fillMaxWidth(),
                ) {
                    MediaFilterRow(
                        state.resolutions,
                        label = { Text("清晰度", overflow = TextOverflow.Visible) },
                        key = { it },
                        eachItem = { item ->
                            FilterChip(
                                item == state.selectedResolution,
                                onClick = { state.preferResolution(item, removeOnExist = true) },
                                label = { Text(remember(item) { item }) },
                            )
                        },
                        Modifier.heightIn(min = 32.dp)
                    )

                    MediaFilterRow(
                        state.subtitleLanguages,
                        label = { Text("字幕语言", overflow = TextOverflow.Visible) },
                        key = { it },
                        eachItem = { item ->
                            FilterChip(
                                item == state.selectedSubtitleLanguage,
                                onClick = { state.preferSubtitleLanguage(item, removeOnExist = true) },
                                label = { Text(item) },
                            )
                        },
                        Modifier.heightIn(min = 32.dp)
                    )

                    MediaFilterFlowRow(
                        state.alliances,
                        label = { Text("字幕组", overflow = TextOverflow.Visible) },
                        eachItem = { item ->
                            FilterChip(
                                item == state.selectedAlliance,
                                onClick = { state.preferAlliance(item, removeOnExist = true) },
                                label = { Text(item) },
                            )
                        },
                        Modifier.heightIn(min = 32.dp)
                    )

                    MediaFilterFlowRow(
                        state.mediaSources,
                        label = { Text("来源", overflow = TextOverflow.Visible) },
                        eachItem = { item ->
                            FilterChip(
                                item == state.selectedMediaSource,
                                onClick = { state.preferMediaSource(item, removeOnExist = true) },
                                label = { Text(remember(item) { renderMediaSource(item) }) },
                            )
                        },
                        Modifier.heightIn(min = 32.dp)
                    )

                    Text(
                        remember(state.candidates.size) { "匹配到 ${state.candidates.size} 条资源" },
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            items(state.candidates, key = { it.mediaId }) { item ->
                MediaItem(
                    item,
                    state.selected == item,
                    state,
                    onClick = {
                        state.select(item)
                    },
                    Modifier
                        .animateItemPlacement()
                        .fillMaxWidth(),
                )
            }
            item { } // dummy spacer
        }

        HorizontalDivider()

        Row(Modifier.align(Alignment.End).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onDismissRequest) {
                Text("关闭")
            }
        }

    }
}

@Composable
private fun MediaItem(
    media: Media,
    selected: Boolean,
    state: MediaSelectorState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick,
        modifier.width(IntrinsicSize.Min),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (selected) 0.dp else 1.dp,
        )
    ) {
        Box {
            Column(Modifier.padding(all = 16.dp)) {
                ProvideTextStyle(MaterialTheme.typography.titleSmall) {
                    Text(media.originalTitle)
                }

                FlowRow(
                    Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (media.size != FileSize.Zero) {
                        InputChip(
                            false,
                            onClick = {},
                            label = { Text(media.size.toString()) },
                        )
                    }
                    InputChip(
                        false,
                        onClick = { state.preferResolution(media.properties.resolution) },
                        label = { Text(media.properties.resolution) },
                        enabled = state.selectedResolution != media.properties.resolution,
                    )
                    media.properties.subtitleLanguages.map {
                        InputChip(
                            false,
                            onClick = { state.preferSubtitleLanguage(it) },
                            label = { Text(it) },
                            enabled = state.selectedSubtitleLanguage != it,
                        )
                    }
                }

                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                    Row(
                        Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            FlowRow(Modifier.weight(1f)) {
                                Text(
                                    remember(media.mediaSourceId) { renderMediaSource(media.mediaSourceId) },
                                    maxLines = 1,
                                    softWrap = false
                                )

                                Spacer(Modifier.width(16.dp))

                                Text(media.properties.alliance)
                            }

                            Text(
                                formatDateTime(media.publishedTime),
                                Modifier.padding(start = 16.dp).align(Alignment.Bottom),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

//            if (selected) {
//                ProvideTextStyleContentColor(MaterialTheme.typography.labelLarge) {
//                    Row(
//                        Modifier.align(Alignment.BottomEnd).padding(16.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                    ) {
//                        Icon(Icons.Rounded.Check, null, tint = LocalContentColor.current)
//                        Text("当前选择", Modifier.padding(start = 4.dp))
//                    }
//                }
//            }
        }
    }
}

fun renderMediaSource(
    id: String
): String = when (id) {
    DmhyMediaSource.ID -> "动漫花园"
    AcgRipMediaSource.ID -> "acg.rip"
    MikanMediaSource.ID -> "Mikan"
    BangumiSubjectProvider.ID -> "Bangumi"
    else -> id
}

@Composable
fun getMediaSourceIcon(
    id: String
): Painter? {
    if (LocalIsPreviewing.current) { // compose resources does not support preview
        return null
    }
    return when (id) {
        DmhyMediaSource.ID -> painterResource(Res.drawable.dmhy)
        AcgRipMediaSource.ID -> painterResource(Res.drawable.acg_rip)
        MikanMediaSource.ID -> painterResource(Res.drawable.mikan)
        BangumiSubjectProvider.ID -> painterResource(Res.drawable.bangumi)
        else -> null
    }
}

private val PLAY_SOURCE_LABEL_WIDTH = 62.dp

@Composable
private fun <T> MediaFilterFlowRow(
    items: List<T>,
    label: @Composable () -> Unit,
    eachItem: @Composable (item: T) -> Unit,
    modifier: Modifier = Modifier,
    labelStyle: TextStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
) {
    Row(modifier, verticalAlignment = Alignment.Top) {
        ProvideTextStyle(labelStyle) {
            Box(Modifier.padding(top = 12.dp).widthIn(min = PLAY_SOURCE_LABEL_WIDTH)) {
                label()
            }
        }

        Box(
            Modifier.padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (item in items) {
                    eachItem(item)
                }
            }
        }
    }
}

@Composable
private fun <T> MediaFilterRow(
    items: List<T>,
    label: @Composable () -> Unit,
    key: (item: T) -> Any,
    eachItem: @Composable (item: T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        ProvideTextStyle(MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)) {
            Box(Modifier.widthIn(min = PLAY_SOURCE_LABEL_WIDTH)) {
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
                items(items, key) { item ->
                    eachItem(item)
                }
            }
        }
    }
}
