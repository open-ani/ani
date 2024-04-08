package me.him188.ani.app.videoplayer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.app.tools.torrent.TorrentManager
import me.him188.ani.app.torrent.TorrentDownloadSession
import me.him188.ani.app.torrent.model.EncodedTorrentData
import me.him188.ani.app.videoplayer.torrent.TorrentVideoData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A streamable video source.
 *
 * [VideoSource]s are stateless: They only represent a location of the resource, not holding file descriptors or network connections, etc.
 *
 * [VideoSource] can be a local file, or a remote resource e.g., a torrent.
 *
 * @param S the type of the stream. For example, a torrent video source would be [TorrentDownloadSession].
 */
sealed interface VideoSource<S : VideoData> {
    val uri: String

    /**
     * Opens the underlying video data.
     *
     * Note that [S] should be closed by the caller.
     *
     * Repeat calls to this function may return different instances so it may be desirable to store the result.
     */
    suspend fun open(): S
}

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