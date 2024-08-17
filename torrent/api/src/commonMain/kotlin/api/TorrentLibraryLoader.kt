package me.him188.ani.app.torrent.api

import me.him188.ani.utils.io.SystemPath
import kotlin.coroutines.CoroutineContext

interface TorrentLibraryLoader {
    fun loadLibraries()
}

interface TorrentDownloaderFactory { // SPI
    val name: String

    val libraryLoader: TorrentLibraryLoader

    /**
     * 创建一个 [TorrentDownloader]. 当 [TorrentDownloader] 被关闭时, [httpFileDownloader] 也会被关闭.
     */
    fun createDownloader(
        rootDataDirectory: SystemPath,
        httpFileDownloader: HttpFileDownloader,
        torrentDownloaderConfig: TorrentDownloaderConfig,
        parentCoroutineContext: CoroutineContext,
    ): TorrentDownloader
}
