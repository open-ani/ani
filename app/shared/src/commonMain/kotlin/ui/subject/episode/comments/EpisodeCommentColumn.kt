package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.foundation.LocalImageViewerHandler
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.richtext.RichText
import me.him188.ani.app.ui.foundation.theme.stronglyWeaken
import me.him188.ani.app.ui.subject.components.comment.Comment
import me.him188.ani.app.ui.subject.components.comment.CommentDefaults
import me.him188.ani.app.ui.subject.components.comment.CommentState
import me.him188.ani.app.ui.subject.components.comment.EditCommentBottomStubPanel
import me.him188.ani.app.ui.subject.components.comment.UIComment

@Composable
fun EpisodeCommentColumn(
    commentState: CommentState,
    editCommentStubText: String,
    modifier: Modifier = Modifier,
    onClickReply: (commentId: Int) -> Unit,
    onClickEditCommentStub: () -> Unit,
    onClickEditCommentStubEmoji: () -> Unit,
    onClickUrl: (url: String) -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val imageViewer = LocalImageViewerHandler.current

    val comments = commentState.list
    val hasMore = commentState.hasMore

    LaunchedEffect(true) {
        launch {
            snapshotFlow { commentState.sourceVersion }
                .distinctUntilChanged()
                .collectLatest {
                    pullToRefreshState.startRefresh()
                }
        }
        launch {
            snapshotFlow { pullToRefreshState.isRefreshing }.collectLatest { refreshing ->
                if (!refreshing) return@collectLatest
                commentState.reload()
                pullToRefreshState.endRefresh()
            }
        }
    }


    Box(modifier = modifier.clipToBounds()) {
        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
        Scaffold(
            bottomBar = {
                EditCommentBottomStubPanel(
                    text = editCommentStubText,
                    hint = "发送评论",
                    onClickEmoji = onClickEditCommentStubEmoji,
                    onClickEditText = onClickEditCommentStub,
                )
            },
        ) { contentPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pullToRefreshState.nestedScrollConnection),
                contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding()),
            ) {
                item { }
                itemsIndexed(comments, key = { _, item -> item.id }) { index, item ->
                    EpisodeComment(
                        comment = item,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                            .padding(top = 12.dp, bottom = 4.dp),
                        onClickImage = { imageViewer.viewImage(it) },
                        onActionReply = { onClickReply(item.id) },
                        onClickUrl = onClickUrl,
                    )
                    if (index != comments.lastIndex) {
                        HorizontalDivider(
                            modifier = modifier.fillMaxWidth(),
                            color = DividerDefaults.color.stronglyWeaken(),
                        )
                    }
                }
                if (hasMore) {
                    item("dummy loader") {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator()
                        }

                        LaunchedEffect(true) { commentState.loadMore() }
                    }
                }
            }
        }
    }

}

private const val LOREM_IPSUM =
    "Ipsum dolor sit amet, consectetur adipiscing elit. Integer nec odio. Praesent libero. Sed cursus ante dapibus diam. Sed nisi. Nulla quis sem at nibh elementum imperdiet."

@Composable
fun EpisodeComment(
    comment: UIComment,
    modifier: Modifier = Modifier,
    onClickUrl: (String) -> Unit,
    onClickImage: (String) -> Unit,
    onActionReply: () -> Unit
) {
    Comment(
        modifier = modifier.ifThen(comment.replyCount != 0) { padding(bottom = 8.dp) },
        avatar = { CommentDefaults.Avatar(comment.creator.avatarUrl) },
        primaryTitle = {
            Text(
                text = comment.creator.nickname ?: comment.creator.id.toString(),
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
        reactionRow = if (comment.reactions.isNotEmpty()) {
            {
                CommentDefaults.ReactionRow(
                    comment.reactions,
                    onClickItem = { },
                )
            }
        } else null,
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
