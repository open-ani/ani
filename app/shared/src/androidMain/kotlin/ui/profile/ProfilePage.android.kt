package me.him188.ani.app.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.app.data.source.session.SessionStatus
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.episode.details.rememberTestAuthState

@Preview
@Composable
internal fun PreviewSelfInfo() {
    ProvideCompositionLocalsForPreview {
        SelfInfo(
            rememberTestAuthState(
                SessionStatus.Verified(
                    "",
                    UserInfo(
                        username = "username",
                        avatarUrl = "https://example.com/avatar.jpg",
                        id = 1,
                        nickname = "",
                        sign = "Sign ".repeat(3),
                    ),
                ),
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