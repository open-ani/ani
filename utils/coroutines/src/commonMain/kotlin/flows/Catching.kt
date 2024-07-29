package me.him188.ani.utils.coroutines.flows

import kotlinx.coroutines.flow.FlowCollector
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

suspend inline fun <T, R> FlowCollector<List<T>>.runOrEmitEmptyList(block: () -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    try {
        return block()
    } catch (e: Throwable) {
        emit(emptyList())
        throw e
    }
}
