package me.him188.ani.app.ui.settings.tabs.media.source.rss.test

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.tools.rss.RssItem
import me.him188.ani.app.tools.rss.guessResourceLocation
import me.him188.ani.app.ui.cache.details.MediaDetailsRenderer
import me.him188.ani.app.ui.foundation.OutlinedTag
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.contains
import me.him188.ani.datasources.api.topic.titles.ParsedTopicTitle
import me.him188.ani.datasources.api.topic.titles.RawTitleParser
import me.him188.ani.datasources.api.topic.titles.parse

@Composable
@Suppress("UnusedReceiverParameter")
fun RssTestPaneDefaults.RssInfoTab(
    items: List<RssItemPresentation>,
    onViewDetails: (item: RssItemPresentation) -> Unit,
    selectedItemProvider: () -> RssItemPresentation?,
    modifier: Modifier = Modifier,
    lazyStaggeredGridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
) {
    val selectedItem by remember(selectedItemProvider) {
        derivedStateOf(selectedItemProvider)
    }
    LazyVerticalStaggeredGrid(
        StaggeredGridCells.Adaptive(minSize = 300.dp),
        modifier,
        state = lazyStaggeredGridState,
        verticalItemSpacing = 20.dp,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(items) { item ->
            RssTestResultRssItem(
                item,
                isSelected = selectedItem == item,
                onClick = {
                    onViewDetails(item)
                },
                Modifier.animateItem().fillMaxWidth(),
            )
        }
    }
}

@Immutable
class RssItemPresentation(
    val rss: RssItem,
    val parsed: ParsedTopicTitle,
    val tags: List<Tag>,
) {
    class Tag(
        val value: String,
        val isError: Boolean = false,
        val isMatch: Boolean? = null,
    )

    val subtitleLanguageRendered: String = MediaDetailsRenderer.renderSubtitleLanguages(
        parsed.subtitleKind,
        parsed.subtitleLanguages.map { it.displayName },
    )

    companion object {
        fun compute(rss: RssItem, requestedSort: EpisodeSort): RssItemPresentation {
            val parsed = RawTitleParser.getDefault().parse(rss.title)
            val tags: List<Tag> = parsed.run {
                buildList {
                    fun add(
                        value: String,
                        isError: Boolean = false,
                        isMatch: Boolean? = null,
                    ): Boolean = add(Tag(value, isError, isMatch))

                    episodeRange?.let {
                        add(it.toString(), isMatch = requestedSort in it)
                    } ?: kotlin.run {
                        add("EP", isError = true)
                    }

                    val resourceLocation = rss.guessResourceLocation()
                    when (resourceLocation) {
                        is ResourceLocation.HttpStreamingFile -> add("Streaming")
                        is ResourceLocation.HttpTorrentFile -> add("Torrent")
                        is ResourceLocation.LocalFile -> add("Local")
                        is ResourceLocation.MagnetLink -> add("Magnet")
                        is ResourceLocation.WebVideo -> add("WEB")
                        null -> add("Download", isError = true)
                    }

                    if (subtitleLanguages.isEmpty()) {
                        add("Subtitle", isError = true)
                    } else {
                        for (subtitleLanguage in subtitleLanguages) {
                            add(subtitleLanguage.displayName)
                        }
                    }

                    resolution?.displayName?.let(::add)

                    subtitleKind?.let {
                        add(MediaDetailsRenderer.renderSubtitleKind(it) + "字幕")
                    }
                }
            }

            return RssItemPresentation(rss, parsed, tags)
        }
    }
}

@Composable
fun RssTestResultRssItem(
    item: RssItemPresentation,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = CardDefaults.cardColors(
        containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
        else CardDefaults.cardColors().containerColor,
    )
    Card(
        onClick,
        modifier.width(IntrinsicSize.Min),
        colors = color,
    ) {
        ListItem(
            headlineContent = { Text(item.rss.title) },
            supportingContent = {
                FlowRow(
                    Modifier.padding(top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (tag in item.tags) {
                        when {
                            tag.isMatch == true -> {
                                OutlinedTag(
                                    leadingIcon = { Icon(Icons.Rounded.Check, "符合匹配") },
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ) { Text(tag.value) }
                            }

                            tag.isMatch == false -> {
                                OutlinedTag(
                                    leadingIcon = { Icon(Icons.Rounded.Close, "不符合匹配") },
                                    contentColor = MaterialTheme.colorScheme.tertiary,
                                ) { Text(tag.value) }
                            }

                            tag.isError -> {
                                OutlinedTag(
                                    leadingIcon = { Icon(Icons.Rounded.QuestionMark, "缺失") },
                                    contentColor = MaterialTheme.colorScheme.error,
                                ) { Text(tag.value) }
                            }

                            else -> {
                                OutlinedTag { Text(tag.value) }
                            }
                        }
                    }
                    item.rss.pubDate?.let {
                        OutlinedTag { Text(formatDateTime(it)) }
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = color.containerColor),
        )
    }
}
