package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.foundation.LocalImageViewerHandler
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.ui.foundation.isInDebugMode
import me.him188.ani.app.ui.foundation.richtext.RichText
import me.him188.ani.app.ui.foundation.theme.stronglyWeaken
import me.him188.ani.app.ui.subject.components.comment.Comment
import me.him188.ani.app.ui.subject.components.comment.CommentDefaults
import me.him188.ani.app.ui.subject.components.comment.CommentState
import me.him188.ani.app.ui.subject.components.comment.EditCommentBottomStubPanel
import me.him188.ani.app.ui.subject.components.comment.UIComment

@Composable
fun EpisodeCommentColumn(
    state: CommentState,
    editCommentStubText: TextFieldValue,
    onClickReply: (commentId: Int) -> Unit,
    onClickEditCommentStub: () -> Unit,
    onClickEditCommentStubEmoji: () -> Unit,
    onClickUrl: (url: String) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val imageViewer = LocalImageViewerHandler.current
    val density = LocalDensity.current

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


    Box(modifier = modifier.clipToBounds()) {
        Column {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .nestedScroll(pullToRefreshState.nestedScrollConnection),
                contentPadding = WindowInsets.navigationBars.asPaddingValues(),
            ) {
                item { }
                itemsIndexed(state.list, key = { _, item -> item.id }) { index, item ->
                    EpisodeComment(
                        comment = item,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                            // 如果没有回复则 ActionBar 就是最后一个元素，减小一下 bottom padding 以看起来舒服
                            .padding(top = 12.dp, bottom = if (item.replyCount != 0) 12.dp else 4.dp),
                        onClickImage = { imageViewer.viewImage(it) },
                        onActionReply = { onClickReply(item.id) },
                        onClickUrl = onClickUrl,
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
            if (isInDebugMode() || LocalIsPreviewing.current) {
                HorizontalDivider()
                EditCommentBottomStubPanel(
                    text = editCommentStubText,
                    onClickEditText = onClickEditCommentStub,
                    onClickEmoji = onClickEditCommentStubEmoji,
                )
            }
        }
        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }

}

private const val LOREM_IPSUM =
    "Ipsum dolor sit amet, consectetur adipiscing elit. Integer nec odio. Praesent libero. Sed cursus ante dapibus diam. Sed nisi. Nulla quis sem at nibh elementum imperdiet."

@Composable
fun EpisodeComment(
    comment: UIComment,
    onClickUrl: (String) -> Unit,
    onClickImage: (String) -> Unit,
    onActionReply: () -> Unit,
    modifier: Modifier = Modifier
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
        reactionRow = {
            CommentDefaults.ReactionRow(
                comment.reactions,
                onClickItem = { },
            )
        },
        actionRow = {
            CommentDefaults.ActionRow(
                onClickReply = onActionReply,
                onClickReaction = {},
                onClickBlock = {},
                onClickReport = {},
            )
        },
        reply = if (comment.briefReplies.isNotEmpty()) {
            {
                CommentDefaults.ReplyList(
                    replies = comment.briefReplies,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    hiddenReplyCount = comment.replyCount - comment.briefReplies.size,
                    onClickUrl = { },
                    onClickExpand = { },
                )
            }
        } else null,
    )
}
