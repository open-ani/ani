package me.him188.ani.utils.coroutines

import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

actual suspend fun <R> runInterruptible(context: CoroutineContext, block: () -> R): R {
    return withContext(context) {
        block()
    }
}