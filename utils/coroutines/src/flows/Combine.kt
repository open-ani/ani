package me.him188.ani.utils.coroutines.flows

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector


inline fun <reified T, R> Iterable<Flow<T>>.combine(
    crossinline transform: suspend (Array<T>) -> R
): Flow<R> = kotlinx.coroutines.flow.combine(this, transform = transform)

inline fun <reified T, R> Iterable<Flow<T>>.combineTransform(
    @BuilderInference crossinline transform: suspend FlowCollector<R>.(Array<T>) -> Unit
): Flow<R> = kotlinx.coroutines.flow.combineTransform(this, transform = transform)

inline fun <reified T> Iterable<Flow<T>>.combinedAny(
    crossinline predicate: (T) -> Boolean,
): Flow<Boolean> = this.combine { it.any(predicate) }

inline fun <reified T> Iterable<Flow<T>>.combinedAll(
    crossinline predicate: (T) -> Boolean,
): Flow<Boolean> = this.combine { it.all(predicate) }

inline fun <reified T> Iterable<Flow<T>>.combinedNone(
    crossinline predicate: (T) -> Boolean,
): Flow<Boolean> = this.combine { it.none(predicate) }

inline fun <reified T, R> Iterable<Flow<T>>.combinedMap(
    crossinline predicate: (T) -> R,
): Flow<List<R>> = this.combine { it.map(predicate) }
