package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.richtext.UIRichElement
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@Preview
@Composable
private fun PreviewCommentColumn() {
    ProvideCompositionLocalsForPreview {
        EpisodeCommentColumn(1227087)
    }
}

@Preview
@Composable
private fun PreviewComment() {
    ProvideCompositionLocalsForPreview {
        Comment(
            comment = remember {
                UiComment(
                    id = "1",
                    content = UIRichText(
                        listOf(
                            UIRichElement.AnnotatedText(
                                listOf(
                                    UIRichElement.Annotated.Text(
                                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer nec odio. Praesent libero. Sed cursus ante dapibus diam. Sed nisi. Nulla quis sem at nibh elementum imperdiet.",

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
                        nickname = "nickname him188",
                        avatarUrl = "https://picsum.photos/200/300",
                    ),
                    briefReplies = listOf(),
                    replyCount = 0,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )

    }
}
