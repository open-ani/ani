package me.him188.ani.app.torrent.api

import me.him188.ani.utils.io.SystemPath
import kotlin.coroutines.CoroutineContext

interface TorrentLibraryLoader {
    fun loadLibraries()
}

interface TorrentDownloaderFactory { // SPI
    val name: String

    val libraryLoader: TorrentLibraryLoader

    fun createDownloader(
        rootDataDirectory: SystemPath,
        httpFileDownloader: HttpFileDownloader,
        torrentDownloaderConfig: TorrentDownloaderConfig,
        parentCoroutineContext: CoroutineContext,
    ): TorrentDownloader
}
