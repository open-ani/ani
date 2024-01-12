package me.him188.ani.app.torrent

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

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

    public val pieces: StateFlow<MutableList<PieceState>> // array will change

    /**
     * @throws CancellationException if coroutine scope is cancelled
     */
    public suspend fun awaitFinished()
}

public enum class PieceState {
    READY,
    DOWNLOADING,
    FINISHED,
    FAILED,
    NOT_AVAILABLE,
    ;
}