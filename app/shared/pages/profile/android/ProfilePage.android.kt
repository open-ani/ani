package me.him188.ani.app.ui.profile

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.launchInBackground
import org.openapitools.client.models.Avatar
import org.openapitools.client.models.User
import org.openapitools.client.models.UserGroup

@Composable
internal actual fun ColumnScope.PlatformDebugInfoItems(viewModel: AccountViewModel, snackbar: SnackbarHostState) {
    val context = LocalContext.current
    FilledTonalButton({
        context.applicationContext.cacheDir.resolve("torrent-caches").deleteRecursively()
        viewModel.launchInBackground {
            snackbar.showSnackbar("Cache cleared")
        }
    }) {
        Text("清除全部下载缓存")
    }
}

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
                    "https://example.com/avatar.jpg"
                ),
                id = 1,
                nickname = "",
                sign = "Sign ".repeat(3),
                userGroup = UserGroup.User,
            ),
            true,
            {},
        )
    }
}

@Preview
@Composable
private fun PreviewProfilePage() {
    ProvideCompositionLocalsForPreview {
        ProfilePage(
            onClickSettings = {}
        )
    }
}