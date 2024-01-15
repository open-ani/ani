package me.him188.ani.app.torrent

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import me.him188.ani.app.torrent.model.Piece

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

    public val pieces: List<Piece>

    /**
     * 最大可播放到的字节数. 顺序播放视频时使用. 在即将达到这个字节时, 需暂停并等待缓冲.
     *
     * 该 [SharedFlow] 至少有一个 replay. [Flow.collect] 时一定能拿到当前最新的值.
     */
    public val playableByteIndexExclusive: StateFlow<Long>

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