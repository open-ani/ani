package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.rememberBackgroundScope
import me.him188.ani.app.ui.subject.components.comment.CommentEditorState
import me.him188.ani.app.ui.subject.components.comment.EditComment
import me.him188.ani.app.ui.subject.components.comment.EditCommentBottomStubPanel
import me.him188.ani.app.ui.subject.components.comment.EditCommentDefaults
import me.him188.ani.app.ui.subject.components.comment.EditCommentSticker

@Preview
@Composable
fun PreviewEditComment() {
    ProvideCompositionLocalsForPreview {
        val scope = rememberBackgroundScope()
        EditComment(
            state = remember {
                CommentEditorState(
                    showExpandEditCommentButton = true,
                    initialEditExpanded = false,
                    panelTitle = mutableStateOf("评论：我心里危险的东西 第二季"),
                    stickers = mutableStateOf(
                        (0..64)
                            .map { EditCommentSticker(it, null) }
                            .toList(),
                    ),
                    onSend = { _, _ -> },
                    backgroundScope = scope.backgroundScope,
                )
            },
        )
    }
}

@Preview
@Composable
fun PreviewEditCommentStickerPanel() {
    ProvideCompositionLocalsForPreview {
        EditCommentDefaults.StickerSelector(
            list = (0..64)
                .map { EditCommentSticker(it, null) }
                .toList(),
            onClickItem = { },
        )
    }
}

@Preview
@Composable
fun PreviewEditCommentBottomStubPanel() {
    ProvideCompositionLocalsForPreview {
        EditCommentBottomStubPanel(
            text = TextFieldValue("发送评论"),
            onClickEditText = { },
            onClickEmoji = { },
        )
    }
}