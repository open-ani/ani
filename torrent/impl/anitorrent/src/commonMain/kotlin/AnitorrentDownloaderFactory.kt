package me.him188.ani.app.torrent.anitorrent

import me.him188.ani.app.torrent.api.HttpFileDownloader
import me.him188.ani.app.torrent.api.TorrentDownloader
import me.him188.ani.app.torrent.api.TorrentDownloaderConfig
import me.him188.ani.app.torrent.api.TorrentDownloaderFactory
import me.him188.ani.app.torrent.api.TorrentLibraryLoader
import me.him188.ani.utils.io.SystemPath
import kotlin.coroutines.CoroutineContext

class AnitorrentDownloaderFactory : TorrentDownloaderFactory {
    override val name: String get() = "Anitorrent" // don't change

    override val libraryLoader: TorrentLibraryLoader get() = getAnitorrentTorrentLibraryLoader()

    override fun createDownloader(
        rootDataDirectory: SystemPath,
        httpFileDownloader: HttpFileDownloader,
        torrentDownloaderConfig: TorrentDownloaderConfig,
        parentCoroutineContext: CoroutineContext
    ): TorrentDownloader =
        createAnitorrentTorrentDownloader(
            rootDataDirectory,
            httpFileDownloader,
            torrentDownloaderConfig,
            parentCoroutineContext,
        )
}

internal expect fun getAnitorrentTorrentLibraryLoader(): TorrentLibraryLoader
