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
import me.him188.ani.app.data.source.media.source.MediaListFilter
import me.him188.ani.app.data.source.media.source.MediaListFilters
import me.him188.ani.app.data.source.media.source.RssSearchConfig
import me.him188.ani.app.data.source.media.source.RssSearchQuery
import me.him188.ani.app.data.source.media.source.toFilterContext
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.tools.rss.RssItem
import me.him188.ani.app.tools.rss.guessResourceLocation
import me.him188.ani.app.ui.cache.details.MediaDetailsRenderer
import me.him188.ani.app.ui.foundation.OutlinedTag
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
    val tags: List<Tag>,
) {
    class Tag(
        val value: String,
        /**
         * 该标签表示一个缺失的项目. 例如缺失 EP.
         */
        val isMissing: Boolean = false,
        /**
         * 该标签是否匹配了用户的搜索条件.
         * - `true`: 满足了一个条件. UI 显示为紫色的 check
         * - `false`: 不满足条件. UI 显示为红色的 close
         * - `null`: 这不是一个搜索条件. UI 不会特别高亮此标签.
         */
        val isMatch: Boolean? = null,
    )

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
        ): List<Tag> = buildList {
            fun emit(
                value: String,
                isMissing: Boolean = false,
                isMatch: Boolean? = null,
            ): Boolean = add(Tag(value, isMissing, isMatch))

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
                            isMatch = MediaListFilters.ContainsEpisodeSort.applyOn(candidate),
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

                            tag.isMissing -> {
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
