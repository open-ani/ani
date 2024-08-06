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
import me.him188.ani.app.ui.subject.components.comment.UIRichText
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@Preview
@Composable
private fun PreviewSubjectComment() {
    ProvideCompositionLocalsForPreview {
        SubjectComment(
            comment = remember {
                UIComment(
                    id = 123,
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
                    createdAt = run {
                        if (Random.nextBoolean()) {
                            System.currentTimeMillis() - 1.minutes.inWholeMilliseconds
                        } else {
                            System.currentTimeMillis() - 2.days.inWholeMilliseconds
                        }
                    },
                    creator = UserInfo(
                        id = 1,
                        username = "",
                        nickname = "nickname him188",
                        avatarUrl = "https://picsum.photos/200/300",
                    ),
                    reactions = emptyList(),
                    briefReplies = emptyList(),
                    replyCount = 0,
                    rating = 5,
                )
            },
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

private fun generateUiComment(size: Int) = buildList {
    repeat(size) { i ->
        add(
            UIComment(
                id = i,
                content = UIRichText(
                    listOf(
                        UIRichElement.AnnotatedText(
                            listOf(
                                UIRichElement.Annotated.Text(
                                    "$i Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer nec odio. Praesent libero. Sed cursus ante dapibus diam. Sed nisi. Nulla quis sem at nibh elementum imperdiet.",

                                    ),
                            ),
                        ),
                    ),
                ),
                createdAt = run {
                    if (Random.nextBoolean()) {
                        System.currentTimeMillis() - 1.minutes.inWholeMilliseconds
                    } else {
                        System.currentTimeMillis() - 2.days.inWholeMilliseconds
                    }
                },
                creator = UserInfo(
                    id = 1,
                    username = "",
                    nickname = "nickname him188 $i",
                    avatarUrl = "https://picsum.photos/200/300",
                ),
                reactions = emptyList(),
                briefReplies = emptyList(),
                replyCount = 0,
                rating = (0..10).random(),
            ),
        )
    }
}