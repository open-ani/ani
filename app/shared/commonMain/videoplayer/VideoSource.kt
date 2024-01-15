package me.him188.ani.app.videoplayer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.him188.ani.app.torrent.TorrentDownloadSession
import me.him188.ani.app.torrent.TorrentDownloaderManager
import me.him188.ani.app.torrent.model.EncodedTorrentData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Represents a streamable video source.
 * @param S the type of the stream. For example, a torrent video source would be [TorrentDownloadSession].
 */
sealed interface VideoSource<S> {
    val uri: String

    /**
     * Start streaming this video.
     *
     * Returns a flow of the video stream. The flow means that the stream may restart.
     * This may be caused by switching network so that the stream needs to be reconnected.
     */
    suspend fun startStreaming(): Flow<S>
}

class TorrentVideoSource(
    private val encodedTorrentData: EncodedTorrentData,
) : VideoSource<TorrentDownloadSession>, KoinComponent {
    private val factory: TorrentDownloaderManager by inject()

    @OptIn(ExperimentalStdlibApi::class)
    override val uri: String by lazy {
        "torrent://${encodedTorrentData.data.toHexString()}"
    }

    override suspend fun startStreaming(): Flow<TorrentDownloadSession> {
        return factory.torrentDownloader.map {
            it.startDownload(encodedTorrentData)
        }
    }

    override fun toString(): String {
        return "TorrentVideoSource(uri=$uri)"
    }
}