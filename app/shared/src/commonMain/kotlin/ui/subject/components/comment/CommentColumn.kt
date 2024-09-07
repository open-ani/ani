package me.him188.ani.app.ui.subject.components.comment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.him188.ani.app.ui.foundation.theme.stronglyWeaken

@Composable
fun CommentColumn(
    state: CommentState,
    modifier: Modifier = Modifier,
    hasDividerLine: Boolean = true,
    listState: LazyListState = rememberLazyListState(),
    pullToRefreshState: PullToRefreshState = rememberPullToRefreshState(),
    commentItem: @Composable LazyItemScope.(index: Int, item: UIComment) -> Unit
) {
    LaunchedEffect(true) {
        launch {
            snapshotFlow { state.sourceVersion }
                .distinctUntilChanged()
                .collectLatest {
                    pullToRefreshState.startRefresh()
                }
        }
        launch {
            snapshotFlow { pullToRefreshState.isRefreshing }.collectLatest { refreshing ->
                if (!refreshing) return@collectLatest
                state.reload()
                pullToRefreshState.endRefresh()
            }
        }
    }

    Box(
        modifier = modifier.clipToBounds(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(pullToRefreshState.nestedScrollConnection),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
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
            
            if (state.hasMore) {
                item("dummy loader") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }

                    LaunchedEffect(true) { state.loadMore() }
                }
            }
        }
        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}