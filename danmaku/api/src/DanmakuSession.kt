package me.him188.ani.danmaku.api

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import me.him188.ani.danmaku.protocol.DanmakuInfo
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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
    fun at(progress: Flow<Duration>): Flow<DanmakuEvent>
}

sealed class DanmakuEvent {
    /**
     * 发送一个新弹幕
     */
    class Add(val danmaku: Danmaku) : DanmakuEvent()

    /**
     * 清空屏幕并以这些弹幕填充. 常见于快进/快退时
     */
    class Repopulate(val list: List<Danmaku>) : DanmakuEvent()
}

fun emptyDanmakuSession(): DanmakuSession {
    return object : DanmakuSession {
        override fun at(progress: Flow<Duration>): Flow<DanmakuEvent> = emptyFlow()
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
            val list = sequence.mapTo(ArrayList()) {
                DanmakuSanitizer.sanitize(it)
            }
            list.sortBy { it.playTimeMillis }
            return TimeBasedDanmakuSession(list, shiftMillis, samplePeriod, coroutineContext)
        }
    }

    override fun at(progress: Flow<Duration>): Flow<DanmakuEvent> {
        if (list.isEmpty()) {
            return emptyFlow() // fast path
        }
        // 弹幕算法总览:

        val state = DanmakuSessionFlowState(
            list,
            repopulateThreshold = 3.seconds,
            repopulateDistance = { 20.seconds },
        )
        val algorithm = DanmakuSessionAlgorithm(state)
        return channelFlow {
            // 一个单独协程收集当前进度
            launch(coroutineContext, start = CoroutineStart.UNDISPATCHED) {
                progress.collect {
                    state.curTimeShared = it
                }
                // progress finished, no need to calculate
                this@channelFlow.channel.close()
            }

            val sendItem: (DanmakuEvent) -> Boolean = {
                trySend(it).isSuccess
            }

            while (true) {
                algorithm.tick(sendItem)
                delay(1000 / 20) // always check for cancellation
            }
        }

        // 下面的算法有 bug, 而且会创建大量协程影响性能

//        var lastTime: Duration = Duration.ZERO
//        var lastIndex = -1// last index at which we accessed [list]
//        return progress.map { it - shiftMillis.milliseconds }
//            .let {
//                if (samplePeriod == Duration.ZERO) it else it.sample(samplePeriod)
//            }
//            .transformLatest { curTime ->
//                if (curTime < lastTime) {
//                    // Went back, reset position to the correct one
//                    lastIndex = list.indexOfFirst { it.playTimeMillis >= curTime.inWholeMilliseconds } - 1
//                    if (lastIndex == -2) {
//                        lastIndex = -1
//                    }
//                }
//
//                lastTime = curTime
//
//                val curTimeSecs = curTime.inWholeMilliseconds
//
//                for (i in (lastIndex + 1)..list.lastIndex) {
//                    val item = list[i]
//                    if (curTimeSecs >= item.playTimeMillis // 达到了弹幕发送的时间
//                    ) {
//                        if (curTimeSecs - item.playTimeMillis > 3000) {
//                            // 只发送三秒以内的, 否则会导致快进之后发送大量过期弹幕
//                            continue
//                        }
//                        lastIndex = i
//                        emit(item) // Note: 可能会因为有新的 [curTime] 而 cancel
//                    } else {
//                        // not yet, 因为 list 是排序的, 这也说明后面的弹幕都还没到时间
//                        break
//                    }
//                }
//            }
//            .flowOn(coroutineContext)
    }
}

internal class DanmakuSessionFlowState(
    val list: List<Danmaku>,
    /**
     * 每当快进/快退超过这个阈值后, 重新装填整个屏幕弹幕
     */
    val repopulateThreshold: Duration = 3.seconds,
    /**
     * 重新装填屏幕弹幕时, 从当前时间开始往旧重新装填的距离. 例如当前时间为 15s, repopulateDistance 为 3s, 则会装填 12-15s 的弹幕
     * 需要根据屏幕宽度, 弹幕密度, 以及弹幕速度计算
     */
    val repopulateDistance: () -> Duration,
    /**
     * 重新装填屏幕弹幕时, 最多装填的弹幕数量
     */
    val repopulateMaxCount: Int = 40,
) {
    var lastTime: Duration = Duration.INFINITE

    /**
     * 当前视频播放进度
     */
    @Volatile
    var curTimeShared: Duration = Duration.INFINITE

    /**
     * 最后成功发送了的弹幕的索引
     */
    @JvmField
    var lastIndex = -1
}


/**
 * 弹幕装填算法的具体实现
 */ // see DanmakuSessionAlgorithmTest
internal class DanmakuSessionAlgorithm(
    val state: DanmakuSessionFlowState,
) {
    /**
     * 对于每一个时间已经到达的弹幕
     */
    private inline fun foreachDanmaku(block: (Danmaku) -> Unit) {
        var i = state.lastIndex + 1
        val list = state.list
        try {
            while (i <= list.lastIndex) {
                block(list[i])
                i++
            }
            // 都发送成功了, 说明我们到了最后
        } finally {
            state.lastIndex = i - 1
        }
    }

    fun tick(sendEvent: (DanmakuEvent) -> Boolean) {
        val curTime = state.curTimeShared
        if (curTime == Duration.INFINITE) {
            return
        }
        val list = state.list

        try {
            if (state.lastTime == Duration.INFINITE // 第一帧
                || (curTime - state.lastTime).absoluteValue >= state.repopulateThreshold
            ) {
                // 移动太远, 重新装填屏幕弹幕
                // 初次播放如果进度不是在 0 也会触发这个

                val targetTime = (curTime - state.repopulateDistance()).inWholeMilliseconds

                //   1   2   3   4   5   6   7   8   9   10
                //     ^             ^ 
                //  curTime      lastTime

                // 跳到 repopulateDistance 时间前的一个
                state.lastIndex = list
                    .binarySearchBy(targetTime, selector = { it.playTimeMillis })
                    .let {
                        if (it >= 0) {
                            if (list[it].playTimeMillis < targetTime) {
                                it + 1
                            } else it
                        } else -(it + 1) - 1
                    }
                    .coerceAtLeast(-1)

                // Now:

                // 
                // repopulateDistance = 3
                // 
                // 
                // lastIndex
                //   v
                //   1   2   3   4   5   6   7   8   9   10
                //     ^             ^ 
                //  curTime      lastTime

                // 发送所有在 repopulateDistance 时间内的弹幕
                val curTimeMillis = curTime.inWholeMilliseconds
                sendEvent(DanmakuEvent.Repopulate(buildList {
                    foreachDanmaku { item ->
                        if (curTimeMillis < item.playTimeMillis) {
                            // 还没有达到弹幕发送时间, 因为 list 是排序的, 这也说明后面的弹幕都还没到时间
                            return@buildList
                        }
                        if (size >= state.repopulateMaxCount) {
                            return@buildList
                        }
                        add(item)
                    }
                }))
                return
            }
        } finally { // 总是更新
            state.lastTime = curTime
        }

        val curTimeMillis = curTime.inWholeMilliseconds

        foreachDanmaku { item ->
            if (curTimeMillis < item.playTimeMillis) {
                // 还没有达到弹幕发送时间, 因为 list 是排序的, 这也说明后面的弹幕都还没到时间
                return
            }
            if (!sendEvent(DanmakuEvent.Add(item))) {
                return // 发送失败, 意味着 channel 满了, 即 flow collector 满了, 下一逻辑帧再尝试
            }
        }
    }

}