package me.him188.ani.app.torrent.api.files

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.selectUnbiased

abstract class DownloadStats {
    /**
     * 总请求大小. 注意这不是在硬盘上的大小. 在硬盘上可能会略有差别.
     */
    abstract val totalBytes: Flow<Long>

    open val downloadedBytes: Flow<Long>
        get() = combine(totalBytes, progress) { total, pro -> (total * pro).toLong() }

    /**
     * Bytes per second. `null` if not available, i.e. just started
     */
    open val downloadRate: Flow<Long?>
        get() = flow {
            coroutineScope {
                val window = arrayOf(0L, 0L, 0L, 0L, 0L)
                var index = 0
                var counted = 0
                // 每秒记录一个值
                val bytes = downloadedBytes.produceIn(this)
                val ticker = flow {
                    while (true) {
                        delay(1000)
                        emit(Unit)
                    }
                }.produceIn(this)

                while (isActive) {
                    selectUnbiased {
                        bytes.onReceive {
                            index = (index + 1) % window.size
                            window[index] = it
                            counted++
                        }
                        // 每秒计算平均变化速度
                        ticker.onReceive {
                            val current = window[index]
                            val last = window[(index + 1) % window.size] // 下一个 index 也就是距今最远的一个
                            if (counted == 0) {
                                emit(0)
                            } else {
                                emit((current - last).coerceAtLeast(0) / counted.coerceAtMost(window.size))
                            }
                        }
                    }
                }
            }
        }

    abstract val uploadRate: Flow<Long?>

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
    FAILED,
    NOT_AVAILABLE,
    ;
}