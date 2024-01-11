package me.him188.ani.utils.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

fun combineOr(vararg flows: Flow<Boolean>): Flow<Boolean> = combine(*flows) { flows -> flows.any { it } }

fun <T, R> Flow<T>.runningFoldNoInitialEmit(
    initial: R,
    @BuilderInference operation: suspend (accumulator: R, value: T) -> R
): Flow<R> = flow {
    var accumulator: R = initial
    collect { value ->
        accumulator = operation(accumulator, value)
        emit(accumulator)
    }
}
