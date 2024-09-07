package me.him188.ani.app.ui.settings.tabs.media.source.rss.test

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.subject.episode.details.renderSubtitleLanguage
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.FileSize

@Composable
fun RssTestPaneDefaults.FinalResultTab(
    state: RssTestPaneState,
    result: RssTestResult.Success,
    onViewDetails: (item: Media) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        GridCells.Adaptive(minSize = 300.dp),
        modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(result.mediaList, key = { it.mediaId }) { item ->
            RssTestResultMediaItem(
                item,
                isSelected = state.viewingItem?.value == item,
                onClick = {
                    onViewDetails(item)
                },
                Modifier.animateItem().fillMaxWidth(),
            )
        }
    }
}

@Composable
fun RssTestResultMediaItem(
    media: Media,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick,
        modifier.width(IntrinsicSize.Min),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
            else CardDefaults.cardColors().containerColor,
        ),
    ) {
        Column(Modifier.padding(all = 16.dp)) {
            ProvideTextStyle(MaterialTheme.typography.titleSmall) {
                Text(media.originalTitle)
            }

            // Labels
            FlowRow(
                Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (media.properties.size != FileSize.Zero && media.properties.size != FileSize.Unspecified) {
                    InputChip(
                        false,
                        onClick = {},
                        label = { Text(media.properties.size.toString()) },
                    )
                }
                InputChip(
                    false,
                    onClick = { },
                    label = { Text(media.properties.resolution) },
                )
                media.properties.subtitleLanguageIds.map {
                    InputChip(
                        false,
                        onClick = { },
                        label = { Text(renderSubtitleLanguage(it)) },
                    )
                }
            }

            // Bottom row: source, alliance, published time
            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                Row(
                    Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Layout note:
                    // On overflow, only the alliance will be ellipsized.

                    Row(
                        Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.weight(1f, fill = false), contentAlignment = Alignment.Center) {
                            Text(
                                media.properties.alliance,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Text(
                        formatDateTime(media.publishedTime, showTime = false),
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}
