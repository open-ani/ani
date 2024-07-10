package me.him188.ani.app.torrent.api

import java.io.File
import kotlin.coroutines.CoroutineContext

interface TorrentLibraryLoader {
    fun loadLibraries()
}

interface TorrentDownloaderFactory { // SPI
    val name: String

    val libraryLoader: TorrentLibraryLoader

    fun createDownloader(
        rootDataDirectory: File,
        httpFileDownloader: HttpFileDownloader,
        torrentDownloaderConfig: TorrentDownloaderConfig,
        parentCoroutineContext: CoroutineContext,
    ): TorrentDownloader
}
