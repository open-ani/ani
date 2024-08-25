package me.him188.ani.app.ui.subject.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.foundation.LocalImageViewerHandler
import me.him188.ani.app.ui.foundation.richtext.RichText
import me.him188.ani.app.ui.foundation.theme.stronglyWeaken
import me.him188.ani.app.ui.subject.components.comment.Comment
import me.him188.ani.app.ui.subject.components.comment.CommentDefaults
import me.him188.ani.app.ui.subject.components.comment.CommentState
import me.him188.ani.app.ui.subject.components.comment.UIComment
import me.him188.ani.app.ui.subject.rating.FiveRatingStars

@Composable
fun SubjectDetailsDefaults.SubjectCommentColumn(
    state: CommentState,
    onClickUrl: (url: String) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val imageViewer = LocalImageViewerHandler.current

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
        modifier = Modifier.fillMaxSize().clipToBounds(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = modifier,
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item("spacer header") { Spacer(Modifier.height(1.dp)) }
            itemsIndexed(state.list, key = { _, item -> item.id }) { index, item ->
                SubjectComment(
                    comment = item,
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    onClickImage = { imageViewer.viewImage(it) },
                    onClickUrl = onClickUrl,
                    onClickReaction = { commentId, reactionId ->
                        state.submitReaction(commentId, reactionId)
                    },
                )
                if (index != state.list.lastIndex) {
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

@Composable
fun SubjectComment(
    comment: UIComment,
    onClickUrl: (String) -> Unit,
    onClickImage: (String) -> Unit,
    onClickReaction: (commentId: Int, reactionId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Comment(
        avatar = { CommentDefaults.Avatar(comment.creator?.avatarUrl) },
        primaryTitle = {
            Text(
                text = comment.creator?.nickname ?: comment.creator?.id.toString(),
                textAlign = TextAlign.Center,
            )
        },
        secondaryTitle = { Text(formatDateTime(comment.createdAt)) },
        content = {
            RichText(
                elements = comment.content.elements,
                modifier = Modifier.fillMaxWidth(),
                onClickUrl = onClickUrl,
                onClickImage = onClickImage,
            )
        },
        modifier = modifier,
        rhsTitle = {
            if (comment.rating != null && comment.rating > 0) {
                FiveRatingStars(comment.rating)
            }
        },
        reactionRow = {
            CommentDefaults.ReactionRow(
                comment.reactions,
                onClickItem = { onClickReaction(comment.id, it) },
            )
        },
    )
}