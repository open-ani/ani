package me.him188.ani.android

import kotlinx.coroutines.CoroutineScope
import me.him188.ani.android.navigation.AndroidBrowserNavigator
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.torrent.DefaultTorrentManager
import me.him188.ani.app.torrent.TorrentDownloader
import me.him188.ani.app.torrent.TorrentManager
import me.him188.ani.app.videoplayer.ExoPlayerControllerFactory
import me.him188.ani.app.videoplayer.PlayerControllerFactory
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
            downloaderFactory = {
                TorrentDownloader(
                    cacheDirectory = torrentCacheDir,
                )
            }
        )
    }
    single<PlayerControllerFactory> { ExoPlayerControllerFactory() }
}