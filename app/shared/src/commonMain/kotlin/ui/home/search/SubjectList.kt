/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.app.ui.home.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.yield
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.subject.details.components.SubjectDetailsHeader

class SubjectPreviewListState(
    items: State<List<SubjectInfo>>,
    hasMore: State<Boolean>,
    private val onRequestMore: suspend () -> Unit,
    backgroundScope: CoroutineScope,
) {
    val items by items
    val hasMore by hasMore

    private val loadMoreTasker = MonoTasker(backgroundScope)
    val isLoading by loadMoreTasker::isRunning

    fun loadMore() {
        if (isLoading) return
        loadMoreTasker.launch {
            yield()
            onRequestMore()
        }
    }
}

/**
 * 番剧预览列表, 双列模式
 */
@Composable
fun SubjectPreviewColumn(
    state: SubjectPreviewListState,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(360.dp),
        modifier.background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 20.dp,
            start = contentPadding.calculateStartPadding(layoutDirection) + 20.dp,
            end = contentPadding.calculateEndPadding(layoutDirection) + 20.dp,

            ),
        verticalItemSpacing = 20.dp,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // 用这个来让 (初始化时) 增加新的元素时, 保持滚动位置在最开始, 而不是到最后
        item("dummy", span = StaggeredGridItemSpan.FullLine) {}

        items(state.items, key = { it.id }) { subject ->
            val navigator = LocalNavigator.current
            SubjectPreviewCard(
//                title = { Text(subject.displayName) },
                imageUrl = subject.imageLarge,
//                airingLabel = {
//
//
//                    AiringLabel(
//                        remember(subject) {
//                            AiringLabelState(
//                                stateOf(SubjectAiringInfo.computeFromSubjectInfo(subject)),
//                                stateOf(SubjectProgressInfo.Start),
//                            )
//                        },
//                    )
//                },
                onClick = { navigator.navigateSubjectDetails(subject.id) },
                Modifier.animateItemPlacement(),
            ) {
                SubjectDetailsHeader(
                    subject,
                    subject.imageLarge,
                    seasonTags = {},
                    collectionData = {
//                        SubjectDetailsDefaults.CollectionData(
//                            collectionStats = subject.collection,
//                        )
                    },
                    collectionAction = {

                    },
                    selectEpisodeButton = {},
                    rating = {
                        Button({}) {
                            Text("查看详情")
                        }
//                        Rating(
//                            rating = subject.ratingInfo,
//                            selfRatingScore = 0,
//                            onClick = {},
//                            clickEnabled = false,
//                        )
                    },
                    Modifier.padding(all = 16.dp),
                    showLandscapeUI = false,
                )
            }
        }

        item("loading", span = StaggeredGridItemSpan.FullLine, contentType = "loading") {
            if (state.hasMore) {
                SideEffect {
                    state.loadMore()
                }
            }
            if (state.isLoading) {
                Row(
                    Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp)
                    Text(
                        "加载中",
                        Modifier.padding(start = 8.dp),
                        softWrap = false,
                    )
                }
                return@item
            }

            // no more items 
        }

        item("footer") {}
    }
}

/**
 * 一个番剧预览卡片, 一行显示两个的那种, 只有图片和名称
 */
@Composable
fun SubjectPreviewCard(
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    Card(
        onClick,
        modifier,
        shape = shape,
    ) {
        Row {
//            Card(shape = shape) {
//                AsyncImage(
//                    imageUrl,
//                    null,
//                    Modifier.fillMaxWidth()
//                        .heightIn(min = 160.dp, max = 320.dp)
//                        .background(Color.LightGray),
//                    contentScale = ContentScale.Crop,
//                )
//            }

            Column {
                content()
            }
        }
    }
}
