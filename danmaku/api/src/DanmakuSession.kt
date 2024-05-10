package me.him188.ani.danmaku.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.transformLatest
import me.him188.ani.danmaku.protocol.DanmakuInfo
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

interface DanmakuSession {
    val totalCount: Flow<Int?> get() = emptyFlow()

    /**
     * 创建一个随视频进度 [progress] 匹配到的弹幕数据流.
     *
     * 每当有一个新的 [Duration] 从 [progress] emit, 本函数返回的 flow 都会 emit 一些新的 [Danmaku] 对象, 表示在该视频进度 [Duration] 匹配到的弹幕列表.
     *
     * 当有新的 [Duration] 从 [progress] emit, 并且上一个时间点匹配到的弹幕列表还没被完全 collect 时, 将会抛弃上一个时间点匹配到的弹幕列表, 并且从新的时间点开始匹配.
     *
     * ### Flow 终止
     *
     * 当 [progress] [完结][Flow.onCompletion] 时, 本函数返回的 flow 也会 [完结][Flow.onCompletion].
     */
    fun at(progress: Flow<Duration>): Flow<Danmaku>
}

fun emptyDanmakuSession(): DanmakuSession {
    return object : DanmakuSession {
        override fun at(progress: Flow<Duration>): Flow<Danmaku> = emptyFlow()
    }
}

class TimeBasedDanmakuSession private constructor(
    /**
     * List of danmaku. Must be sorted by [DanmakuInfo.playTime], and must not change after construction.
     */
    private val list: List<Danmaku>,
    private val shiftMillis: Long = 0,
    private val samplePeriod: Duration = 30.milliseconds,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext,
) : DanmakuSession {
    override val totalCount: Flow<Int?> = flowOf(list.size)

    companion object {
        fun create(
            sequence: Sequence<Danmaku>,
            shiftMillis: Long = 0,
            samplePeriod: Duration = 30.milliseconds,
            coroutineContext: CoroutineContext = EmptyCoroutineContext,
        ): DanmakuSession {
            val list = sequence.toCollection(ArrayList())
            list.sortBy { it.playTimeMillis }
            return TimeBasedDanmakuSession(list, shiftMillis, samplePeriod, coroutineContext)
        }
    }

    override fun at(progress: Flow<Duration>): Flow<Danmaku> {
        if (list.isEmpty()) {
            return emptyFlow() // fast path
        }

        var lastTime: Duration = Duration.ZERO
        var lastIndex = -1// last index at which we accessed [list]
        return progress.map { it - shiftMillis.milliseconds }
            .let {
                if (samplePeriod == Duration.ZERO) it else it.sample(samplePeriod)
            }
            .transformLatest { curTime ->
                if (curTime < lastTime) {
                    // Went back, reset position to the correct one
                    lastIndex = list.indexOfFirst { it.playTimeMillis >= curTime.inWholeMilliseconds } - 1
                    if (lastIndex == -2) {
                        lastIndex = -1
                    }
                }

                lastTime = curTime

                val curTimeSecs = curTime.inWholeMilliseconds

                for (i in (lastIndex + 1)..list.lastIndex) {
                    val item = list[i]
                    if (curTimeSecs >= item.playTimeMillis // 达到了弹幕发送的时间
                        && curTimeSecs - item.playTimeMillis <= 3000 // 只发送三秒以内的, 否则会导致快进之后发送大量过期弹幕
                    ) {
                        lastIndex = i
                        emit(item) // Note: 可能会因为有新的 [curTime] 而 cancel
                    } else {
                        // not yet, 因为 list 是排序的, 这也说明后面的弹幕都还没到时间
                        break
                    }
                }
            }
            .flowOn(coroutineContext)
    }
}