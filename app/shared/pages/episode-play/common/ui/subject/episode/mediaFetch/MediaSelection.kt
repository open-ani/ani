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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.media.Media
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.datasources.acgrip.AcgRipDownloadProvider
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.dmhy.DmhyDownloadProvider

@Composable
fun MediaSelector(
    state: MediaSelectorState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    progress: @Composable (RowScope.() -> Unit)? = null,
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MediaFilterRow(
                        state.resolutions,
                        label = { Text("清晰度", overflow = TextOverflow.Visible) },
                        key = { it },
                        eachItem = { item ->
                            FilterChip(
                                item == state.selectedResolution,
                                onClick = { state.preferResolution(item) },
                                label = { Text(remember(item) { item }) },
                                Modifier.height(32.dp)
                            )
                        },
                    )

                    MediaFilterRow(
                        state.subtitleLanguages,
                        label = { Text("字幕语言", overflow = TextOverflow.Visible) },
                        key = { it },
                        eachItem = { item ->
                            FilterChip(
                                item == state.selectedSubtitleLanguage,
                                onClick = { state.preferSubtitleLanguage(item) },
                                label = { Text(item) },
                                Modifier.height(32.dp)
                            )
                        },
                    )

                    MediaFilterFlowRow(
                        state.alliances,
                        label = { Text("字幕组", overflow = TextOverflow.Visible) },
                        eachItem = { item ->
                            FilterChip(
                                item == state.selectedAlliance,
                                onClick = { state.preferAlliance(item) },
                                label = { Text(item) },
                                Modifier.height(32.dp)
                            )
                        },
                    )

                    MediaFilterFlowRow(
                        state.mediaSources,
                        label = { Text("来源", overflow = TextOverflow.Visible) },
                        eachItem = { item ->
                            FilterChip(
                                item == state.selectedMediaSource,
                                onClick = { state.preferMediaSource(item) },
                                label = { Text(item) },
                                Modifier.height(32.dp)
                            )
                        },
                    )

                    Text(
                        remember(state.candidates.size) { "匹配到 ${state.candidates.size} 条资源" },
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            items(state.candidates, key = { it.id }) { item ->
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
            TextButton(
                onDismissRequest,
                Modifier.padding(start = 8.dp)
            ) {
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
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
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
                    InputChip(
                        false,
                        onClick = { state.preferResolution(media.properties.resolution) },
                        label = { Text(media.properties.resolution) },
                        Modifier.height(32.dp),
                        enabled = state.selectedResolution != media.properties.resolution,
                    )
                    media.properties.subtitleLanguages.map {
                        InputChip(
                            false,
                            onClick = { state.preferSubtitleLanguage(it) },
                            label = { Text(it) },
                            Modifier.height(32.dp),
                            enabled = state.selectedSubtitleLanguage != it,
                        )
                    }
                }

                if (media.size != 0.bytes) {
                    Row(
                        Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(media.size.toString())
                    }
                }

                Row(
                    Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                            Text(remember(media.mediaSourceId) { renderMediaSource(media.mediaSourceId) })
                        }

                        Spacer(Modifier.width(16.dp))

                        ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                            Text(media.properties.alliance)
                        }

                        Spacer(Modifier.weight(1f))

                        ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                            Text(formatDateTime(media.publishedTime))
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

private fun renderMediaSource(
    id: String
): String = when (id) {
    DmhyDownloadProvider.ID -> "动漫花园"
    AcgRipDownloadProvider.ID -> "acg.rip"
    else -> id
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
            Box(Modifier.padding(top = 4.dp).widthIn(min = PLAY_SOURCE_LABEL_WIDTH)) {
                label()
            }
        }

        Box(
            Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
