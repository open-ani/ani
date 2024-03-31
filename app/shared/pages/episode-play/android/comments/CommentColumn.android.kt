package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import org.openapitools.client.models.Avatar
import org.openapitools.client.models.User
import org.openapitools.client.models.UserGroup

@Preview
@Composable
private fun PreviewCommentColumn() {
    ProvideCompositionLocalsForPreview {
        CommentColumn()
    }
}

@Preview
@Composable
private fun PreviewComment() {
    ProvideCompositionLocalsForPreview {
        Comment(
            remember {
                User(
                    id = 0,
                    username = "username",
                    avatar = Avatar(
                        large = "https://picsum.photos/200/300",
                        medium = "https://picsum.photos/200/300",
                        small = "https://picsum.photos/200/300",
                    ),
                    nickname = "Nickname",
                    userGroup = UserGroup.User,
                    sign = "sign"
                )
            },

            comment = remember {
                Comment(
                    id = "nominavi",
                    type = 8010,
                    summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer nec odio. Praesent libero. Sed cursus ante dapibus diam. Sed nisi. Nulla quis sem at nibh elementum imperdiet.",
                    createdAt = System.currentTimeMillis(),
                    authorUsername = null
                )
            }
        )

    }
}