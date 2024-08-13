package me.him188.ani.app.ui.subject.details.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.rememberBackgroundScope
import me.him188.ani.app.ui.foundation.richtext.UIRichElement
import me.him188.ani.app.ui.subject.components.comment.CommentState
import me.him188.ani.app.ui.subject.components.comment.UIComment
import me.him188.ani.app.ui.subject.components.comment.UICommentReaction
import me.him188.ani.app.ui.subject.components.comment.UIRichText
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

@Preview
@Composable
private fun PreviewSubjectComment() {
    ProvideCompositionLocalsForPreview {
        SubjectComment(
            comment = remember { generateUiComment(1).single() },
            modifier = Modifier.fillMaxWidth(),
            onClickImage = { },
            onClickUrl = { },
        )

    }
}

@Preview
@Composable
private fun PreviewSubjectCommentColumn() {
    ProvideCompositionLocalsForPreview {
        val scope = rememberBackgroundScope()
        SubjectCommentColumn(
            state = remember {
                CommentState(
                    sourceVersion = mutableStateOf(Any()),
                    list = mutableStateOf(generateUiComment(4)),
                    hasMore = mutableStateOf(false),
                    onReload = { },
                    onLoadMore = { },
                    backgroundScope = scope.backgroundScope,
                )
            },
            onClickUrl = { },
        )
    }
}

fun generateUiComment(
    size: Int,
    content: UIRichText = UIRichText(
        listOf(
            UIRichElement.AnnotatedText(
                listOf(
                    UIRichElement.Annotated.Text(
                        "${(0..1000).random()}Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                                "Integer nec odio. Praesent libero. Sed cursus ante dapibus diam. Sed nisi. Nulla " +
                                "quis sem at nibh elementum imperdiet.",
                    ),
                ),
            ),
        ),
    ),
    generateReply: Boolean = false
): List<UIComment> = buildList {
    repeat(size) { i ->
        add(
            UIComment(
                id = i,
                content = content,
                createdAt = run {
                    System.currentTimeMillis() - (1..10000).random().minutes.inWholeMilliseconds
                },
                creator = UserInfo(
                    id = (1..100).random(),
                    username = "",
                    nickname = "nickname him188 $i",
                    avatarUrl = "https://picsum.photos/200/300",
                ),
                reactions = buildList {
                    repeat((0..8).random()) {
                        add(UICommentReaction((0..100).random(), (0..100).random(), Random.nextBoolean()))
                    }
                },
                briefReplies = if (generateReply) {
                    generateUiComment((0..3).random(), content, false)
                } else emptyList(),
                replyCount = (0..100).random(),
                rating = (0..10).random(),
            ),
        )
    }
}