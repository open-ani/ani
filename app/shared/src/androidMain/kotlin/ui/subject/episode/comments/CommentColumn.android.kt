package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.richtext.UIRichElement
import me.him188.ani.app.ui.subject.components.comment.UIRichText
import me.him188.ani.app.ui.subject.details.components.generateUiComment
import me.him188.ani.app.ui.subject.details.components.rememberTestCommentState

@Preview
@Composable
private fun PreviewEpisodeComment() {
    ProvideCompositionLocalsForPreview {
        EpisodeComment(
            comment = remember {
                generateUiComment(
                    size = 1,
                    content = UIRichText(
                        listOf(
                            UIRichElement.AnnotatedText(
                                listOf(
                                    UIRichElement.Annotated.Text("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer nec odio. Praesent libero. Sed cursus ante dapibus diam. Sed nisi. Nulla quis sem at nibh elementum imperdiet."),
                                    UIRichElement.Annotated.Text("masked text", mask = true),
                                ),
                            ),
                        ),
                    ),
                ).single()
            },
            modifier = Modifier.fillMaxWidth(),
            onActionReply = { },
            onClickImage = { },
            onClickUrl = { },
        )

    }
}

@Preview
@Composable
private fun PreviewEpisodeCommentColumn() {
    ProvideCompositionLocalsForPreview {
        EpisodeCommentColumn(
            state = rememberTestCommentState(commentList = generateUiComment(4)),
            editCommentStubText = TextFieldValue("this is my new pending comment"),
            onClickReply = { },
            onClickEditCommentStub = { },
            onClickEditCommentStubEmoji = { },
            onClickUrl = { },
        )
    }
}