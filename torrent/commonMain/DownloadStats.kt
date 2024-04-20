package me.him188.ani.app.torrent

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/**
 * @see TorrentDownloader
 */
public interface DownloadStats {
    /**
     * 总请求大小. 注意这不是在硬盘上的大小. 在硬盘上可能会略有差别.
     */
    public val totalBytes: Flow<Long>

    public val downloadedBytes: Flow<Long>

    /**
     * Bytes per second. `null` if not available, i.e. just started
     */
    public val downloadRate: Flow<Long?>
    public val uploadRate: Flow<Long?>

    /**
     * Range: `0..1`
     */
    public val progress: Flow<Float>

    /**
     * 该文件是否已经全部下载完成.
     *
     * 注意, 对于总统计, 该属性永远为 false.
     */
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