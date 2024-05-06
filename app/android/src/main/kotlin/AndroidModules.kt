package me.him188.ani.android

import kotlinx.coroutines.CoroutineScope
import me.him188.ani.android.navigation.AndroidBrowserNavigator
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.tools.torrent.DefaultTorrentManager
import me.him188.ani.app.tools.torrent.TorrentManager
import me.him188.ani.app.videoplayer.ExoPlayerStateFactory
import me.him188.ani.app.videoplayer.ui.state.PlayerStateFactory
import org.koin.dsl.module
import java.io.File

fun getAndroidModules(
    torrentCacheDir: File,
    coroutineScope: CoroutineScope,
) = module {
    single<BrowserNavigator> { AndroidBrowserNavigator() }
    single<TorrentManager> {
        DefaultTorrentManager(
            coroutineScope.coroutineContext,
            saveDir = { torrentCacheDir },
        )
    }
    single<PlayerStateFactory> { ExoPlayerStateFactory() }
}