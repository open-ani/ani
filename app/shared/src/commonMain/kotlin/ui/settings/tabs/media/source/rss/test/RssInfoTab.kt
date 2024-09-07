package me.him188.ani.app.ui.settings.tabs.media.source.rss.test

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.tools.rss.RssChannel
import me.him188.ani.app.tools.rss.RssItem

@Composable
fun RssTestPaneDefaults.RssInfoTab(
    state: RssTestPaneState,
    channel: RssChannel,
    onViewDetails: (item: RssItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalStaggeredGrid(
        StaggeredGridCells.Adaptive(minSize = 300.dp),
        modifier,
        state = state.rssTabGridState,
//                            verticalArrangement = Arrangement.spacedBy(20.dp),
        verticalItemSpacing = 20.dp,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(channel.items) { item ->
            RssTestResultRssItem(
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
fun RssTestResultRssItem(
    item: RssItem,
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
                Text(item.title)
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
                                item.link,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
//
//                    Text(
//                        formatDateTime(item.pubDate?.toInstant(TimeZone.currentSystemDefault()), showTime = false),
//                        maxLines = 1,
//                        softWrap = false,
//                    )
                }
            }
        }
    }
}
