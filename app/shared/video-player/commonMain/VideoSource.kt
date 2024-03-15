package me.him188.ani.app.videoplayer

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import me.him188.ani.app.torrent.TorrentDownloadSession
import me.him188.ani.app.torrent.TorrentDownloaderManager
import me.him188.ani.app.torrent.model.EncodedTorrentData
import me.him188.ani.utils.coroutines.mapAutoClose
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A streamable video source.
 *
 * This can be a local file, or a remote resource, for example, a torrent.
 *
 * @param S the type of the stream. For example, a torrent video source would be [TorrentDownloadSession].
 */
sealed interface VideoSource<S> {
    val uri: String

    /**
     * Start streaming this video.
     *
     * Returns a flow of the video stream. The flow means that the stream may restart.
     * This may be caused by switching network so that the stream needs to be reconnected.
     *
     * It is guaranteed that when the flow emits a new [S], the old [S] being replaced must be closed if it has acquired any resource.
     */
    suspend fun startStreaming(): Flow<S>
}

interface TorrentVideoSource : VideoSource<TorrentDownloadSession> {
    /**
     * Current session. The flow is active only if [startStreaming] is called and the returned flow is being collected.
     */
    val session: SharedFlow<TorrentDownloadSession>
}

fun TorrentVideoSource(encodedTorrentData: EncodedTorrentData): TorrentVideoSource =
    TorrentVideoSourceImpl(encodedTorrentData)

private class TorrentVideoSourceImpl(
    private val encodedTorrentData: EncodedTorrentData,
) : TorrentVideoSource, KoinComponent {
    private val factory: TorrentDownloaderManager by inject()

    @OptIn(ExperimentalStdlibApi::class)
    override val uri: String by lazy {
        "torrent://${encodedTorrentData.data.toHexString()}"
    }

    override val session: MutableSharedFlow<TorrentDownloadSession> =
        MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override suspend fun startStreaming(): Flow<TorrentDownloadSession> {
        return factory.torrentDownloader.mapAutoClose { torrentDownloader ->
            torrentDownloader.startDownload(encodedTorrentData).also {
                session.tryEmit(it)
            }
        }
    }

    override fun toString(): String = "TorrentVideoSource(uri=$uri)"
}