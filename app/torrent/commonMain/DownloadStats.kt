package me.him188.ani.app.torrent

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.torrent.model.Piece

/**
 * @see TorrentDownloader
 */
public interface DownloadStats {
    public val totalBytes: Flow<Long>
    public val downloadedBytes: Flow<Long>
    public val downloadRate: Flow<Long> // bytes per second

    /**
     * Range: `0..1`
     */
    public val progress: Flow<Float>

    public val isFinished: Flow<Boolean>

    public val peerCount: Flow<Int>

    @ExperimentalTorrentApi
    public val pieces: List<Piece>

    /**
     * Waits for all pieces to be downloaded
     *
     * @throws CancellationException if coroutine scope is cancelled
     */
    public suspend fun awaitFinished()
}

@RequiresOptIn
public annotation class ExperimentalTorrentApi

public enum class PieceState {
    READY,
    DOWNLOADING,
    FINISHED,
    FAILED,
    NOT_AVAILABLE,
    ;
}