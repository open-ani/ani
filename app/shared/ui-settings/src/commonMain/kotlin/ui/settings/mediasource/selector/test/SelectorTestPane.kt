/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.selector.test

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.layout.cardHorizontalPadding
import me.him188.ani.app.ui.foundation.layout.cardVerticalPadding
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.app.ui.foundation.widgets.FastLinearProgressIndicator
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.settings.mediasource.EditMediaSourceTestDataCardDefaults
import me.him188.ani.app.ui.settings.mediasource.RefreshIndicatedHeadlineRow
import me.him188.ani.app.ui.settings.mediasource.selector.edit.SelectorConfigurationDefaults

/**
 * 测试数据源. 编辑
 */
@Composable
fun SharedTransitionScope.SelectorTestPane(
    state: SelectorTestState,
    onViewEpisode: (SelectorTestEpisodePresentation) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val verticalSpacing = currentWindowAdaptiveInfo().windowSizeClass.cardVerticalPadding
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        modifier,
        rememberLazyGridState(),
        contentPadding,
        horizontalArrangement = Arrangement.spacedBy(currentWindowAdaptiveInfo().windowSizeClass.cardHorizontalPadding),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Text(
                    "测试数据源",
                    style = MaterialTheme.typography.headlineSmall,
                )

                EditTestDataCard(
                    state,
                    Modifier
                        .padding(top = verticalSpacing)
                        .fillMaxWidth(),
                )

                RefreshIndicatedHeadlineRow(
                    headline = { Text(SelectorConfigurationDefaults.STEP_NAME_1) },
                    onRefresh = { state.subjectSearcher.restartCurrentSearch() },
                    result = state.subjectSearchSelectResult,
                    Modifier.padding(top = verticalSpacing),
                )

                Box(Modifier.height(12.dp), contentAlignment = Alignment.Center) {
                    FastLinearProgressIndicator(
                        state.subjectSearcher.isSearching,
                        delayMillis = 0,
                        minimumDurationMillis = 300,
                    )
                }

                AnimatedContent(
                    state.subjectSearchSelectResult,
                    transitionSpec = AniThemeDefaults.standardAnimatedContentTransition,
                ) { result ->
                    if (result is SelectorTestSearchSubjectResult.Success) {
                        SelectorTestSubjectResultLazyRow(
                            items = result.subjects,
                            state.selectedSubjectIndex,
                            onSelect = { index, _ ->
                                state.selectedSubjectIndex = index
                            },
                            modifier = Modifier.padding(top = verticalSpacing - 8.dp),
                        )
                    }
                }
            }
        }

        val selectedSubject = state.selectedSubject
        if (selectedSubject != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    RefreshIndicatedHeadlineRow(
                        headline = { Text(SelectorConfigurationDefaults.STEP_NAME_2) },
                        onRefresh = { state.episodeListSearcher.restartCurrentSearch() },
                        result = state.episodeListSearchSelectResult,
                        Modifier.padding(top = verticalSpacing),
                    )

                    val url = state.selectedSubject?.subjectDetailsPageUrl ?: ""
                    val clipboard = LocalClipboardManager.current
                    val toaster = LocalToaster.current
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable(onClickLabel = "复制条目链接") {
                                clipboard.setText(AnnotatedString(url))
                                toaster.toast("已复制")
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Rounded.Link,
                            contentDescription = null,
                            Modifier.padding(end = 16.dp).size(24.dp),
                        )
                        Text(
                            url,
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        val uriHandler = LocalUriHandler.current
                        IconButton({ uriHandler.openUri(url) }, Modifier.padding(start = 8.dp)) {
                            Icon(
                                Icons.Rounded.ArrowOutward,
                                contentDescription = "打开条目页面",
                            )
                        }
                    }

                    Box(Modifier.height(4.dp), contentAlignment = Alignment.Center) {
                        FastLinearProgressIndicator(
                            state.episodeListSearcher.isSearching,
                            delayMillis = 0,
                            minimumDurationMillis = 300,
                        )
                    }
                }
            }

            val result = state.episodeListSearchSelectResult
            if (result is SelectorTestEpisodeListResult.Success) {
                if (result.channels != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            Modifier
                                .fillMaxWidth() // workaround for grid span issue https://youtrack.jetbrains.com/issue/CMP-2102
                                .padding(bottom = (verticalSpacing - 8.dp).coerceAtLeast(0.dp)),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("${result.channels.size} 线路")
                            LazyRow(
                                Modifier,
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                items(result.channels) {
                                    FilterChip(
                                        selected = state.filterByChannel == it,
                                        onClick = {
                                            state.filterByChannel = if (state.filterByChannel == it) null else it
                                        },
                                        label = { Text(it) },
                                    )
                                }
                            }
                        }
                    }
                }

                items(
                    state.filteredEpisodes ?: emptyList(),
                    key = { it.id.toString() },
                    contentType = { 1 },
                ) { episode ->
                    SelectorTestEpisodeListGridDefaults.EpisodeCard(
                        episode,
                        { onViewEpisode(episode) },
                        Modifier
                            .fillMaxSize()
                            .padding(bottom = verticalSpacing)
                            .sharedBounds(rememberSharedContentState(episode.id), animatedVisibilityScope),
                    )
                }
            }
        }
    }
}

@Composable
private fun EditTestDataCard(
    state: SelectorTestState,
    modifier: Modifier = Modifier,
) {
    with(EditMediaSourceTestDataCardDefaults) {
        Card(
            modifier,
            shape = cardShape,
            colors = cardColors,
        ) {
            FlowRow {
                KeywordTextField(state, Modifier.weight(1f))
                EpisodeSortTextField(state, Modifier.weight(1f))
            }
        }
    }
}
