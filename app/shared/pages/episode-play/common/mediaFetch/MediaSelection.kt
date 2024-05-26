package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.subject.episode.details.renderSubtitleLanguage
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.topic.FileSize


private inline val WINDOW_VERTICAL_PADDING get() = 8.dp

// For search: "数据源"
/**
 * 通用的数据源选择器. See preview
 *
 * @param progressProvider `1f` to hide the progress bar. `null` to show a endless progress bar.
 * @param actions shown at the bottom
 */
@Composable
fun MediaSelector(
    state: MediaSelectorState,
    modifier: Modifier = Modifier,
    progressProvider: () -> Float? = { 1f },
    onClickItem: ((Media) -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) = Surface {
    Column(modifier) {
        Row(Modifier.fillMaxWidth()) {
            val progress = progressProvider() // recompose this row only as the progress might update frequently
            if (progress == 1f) return@Row

            // 刻意一直展示一个一直在动的进度条, 因为实测其实资源都是一起来的, 也就是进度很多时候只有 0 和 1.
            // 如果按进度展示进度条, 就会一直是 0, 进度条整个是白色的, 看不出来, 不容易区分是正在加载还是加载完了.
            LinearProgressIndicator(
                Modifier.padding(bottom = WINDOW_VERTICAL_PADDING)
                    .fillMaxWidth()
            )
        }

        val lazyListState = rememberLazyListState()
        LazyColumn(
            Modifier.padding(bottom = WINDOW_VERTICAL_PADDING).weight(1f, fill = false),
            lazyListState,
        ) {
            item {
                MediaSourceResultsRow()
            }

            item {
                HorizontalDivider(Modifier.padding(top = 8.dp))
            }

            stickyHeader {
                val isStuck by remember(lazyListState) {
                    derivedStateOf {
                        lazyListState.firstVisibleItemIndex == 2
                    }
                }
                Surface(
//                    tonalElevation = if (isStuck) 3.dp else 0.dp,
                    shadowElevation = if (isStuck) 3.dp else 0.dp
                ) {
                    Column(
                        Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MediaSelectorFilters(state)

                        Text(
                            remember(state.candidates.size, state.mediaList.size) {
                                "筛选到 ${state.candidates.size}/${state.mediaList.size} 条资源"
                            },
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    if (isStuck) {
                        HorizontalDivider(Modifier.fillMaxWidth())
                    }
                }
            }

            items(state.candidates, key = { it.mediaId }) { item ->
                MediaItem(
                    item,
                    state.selected == item,
                    state,
                    onClick = {
                        state.select(item)
                        onClickItem?.invoke(item)
                    },
                    Modifier
                        .animateItemPlacement()
                        .fillMaxWidth().padding(bottom = 8.dp),
                )
            }
            item { } // dummy spacer
        }

        if (actions != null) {
            HorizontalDivider(Modifier.padding(bottom = 8.dp))

            Row(
                Modifier.align(Alignment.End).padding(bottom = 8.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                    actions()
                }
            }
        }
    }
}

@Composable
private fun MediaSourceResultsRow(modifier: Modifier = Modifier) {
    LazyHorizontalGrid(
        GridCells.Fixed(2),
        Modifier.height(64.dp * 2 + 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            OutlinedCard(
                modifier,
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(
                    Modifier.padding(all = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Mikan", style = MaterialTheme.typography.titleMedium)

                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Outlined.Subtitles, "弹幕数量")
                        }
                    }

                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                        }
                    }
                }
            }
        }
    }
}


private inline val minWidth get() = 60.dp
private inline val maxWidth get() = 120.dp

/**
 * 筛选
 */
@Composable
private fun MediaSelectorFilters(
    state: MediaSelectorState,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MediaSelectorFilterChip(
            selected = state.selectedMediaSource,
            allValues = { state.mediaSources },
            onSelect = { state.preferMediaSource(it) },
            onDeselect = { state.preferMediaSource(it, removeOnExist = true) },
            name = { Text("数据源") },
            Modifier.widthIn(min = minWidth, max = maxWidth),
            label = { MediaSelectorFilterChipText(renderMediaSource(it)) },
            leadingIcon = { id ->
                getMediaSourceIcon(id)?.let {
                    Icon(it, null)
                }
            }
        )
        MediaSelectorFilterChip(
            selected = state.selectedResolution,
            allValues = { state.resolutions },
            onSelect = { state.preferResolution(it) },
            onDeselect = { state.preferResolution(it, removeOnExist = true) },
            name = { Text("分辨率") },
            Modifier.widthIn(min = minWidth, max = maxWidth),
        )
        MediaSelectorFilterChip(
            selected = state.selectedSubtitleLanguageId,
            allValues = { state.subtitleLanguageIds },
            onSelect = { state.preferSubtitleLanguage(it) },
            onDeselect = { state.preferSubtitleLanguage(it, removeOnExist = true) },
            name = { Text("字幕") },
            Modifier.widthIn(min = minWidth, max = maxWidth),
            label = { MediaSelectorFilterChipText(renderSubtitleLanguage(it)) }
        )
        MediaSelectorFilterChip(
            selected = state.selectedAlliance,
            allValues = { state.alliances },
            onSelect = { state.preferAlliance(it) },
            onDeselect = { state.preferAlliance(it, removeOnExist = true) },
            name = { Text("字幕组") },
            Modifier.widthIn(min = minWidth, max = maxWidth),
        )
    }
}

@Composable
private fun MediaSelectorFilterChipText(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        overflow = TextOverflow.Clip,
        softWrap = false,
        maxLines = 1,
        modifier = modifier,
    )
}

/**
 * @param selected 选中的值, 为 null 时表示未选中
 * @param name 未被选中时显示
 * @param label 选中时显示
 */
@Composable
private fun <T : Any> MediaSelectorFilterChip(
    selected: T?,
    allValues: () -> List<T>,
    onSelect: (T) -> Unit,
    onDeselect: (T) -> Unit,
    name: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (T) -> Unit = { MediaSelectorFilterChipText(it.toString()) },
    leadingIcon: @Composable ((T?) -> Unit)? = null,
) {
    var showDropdown by rememberSaveable {
        mutableStateOf(false)
    }

    val allValuesState by remember(allValues) {
        derivedStateOf(allValues)
    }
    val isSingleValue by remember { derivedStateOf { allValuesState.size == 1 } }
    val selectedState by rememberUpdatedState(selected)

    Box {
        InputChip(
            selected = isSingleValue || selected != null,
            onClick = {
                if (!isSingleValue) {
                    showDropdown = true
                }
            },
            label = {
                if (isSingleValue) {
                    allValuesState.firstOrNull()?.let {
                        label(it)
                    }
                } else {
                    Box {
                        Box(Modifier.alpha(if (selectedState == null) 1f else 0f)) {
                            name()
                        }
                        selectedState?.let {
                            label(it)
                        }
                    }
                }
            },
            leadingIcon = leadingIcon?.let { { leadingIcon(selectedState) } },
            trailingIcon = if (isSingleValue) null else {
                {
                    if (selected == null) {
                        Icon(Icons.Default.ArrowDropDown, "展开")
                    } else {
                        Icon(
                            Icons.Default.Close, "取消筛选",
                            Modifier.clickable { selectedState?.let { onDeselect(it) } }
                        )
                    }
                }
            },
            modifier = modifier,
        )

        DropdownMenu(showDropdown, onDismissRequest = { showDropdown = false }) {
            allValuesState.forEach { item ->
                DropdownMenuItem(
                    text = { label(item) },
                    trailingIcon = {
                        if (selectedState == item) {
                            Icon(Icons.Default.Check, "当前选中")
                        }
                    },
                    onClick = {
                        onSelect(item)
                        showDropdown = false
                    }
                )
            }
        }
    }
}

/**
 * 一个资源的卡片
 */
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
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer
            else CardDefaults.elevatedCardColors().containerColor
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
                    onClick = { state.preferResolution(media.properties.resolution) },
                    label = { Text(media.properties.resolution) },
                    enabled = state.selectedResolution != media.properties.resolution,
                )
                media.properties.subtitleLanguageIds.map {
                    InputChip(
                        false,
                        onClick = { state.preferSubtitleLanguage(it) },
                        label = { Text(renderSubtitleLanguage(it)) },
                        enabled = state.selectedSubtitleLanguageId != it,
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when (media.location) {
                                MediaSourceLocation.Local -> Icon(Icons.Rounded.DownloadDone, null)
                                MediaSourceLocation.Lan -> Icon(Icons.Rounded.Radar, null)
                                MediaSourceLocation.Online -> Icon(Icons.Rounded.Public, null)
                            }

                            Text(
                                remember(media.mediaSourceId) { renderMediaSource(media.mediaSourceId) },
                                maxLines = 1,
                                softWrap = false,
                            )
                        }

                        Box(Modifier.weight(1f, fill = false), contentAlignment = Alignment.Center) {
                            Text(
                                media.properties.alliance,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
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
