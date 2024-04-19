package me.him188.ani.utils.coroutines.flows

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 创建一个 flow, 当 [durationMillis] 时间内没有新的元素时, 会调用 [reset] 方法.
 */
fun <T> Flow<T>.resetStale(
    durationMillis: Long,
    reset: suspend FlowCollector<T>.() -> Unit,
): Flow<T> {
    val upstream = this
    return channelFlow {
        val collector: FlowCollector<T> = FlowCollector { value -> send(value) }
        coroutineScope {
            val time = object {
                @Volatile
                var value: Long = 0L
            }
            launch {
                while (isActive) {
                    delay(durationMillis)
                    val now = System.currentTimeMillis()
                    if (now - time.value >= durationMillis) {
                        time.value = Long.MAX_VALUE
                        reset(collector)
                    }
                }
            }
            upstream.collect {
                time.value = System.currentTimeMillis()
                send(it)
            }
        }
    }
}