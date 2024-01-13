package me.him188.ani.app.ui.profile

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.ui.foundation.launchInBackground

@Composable
internal actual fun ColumnScope.PlatformDebugInfoItems(viewModel: AccountViewModel, snackbar: SnackbarHostState) {
    val context = LocalContext.current
    Button({
        context.applicationContext.cacheDir.resolve("torrent-caches").deleteRecursively()
        viewModel.launchInBackground {
            snackbar.showSnackbar("Cache cleared")
        }
    }) {
        Text("Clear cache")
    }
}