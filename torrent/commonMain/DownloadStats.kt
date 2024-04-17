package me.him188.ani.app.torrent

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * @see TorrentDownloader
 */
public interface DownloadStats {
    public val totalBytes: Flow<Long>
    public val downloadedBytes: Flow<Long>

    /**
     * Bytes per second. `null` if not available, i.e. just started
     */
    public val downloadRate: StateFlow<Long?>
    public val uploadRate: StateFlow<Long?>

    /**
     * Range: `0..1`
     */
    public val progress: StateFlow<Float>

    public val isFinished: Flow<Boolean>

    public val peerCount: Flow<Int>

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