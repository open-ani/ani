package me.him188.ani.app.torrent.api.files

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.selects.select

/**
 * 5 秒内的平均速度
 */
class RateAverager(
    private val bytes: ReceiveChannel<Long>,
    private val ticker: ReceiveChannel<Unit>,
    windowSize: Int = 5,
) {
    init {
        require(windowSize > 0) { "windowSize must be greater than 0" }
    }

    val window = LongArray(windowSize)
    var currentIndex = -1
    var counted = 0u

    suspend fun runPass(): Long? {
        return select {
            bytes.onReceive {
                currentIndex = (currentIndex + 1) % window.size
                window[currentIndex] = it
                counted++
                null
            }
            // 每秒计算平均变化速度
            ticker.onReceive {
                if (currentIndex == -1) {
                    return@onReceive 0
                }

                val current = window[currentIndex]
                if (counted == 1u) {
                    return@onReceive current
                }

                val toCompare = window[
                    if (counted < window.size.toUInt()) {
                        currentIndex - 1
                    } else {
                        (currentIndex + 1) % window.size
                    },
                ]
                if (counted == 0u) {
                    0
                } else {
                    (current - toCompare).coerceAtLeast(0L) / counted.coerceAtMost(window.size.toUInt())
                        .toLong()
                }
            }
        }
    }

    companion object {
        fun create(
            downloadedBytes: Flow<Long>,
            tickerFlow: Flow<Unit> = flow {
                while (true) {
                    delay(1000)
                    emit(Unit)
                }
            },
        ): Flow<Long> = flow {
            coroutineScope {
                RateAverager(downloadedBytes.produceIn(this), tickerFlow.produceIn(this)).run {
                    while (true) {
                        runPass()?.let {
                            emit(it)
                        }
                    }
                }
            }
        }
    }
}