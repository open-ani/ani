/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.rss.test

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.models.ApiFailure
import me.him188.ani.app.tools.rememberUiMonoTasker
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.interaction.nestedScrollWorkaround
import me.him188.ani.app.ui.foundation.layout.connectedScroll
import me.him188.ani.app.ui.foundation.layout.rememberConnectedScrollState
import me.him188.ani.app.ui.foundation.widgets.FastLinearProgressIndicator
import me.him188.ani.app.ui.settings.mediasource.EditMediaSourceTestDataCardDefaults
import me.him188.ani.app.ui.settings.mediasource.rss.detail.RssViewingItem
import me.him188.ani.utils.platform.isMobile

@Composable
fun RssTestPane(
    state: RssTestPaneState,
    onNavigateToDetails: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Column(
        modifier
            .padding(contentPadding),
    ) {
        val connectedScrollState = rememberConnectedScrollState()
        Column(Modifier.connectedScroll(connectedScrollState)) {
            Text("测试数据源", style = MaterialTheme.typography.headlineSmall)

            EditTestDataCard(
                state,
                Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth(),
            )

            Row(Modifier.padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("查询结果", style = MaterialTheme.typography.headlineSmall)

                IconButton({ state.searcher.restartCurrentSearch() }) {
                    Icon(Icons.Rounded.RestartAlt, contentDescription = "刷新")
                }

                val searchResult = state.searcher.searchResult
                AnimatedVisibility(searchResult is RssTestResult.Failed) {
                    if (searchResult !is RssTestResult.Failed) return@AnimatedVisibility
                    TextButton(
                        onClick = {
                            state.searcher.restartCurrentSearch() // TODO: see error detail 
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Icon(Icons.Rounded.Error, null, Modifier.align(Alignment.CenterVertically))
                        Text(
                            when (searchResult) {
                                is RssTestResult.ApiError -> {
                                    when (searchResult.reason) {
                                        ApiFailure.NetworkError -> "网络错误"
                                        ApiFailure.ServiceUnavailable -> "服务器错误"
                                        ApiFailure.Unauthorized -> "未授权"
                                    }
                                }

                                is RssTestResult.UnknownError -> "未知错误: ${searchResult.exception}"
                            },
                            Modifier.padding(start = 8.dp).align(Alignment.CenterVertically),
                        )
                    }
                }
            }

            Box(Modifier.height(12.dp), contentAlignment = Alignment.Center) {
//            androidx.compose.animation.AnimatedVisibility(
//                state.isSearching,
//                enter = expandVertically(tween(300, easing = StandardDecelerate)),
//                exit = shrinkVertically(tween(300, easing = StandardAccelerate)),
//            ) {
//                LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 4.dp))
//            }

                FastLinearProgressIndicator(state.searcher.isSearching, delayMillis = 0, minimumDurationMillis = 300)
            }
        }
        val tabs = RssTestPaneTab.entries
        val pagerState = state.pagerState

        val switchTabTasker = rememberUiMonoTasker()
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, tab ->
                SegmentedButton(
                    selected = index == pagerState.currentPage,
                    onClick = { switchTabTasker.launch { pagerState.animateScrollToPage(index) } },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
                    Modifier.weight(1f),
                ) {
                    Text(
                        when (tab) {
                            RssTestPaneTab.Overview -> "总览"
                            RssTestPaneTab.RssInfo -> "RSS 信息"
                            RssTestPaneTab.FinalResult -> "最终结果"
                        },
                        maxLines = 1,
                    )
                }
            }
        }

        Crossfade(state.searcher.searchResult, Modifier.padding(top = 20.dp)) { result ->

            HorizontalPager(
                pagerState,
                userScrollEnabled = LocalPlatform.current.isMobile(),
                verticalAlignment = Alignment.Top,
            ) { pageIndex ->
                if (result !is RssTestResult.Success) return@HorizontalPager

                when (RssTestPaneTab.entries[pageIndex]) {
                    RssTestPaneTab.Overview -> {
                        RssTestPaneDefaults.OverviewTab(
                            result,
                            Modifier
                                .nestedScrollWorkaround(state.overallTabGridState, connectedScrollState)
                                .nestedScroll(connectedScrollState.nestedScrollConnection),
                            state = state.overallTabGridState,
                        )
                    }

                    RssTestPaneTab.RssInfo -> {
                        RssTestPaneDefaults.RssInfoTab(
                            result.rssItems,
                            onViewDetails = { item ->
                                state.viewDetails(item)
                                onNavigateToDetails()
                            },
                            selectedItemProvider = {
                                (state.viewingItem as? RssViewingItem.ViewingRssItem)?.value
                            },
                            Modifier
                                .nestedScrollWorkaround(state.rssTabGridState, connectedScrollState)
                                .nestedScroll(connectedScrollState.nestedScrollConnection),
                            lazyStaggeredGridState = state.rssTabGridState,
                        )
                    }

                    RssTestPaneTab.FinalResult -> {
                        RssTestPaneDefaults.FinalResultTab(
                            result,
                            onViewDetails = { item ->
                                state.viewDetails(item)
                                onNavigateToDetails()
                            },
                            selectedItemProvider = {
                                (state.viewingItem as? RssViewingItem.ViewingMedia)?.value
                            },
                            Modifier
                                .nestedScrollWorkaround(state.finalResultsTabGridState, connectedScrollState)
                                .nestedScroll(connectedScrollState.nestedScrollConnection),
                            lazyGridState = state.finalResultsTabGridState,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditTestDataCard(
    state: RssTestPaneState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier,
        shape = EditMediaSourceTestDataCardDefaults.cardShape,
        colors = EditMediaSourceTestDataCardDefaults.cardColors,
    ) {
        EditMediaSourceTestDataCardDefaults.FlowRow {
            EditMediaSourceTestDataCardDefaults.KeywordTextField(state, Modifier.weight(1f))
            EditMediaSourceTestDataCardDefaults.EpisodeSortTextField(state, Modifier.weight(1f))
            if (state.showPage) {
                TextField(
                    value = state.pageString,
                    onValueChange = { state.pageString = it.trim() },
                    Modifier.weight(1f),
                    label = { Text("页码") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                    isError = state.pageIsError,
                )
            }
        }
    }
}

@Immutable
enum class RssTestPaneTab {
    Overview,
    RssInfo,
    FinalResult,
}

@Stable
object RssTestPaneDefaults
