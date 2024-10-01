/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.rss.test

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import me.him188.ani.app.domain.mediasource.MediaListFilter
import me.him188.ani.app.domain.mediasource.MediaListFilters
import me.him188.ani.app.domain.mediasource.rss.RssSearchConfig
import me.him188.ani.app.domain.mediasource.rss.RssSearchQuery
import me.him188.ani.app.domain.mediasource.rss.toFilterContext
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.domain.rss.RssItem
import me.him188.ani.app.domain.rss.guessResourceLocation
import me.him188.ani.app.ui.foundation.OutlinedTag
import me.him188.ani.app.ui.media.MediaDetailsRenderer
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.ResourceLocation
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
    val tags: List<MatchTag>,
) {
    val subtitleLanguageRendered: String = MediaDetailsRenderer.renderSubtitleLanguages(
        parsed.subtitleKind,
        parsed.subtitleLanguages.map { it.displayName },
    )

    companion object {
        fun compute(
            rss: RssItem,
            config: RssSearchConfig,
            query: RssSearchQuery,
        ): RssItemPresentation {
            val parsed = RawTitleParser.getDefault().parse(rss.title)
            val tags = computeTags(rss, parsed, query, config)
            return RssItemPresentation(rss, parsed, tags)
        }

        /**
         * 计算出用于标记该资源与 [RssSearchQuery] 的匹配情况的 tags. 例如标题成功匹配、缺失 EP 等.
         */
        private fun computeTags(
            rss: RssItem,
            title: ParsedTopicTitle,
            query: RssSearchQuery,
            config: RssSearchConfig,
        ): List<MatchTag> = buildMatchTags {
            with(query.toFilterContext()) {
                val candidate = rss.asCandidate(title)

                if (config.filterByEpisodeSort) {
                    val episodeRange = title.episodeRange
                    if (episodeRange == null) {
                        // 期望使用 EP 过滤但是没有 EP 信息, 属于为缺失
                        emit("EP", isMissing = true)
                    } else {
                        emit(
                            episodeRange.toString(),
                            isMatch = MediaListFilters.ContainsAnyEpisodeInfo.applyOn(candidate),
                        )
                    }
                } else {
                    // 不需要用 EP 过滤也展示 EP 信息
                    title.episodeRange?.let {
                        emit(it.toString())
                    }
                }

                if (config.filterBySubjectName) {
                    emit(
                        "标题",
                        isMatch = MediaListFilters.ContainsSubjectName.applyOn(candidate),
                    )
                }
            }

            val resourceLocation = rss.guessResourceLocation()
            when (resourceLocation) {
                is ResourceLocation.HttpStreamingFile -> emit("Streaming")
                is ResourceLocation.HttpTorrentFile -> emit("Torrent")
                is ResourceLocation.LocalFile -> emit("Local")
                is ResourceLocation.MagnetLink -> emit("Magnet")
                is ResourceLocation.WebVideo -> emit("WEB")
                null -> emit("Download", isMissing = true)
            }

            // 以下为普通 tags

            if (title.subtitleLanguages.isEmpty()) {
                emit("Subtitle", isMissing = true)
            } else {
                for (subtitleLanguage in title.subtitleLanguages) {
                    emit(subtitleLanguage.displayName)
                }
            }

            title.resolution?.displayName?.let(::emit)

            title.subtitleKind?.let {
                emit(MediaDetailsRenderer.renderSubtitleKind(it) + "字幕")
            }
        }
    }
}

private fun RssItem.asCandidate(parsed: ParsedTopicTitle): MediaListFilter.Candidate {
    return object : MediaListFilter.Candidate {
        override val originalTitle: String get() = title
        override val episodeRange: EpisodeRange? get() = parsed.episodeRange
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
                        OutlinedMatchTag(tag)
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
