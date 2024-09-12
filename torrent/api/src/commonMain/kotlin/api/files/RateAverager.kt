package me.him188.ani.app.torrent.api.files

import kotlinx.atomicfu.AtomicLongArray
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.selects.select
import me.him188.ani.utils.platform.annotations.TestOnly
import kotlin.concurrent.Volatile
import kotlin.time.Duration.Companion.seconds

/**
 * 平均速度计算工具.
 *
 * [bytes] 提供下载总量数据, [ticker] 用于同步计时 (每秒一次).
 * 该工具本身不会自动输出值. 需要外部使用一个协程来调用 [runPass] 方法获取该 pass 的平均速度.
 *
 * @param bytes 下载总量数据源. [bytes] 每推送一个值, 都会临时缓存到 [latestValue] 并覆盖之前的缓存. 等待下一次 [ticker] 推送时, [latestValue] 才会被推送到 [window] 里.
 * @param ticker 计时器. 需要每秒推送一个值.
 * @param windowSize 窗口大小. 默认为 5. 即计算历史 5 秒的数据.
 */
class RateAverager(
    private val bytes: ReceiveChannel<Long>,
    private val ticker: ReceiveChannel<Unit>,
    windowSize: Int = 5,
) {
    init {
        require(windowSize > 0) { "windowSize must be greater than 0" }
    }

    /**
     * 用于缓存
     */
    private val window = AtomicLongArray(windowSize) // atomic to ensure visibility

    @TestOnly
    fun getWindowContent(): LongArray {
        return LongArray(window.size) { window[it].value }
    }

    /**
     * 窗口内最新的值的 index
     */
    @Volatile
    var currentIndex = -1

    /**
     * 窗口内已经接受了多少个值. 必须要是 uint, 因为依赖它 round 为 0 的性质.
     */
    @Volatile
    var counted = 0u

    /**
     * 从 [bytes] 接收到的最新的值, 在下一秒由 [ticker] 更新到 [window] 里.
     *
     * `-1` 表示没有值.
     */
    @Volatile
    var latestValue = -1L

    /**
     * 连续多少次没有收到 [bytes] 的值. 用于判断是否需要忘掉最久远的值.
     */
    @Volatile
    var missingValuePassCount = 0

    /**
     * 执行一轮计算. 每接收 [bytes] 和收到 [ticker] 都算作一轮.
     *
     * 函数将会一直挂起, 直到有 [bytes] 或 [ticker] 推送.
     *
     * `while (true) { runPass()?.let { emit(it) } }` 可以用于将其转换为一个 Flow.
     *
     * @return 这一轮的平均速度. 如果此轮是 [bytes] 更新, 则返回 `null` (平均速度未更新).
     */
    suspend fun runPass(): Long? = select {
        bytes.onReceive {
            latestValue = it
            missingValuePassCount = 0
            null
        }
        // 每秒计算平均变化速度
        ticker.onReceive {
            val latestValue = latestValue
            if (latestValue != -1L) {
                // 如果有最新的下载总量, 则将其推入窗口
                pushValueToWindow(latestValue)
                this@RateAverager.latestValue = -1L
            } else {
                if (currentIndex == -1) {
                    // 还没收到过任何一个值
                    return@onReceive 0
                }

                // 如果没有, 就把窗口内 (历史) 最新的值推入, 这样就能"忘掉"最久远的那个值
                missingValuePassCount += 1
                if (missingValuePassCount >= 2) {
                    // 仅在连续两次都没有收到新值时才推送, 因为 bytes 也有可能是每秒才更新一次
                    val value = window[currentIndex].value
                    pushValueToWindow(value)
                }
            }
            check(currentIndex != -1)

            if (counted == 1u) {
                return@onReceive 0
            }

            // RateAverager.kt:113: me.him188.ani.app.torrent.api.files.RateAverager$runPass$2$2::invokeSuspend: Unsupported branching/control within atomic operation
            val windowSize = window.size
            val toCompareIndex = if (counted < windowSize.toUInt()) {
                0
            } else {
                (currentIndex + 1) % windowSize
            }
            
            if (counted == 0u) {
                0
            } else {
                val current = window[currentIndex].value
                val toCompare = window[toCompareIndex].value
                (current - toCompare).coerceAtLeast(0L) / counted.coerceAtMost(windowSize.toUInt())
                    .toLong()
            }
        }
    }

    private fun pushValueToWindow(it: Long) {
        currentIndex = (currentIndex + 1) % window.size
        window[currentIndex].value = it
        counted++
    }
}

/**
 * 创建一个计算 [this] 的平均变化速度的 [Flow]. 注意, 这不是计算平均值.
 *
 * 返回的 flow 一定会每秒 emit 一次. 如果此时还没有 [this@create] 推送, 则 emit `0L`.
 *
 * @param windowSize 窗口大小. 默认为 5. 即计算历史 5 秒的数据.
 * @param tickerFlow 计时器, 需要实现为每秒 emit 一次, 否则行为未定义
 * @return 平均速度 flow.
 */
fun Flow<Long>.averageRate(
    windowSize: Int = 5,
    tickerFlow: Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(1.seconds)
        }
    },
): Flow<Long> = flow {
    coroutineScope {
        RateAverager(produceIn(this), tickerFlow.produceIn(this), windowSize).run {
            while (true) {
                runPass()?.let {
                    emit(it)
                }
            }
        }
    }
}
