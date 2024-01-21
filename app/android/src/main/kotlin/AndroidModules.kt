package me.him188.ani.android

import me.him188.ani.android.navigation.AndroidBrowserNavigator
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.torrent.TorrentDownloader
import me.him188.ani.app.torrent.TorrentDownloaderFactory
import me.him188.ani.app.videoplayer.ExoPlayerControllerFactory
import me.him188.ani.app.videoplayer.PlayerControllerFactory
import org.koin.dsl.module
import java.io.File

fun getAndroidModules(
    torrentCacheDir: File,
) = module {
    single<BrowserNavigator> { AndroidBrowserNavigator() }
    single<TorrentDownloaderFactory> {
        TorrentDownloaderFactory {
            TorrentDownloader(
                cacheDirectory = torrentCacheDir,
            )
        }
    }
    single<PlayerControllerFactory> { ExoPlayerControllerFactory() }
}