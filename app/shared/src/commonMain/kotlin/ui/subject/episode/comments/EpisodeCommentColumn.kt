/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.foundation.LocalImageViewerHandler
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.ui.foundation.isInDebugMode
import me.him188.ani.app.ui.foundation.layout.ConnectedScrollState
import me.him188.ani.app.ui.richtext.RichText
import me.him188.ani.app.ui.subject.components.comment.Comment
import me.him188.ani.app.ui.subject.components.comment.CommentColumn
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
    connectedScrollState: ConnectedScrollState? = null,
) {
    val imageViewer = LocalImageViewerHandler.current

    Column(modifier) {
        CommentColumn(
            state = state,
            listState = lazyListState,
            connectedScrollState = connectedScrollState,
            modifier = Modifier.weight(1f).fillMaxSize(),
        ) { _, comment ->
            EpisodeComment(
                comment = comment,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    // 如果没有回复则 ActionBar 就是最后一个元素，减小一下 bottom padding 以看起来舒服
                    .padding(top = 12.dp, bottom = if (comment.replyCount != 0) 12.dp else 4.dp),
                onClickImage = { imageViewer.viewImage(it) },
                onActionReply = { onClickReply(comment.id) },
                onClickUrl = onClickUrl,
            )
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
