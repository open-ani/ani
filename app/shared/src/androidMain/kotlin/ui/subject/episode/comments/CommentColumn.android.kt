package me.him188.ani.app.ui.subject.episode.comments

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
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@Preview
@Composable
private fun PreviewEpisodeComment() {
    ProvideCompositionLocalsForPreview {
        EpisodeComment(
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
                    reactions = listOf(
                        UICommentReaction(1, 143, false),
                        UICommentReaction(1, 120, true),
                        UICommentReaction(1, 76, false),
                        UICommentReaction(1, 20, false),
                        UICommentReaction(1, 12, false),
                        UICommentReaction(1, 5, true),
                    ),
                    briefReplies = generateUiComment(3),
                    replyCount = 4,
                    rating = null,
                )
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
        val scope = rememberBackgroundScope()
        EpisodeCommentColumn(
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
            editCommentStubText = "this is my new pending comment",
            onClickReply = { },
            onClickUrl = { },
            onClickEditCommentStub = { },
            onClickEditCommentStubEmoji = { },
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
                briefReplies = listOf(),
                replyCount = 0,
                rating = null,
            ),
        )
    }
}