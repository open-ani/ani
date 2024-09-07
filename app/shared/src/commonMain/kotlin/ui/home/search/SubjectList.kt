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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.yield
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.AsyncImage

class SubjectPreviewListState(
    items: State<List<SubjectInfo>>,
    hasMore: State<Boolean>,
    private val onRequestMore: suspend () -> Unit,
    backgroundScope: CoroutineScope,
) {
    val items by items

    private val loadMoreTasker = MonoTasker(backgroundScope)
    val isLoading by loadMoreTasker::isRunning

    fun loadMore() {
        if (isLoading) return
        loadMoreTasker.launch {
            yield()
            onRequestMore()
        }

//        if (!hasMore) return
//
//        loadMoreTasker.launch(start = CoroutineStart.UNDISPATCHED) {
//            yield()
//
//            try {
//                val nextPage = pagedSource.nextPageOrNull()
//                if (nextPage == null) {
//                    _hasMore.value = false
//                } else {
//                    _list.value += nextPage
//                    _hasMore.value = true
//                }
//            } catch (e: Throwable) {
//                _hasMore.value = false
//                throw e
//            } finally {
//                _loading.value = false
//            }
//        }
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
    val lazyGridState = rememberLazyGridState()
    val layoutDirection = LocalLayoutDirection.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier.background(MaterialTheme.colorScheme.background),
        state = lazyGridState,
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 8.dp,
            start = contentPadding.calculateStartPadding(layoutDirection) + 8.dp,
            end = contentPadding.calculateEndPadding(layoutDirection) + 8.dp,

            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 用这个来让 (初始化时) 增加新的元素时, 保持滚动位置在最开始, 而不是到最后
        item("dummy", span = { GridItemSpan(maxLineSpan) }) {}

        items(state.items, key = { it.id }) { subject ->
            val navigator = LocalNavigator.current
            SubjectPreviewCard(
                title = subject.displayName,
                imageUrl = remember(subject.id) { subject.imageCommon },
                onClick = { navigator.navigateSubjectDetails(subject.id) },
                Modifier.animateItem().height(180.dp),
            )
        }

        item("loading", span = { GridItemSpan(maxLineSpan) }, contentType = "loading") {
            SideEffect {
                state.loadMore()
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
    title: String,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Card(
        modifier
            .shadow(2.dp, shape)
            .clip(shape)
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            ),
        shape = shape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            AsyncImage(
                imageUrl,
                title,
                Modifier.fillMaxWidth().height(120.dp).background(Color.LightGray),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                title,
                Modifier.padding(all = 8.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}
