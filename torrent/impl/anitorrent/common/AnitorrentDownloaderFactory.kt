package me.him188.ani.app.torrent.anitorrent

import me.him188.ani.app.torrent.api.HttpFileDownloader
import me.him188.ani.app.torrent.api.TorrentDownloader
import me.him188.ani.app.torrent.api.TorrentDownloaderConfig
import me.him188.ani.app.torrent.api.TorrentDownloaderFactory
import me.him188.ani.app.torrent.api.TorrentLibraryLoader
import java.io.File
import kotlin.coroutines.CoroutineContext

class AnitorrentDownloaderFactory : TorrentDownloaderFactory {
    override val name: String get() = "Anitorrent" // don't change

    override val libraryLoader: TorrentLibraryLoader get() = AnitorrentLibraryLoader

    override fun createDownloader(
        rootDataDirectory: File,
        httpFileDownloader: HttpFileDownloader,
        torrentDownloaderConfig: TorrentDownloaderConfig,
        parentCoroutineContext: CoroutineContext
    ): TorrentDownloader =
        AnitorrentTorrentDownloader(
            rootDataDirectory,
            httpFileDownloader,
            torrentDownloaderConfig,
            parentCoroutineContext,
        )
}
