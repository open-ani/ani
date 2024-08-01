package me.him188.ani.utils.coroutines

import kotlinx.coroutines.flow.MutableStateFlow

inline fun <T> MutableStateFlow<T>.update(block: T.() -> T) {
    while (true) {
        val current = value
        val new = current.block()
        if (compareAndSet(current, new)) {
            return
        }
    }
}