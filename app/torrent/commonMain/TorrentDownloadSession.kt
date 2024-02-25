package me.him188.ani.app.torrent

import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.torrent.download.TorrentDownloadController
import me.him188.ani.app.torrent.file.SeekableInput

/**
 * Represents a torrent download session.
 *
 * Needs to be closed.
 *
 * @See TorrentDownloader
 */
public interface TorrentDownloadSession : DownloadStats, AutoCloseable {
    public val torrentDownloadController: Flow<TorrentDownloadController>

    public val state: Flow<TorrentDownloadState>

    /**
     * Opens the downloaded file as a [SeekableInput].
     */
    public suspend fun createInput(): SeekableInput
}

public sealed class TorrentDownloadState {
    /**
     * The session is ready and awaiting for the torrent to be added.
     */
    public data object Starting : TorrentDownloadState()

    /**
     * The torrent is being fetched from the network.
     *
     * Piece information may not be available yet.
     */
    public data object FetchingMetadata : TorrentDownloadState()

    /**
     * Pieces are being downloaded.
     */
    public data object Downloading : TorrentDownloadState()

    /**
     * All pieces have been downloaded successfully.
     */
    public data object Finished : TorrentDownloadState()
}

