package me.him188.ani.app.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import org.openapitools.client.models.Avatar
import org.openapitools.client.models.User
import org.openapitools.client.models.UserGroup

@Preview
@Composable
internal fun PreviewSelfInfo() {
    ProvideCompositionLocalsForPreview {
        SelfInfo(
            User(
                username = "username",
                avatar = Avatar(
                    "https://example.com/avatar.jpg",
                    "https://example.com/avatar.jpg",
                    "https://example.com/avatar.jpg",
                ),
                id = 1,
                nickname = "",
                sign = "Sign ".repeat(3),
                userGroup = UserGroup.User,
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