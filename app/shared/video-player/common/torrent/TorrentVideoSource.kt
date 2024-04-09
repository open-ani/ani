package me.him188.ani.app.videoplayer.torrent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.app.tools.torrent.TorrentManager
import me.him188.ani.app.torrent.model.EncodedTorrentData
import me.him188.ani.app.videoplayer.data.VideoSource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface TorrentVideoSource : VideoSource<TorrentVideoData>

fun TorrentVideoSource(encodedTorrentData: EncodedTorrentData): TorrentVideoSource =
    TorrentVideoSourceImpl(encodedTorrentData)

private class TorrentVideoSourceImpl(
    private val encodedTorrentData: EncodedTorrentData,
) : TorrentVideoSource, KoinComponent {
    private val manager: TorrentManager by inject()

    @OptIn(ExperimentalStdlibApi::class)
    override val uri: String by lazy {
        "torrent://${encodedTorrentData.data.toHexString()}"
    }

    override suspend fun open(): TorrentVideoData {
        return TorrentVideoData(withContext(Dispatchers.IO) {
            manager.downloader.await().startDownload(encodedTorrentData)
        })
    }

    override fun toString(): String = "TorrentVideoSource(uri=$uri)"
}
