/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.components.comment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.interaction.nestedScrollWorkaround
import me.him188.ani.app.ui.foundation.layout.ConnectedScrollState
import me.him188.ani.app.ui.foundation.theme.stronglyWeaken
import me.him188.ani.app.ui.foundation.thenNotNull
import me.him188.ani.app.ui.foundation.widgets.PullToRefreshBox
import me.him188.ani.utils.platform.isMobile

@Composable
fun CommentColumn(
    state: CommentState,
    modifier: Modifier = Modifier,
    hasDividerLine: Boolean = true,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    connectedScrollState: ConnectedScrollState? = null,
    commentItem: @Composable LazyItemScope.(index: Int, item: UIComment) -> Unit
) {
    LaunchedEffect(true) {
        launch {
            snapshotFlow { state.sourceVersion }
                .collectLatest {
                    if (state.sourceVersionEquals()) return@collectLatest
                    state.sourceVersion = it
                }
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { state.reload() },
        modifier = modifier,
        enabled = LocalPlatform.current.isMobile(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .thenNotNull(
                    connectedScrollState?.let {
                        Modifier.nestedScroll(connectedScrollState.nestedScrollConnection)
                            .nestedScrollWorkaround(listState, connectedScrollState)
                    },
                )
                .fillMaxSize(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = contentPadding,
        ) {
            item("spacer header") { Spacer(Modifier.height(1.dp)) }

            itemsIndexed(items = state.list, key = { _, item -> item.id }) { index, item ->
                commentItem(index, item)

                if (hasDividerLine && index != state.list.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        color = DividerDefaults.color.stronglyWeaken(),
                    )
                }
            }

            item("dummy loader") {
                if (state.hasMore) { // 刚开始的时候, hasMore 为 false
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Spacer(Modifier.height(1.dp)) // 可能 lazy column bug, 如果有一个空的 item, 会导致滚动很卡
                }

                // 总是请求, 是为了在刚刚进入时请求一下. 因为是 lazy 的, 不请求列表就一直是空的
                LaunchedEffect(true) { state.loadMore() }
            }
        }
    }
}