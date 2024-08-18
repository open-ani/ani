package me.him188.ani.app.torrent.api.files

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.him188.ani.utils.coroutines.sampleWithInitial

abstract class DownloadStats {
    /**
     * 总请求大小. 注意这不是在硬盘上的大小. 在硬盘上可能会略有差别.
     */
    abstract val totalSize: Flow<Long>

    open val downloadedBytes: Flow<Long>
        get() = combine(totalSize, progress) { total, pro -> (total * pro).toLong() }

    /**
     * Bytes per second. `null` if not available, i.e. just started
     */
    open val downloadRate: Flow<Long>
        get() = RateAverager.create(
            downloadedBytes.sampleWithInitial(1000),
        )

    abstract val uploadRate: Flow<Long>

    /**
     * Range: `0..1`
     */
    abstract val progress: Flow<Float>

    /**
     * 该文件是否已经全部下载完成.
     */
    open val isFinished: Flow<Boolean>
        get() = progress.map { it >= 1f }

    /**
     * Waits for all pieces to be downloaded
     *
     * @throws CancellationException if coroutine scope is cancelled
     */
    open suspend fun awaitFinished() {
        isFinished.filter { it }.first()
    }
}

@RequiresOptIn
annotation class ExperimentalTorrentApi

enum class PieceState {
    READY,
    DOWNLOADING,
    FINISHED,
    NOT_AVAILABLE,
    ;
}