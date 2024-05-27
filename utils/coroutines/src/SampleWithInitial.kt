package me.him188.ani.utils.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.selects.select
import java.util.concurrent.CancellationException


/**
 * @see sample
 */
fun <T> Flow<T>.sampleWithInitial(periodMillis: Long): Flow<T> {
    require(periodMillis > 0) { "Sample period should be positive" }
    return scopedFlow { downstream ->
        val values = produce(capacity = Channel.CONFLATED) {
            collect { value -> send(value ?: NULL) }
        }
        var initialValueEmitted = false
        var lastValue: Any? = null
        val ticker = fixedPeriodTicker(periodMillis)
        while (lastValue !== DONE) {
            select<Unit> {
                values.onReceiveCatching { result ->
                    result
                        .onSuccess {
                            if (!initialValueEmitted) {
                                initialValueEmitted = true
                                downstream.emit(NULL.unbox(it))
                            } else {
                                lastValue = it
                            }
                        }
                        .onFailure {
                            it?.let { throw it }
                            ticker.cancel(CancellationException())
                            lastValue = DONE
                        }
                }

                ticker.onReceive {
                    val value = lastValue ?: return@onReceive
                    lastValue = null // Consume the value
                    downstream.emit(NULL.unbox(value))
                }
            }
        }
    }
}

internal fun <R> scopedFlow(@BuilderInference block: suspend CoroutineScope.(FlowCollector<R>) -> Unit): Flow<R> =
    flow {
        coroutineScope {
            block(this@flow)
        }
    }

@JvmField
internal val NULL = Symbol("NULL")

/**
 * Symbol to indicate that the value is not yet initialized.
 * It should never leak to the outside world.
 */
@JvmField
internal val UNINITIALIZED = Symbol("UNINITIALIZED")

/*
 * Symbol used to indicate that the flow is complete.
 * It should never leak to the outside world.
 */
@JvmField
internal val DONE = Symbol("DONE")

class Symbol(@JvmField val symbol: String) {
    override fun toString(): String = "<$symbol>"

    @Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
    inline fun <T> unbox(value: Any?): T = if (value === this) null as T else value as T
}

internal fun CoroutineScope.fixedPeriodTicker(
    delayMillis: Long,
): ReceiveChannel<Unit> {
    return produce(capacity = 0) {
        delay(delayMillis)
        while (true) {
            channel.send(Unit)
            delay(delayMillis)
        }
    }
}
