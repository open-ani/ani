package me.him188.ani.app.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.datasources.bangumi.models.BangumiAvatar
import me.him188.ani.datasources.bangumi.models.BangumiUser
import me.him188.ani.datasources.bangumi.models.BangumiUserGroup

@Preview
@Composable
internal fun PreviewSelfInfo() {
    ProvideCompositionLocalsForPreview {
        SelfInfo(
            BangumiUser(
                username = "username",
                avatar = BangumiAvatar(
                    "https://example.com/avatar.jpg",
                    "https://example.com/avatar.jpg",
                    "https://example.com/avatar.jpg",
                ),
                id = 1,
                nickname = "",
                sign = "Sign ".repeat(3),
                userGroup = BangumiUserGroup.User,
            ),
            true,
            onClickSettings = {},
        )
    }
}

@Preview
@Composable
private fun PreviewProfilePage() {
    ProvideCompositionLocalsForPreview {
        ProfilePage(
            onClickSettings = {},
        )
    }
}