package me.him188.ani.app.ui.preference.tabs

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.him188.ani.app.platform.LocalContext


@Composable
internal actual fun ColumnScope.PlatformDebugInfoItems() {
    val context = LocalContext.current
    FilledTonalButton({
        context.applicationContext.cacheDir.resolve("torrent-caches").deleteRecursively()
    }) {
        Text("清除全部下载缓存")
    }
}
