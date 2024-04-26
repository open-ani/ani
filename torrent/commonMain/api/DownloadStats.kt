package me.him188.ani.app.torrent.api

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/**
 * @see Libtorrent4jTorrentDownloader
 */
interface DownloadStats {
    /**
     * 总请求大小. 注意这不是在硬盘上的大小. 在硬盘上可能会略有差别.
     */
    val totalBytes: Flow<Long>

    val downloadedBytes: Flow<Long>

    /**
     * Bytes per second. `null` if not available, i.e. just started
     */
    val downloadRate: Flow<Long?>
    val uploadRate: Flow<Long?>

    /**
     * Range: `0..1`
     */
    val progress: Flow<Float>

    /**
     * 该文件是否已经全部下载完成.
     *
     * 注意, 对于总统计, 该属性永远为 false.
     */
    val isFinished: Flow<Boolean>

    val peerCount: Flow<Int>

    /**
     * Waits for all pieces to be downloaded
     *
     * @throws CancellationException if coroutine scope is cancelled
     */
    suspend fun awaitFinished()
}

@RequiresOptIn
annotation class ExperimentalTorrentApi

enum class PieceState {
    READY,
    DOWNLOADING,
    FINISHED,
    FAILED,
    NOT_AVAILABLE,
    ;
}