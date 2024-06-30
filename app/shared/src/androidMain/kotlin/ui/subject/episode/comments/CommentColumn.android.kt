package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.repositories.Comment
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.datasources.bangumi.models.BangumiAvatar
import me.him188.ani.datasources.bangumi.models.BangumiUser
import me.him188.ani.datasources.bangumi.models.BangumiUserGroup
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@Preview
@Composable
private fun PreviewCommentColumn() {
    ProvideCompositionLocalsForPreview {
        CommentColumn(
            remember {
                CommentViewModel(1227087)
            },
        )
    }
}

@Preview
@Composable
private fun PreviewComment() {
    ProvideCompositionLocalsForPreview {
        Comment(
            remember {
                BangumiUser(
                    id = 0,
                    username = "username",
                    avatar = BangumiAvatar(
                        large = "https://picsum.photos/200/300",
                        medium = "https://picsum.photos/200/300",
                        small = "https://picsum.photos/200/300",
                    ),
                    nickname = "Nickname",
                    userGroup = BangumiUserGroup.User,
                    sign = "sign",
                )
            },

            comment = remember {
                Comment(
                    id = "1",
                    type = 8010,
                    summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer nec odio. Praesent libero. Sed cursus ante dapibus diam. Sed nisi. Nulla quis sem at nibh elementum imperdiet.",
                    createdAt = run {
                        if (Random.nextBoolean()) {
                            System.currentTimeMillis() - 1.minutes.inWholeMilliseconds
                        } else {
                            System.currentTimeMillis() - 2.days.inWholeMilliseconds
                        }
                    },
                    authorUsername = null,
                )
            },
        )

    }
}
