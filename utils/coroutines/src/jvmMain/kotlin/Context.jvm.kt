package me.him188.ani.utils.coroutines

import kotlin.coroutines.CoroutineContext

/**
 * @see kotlinx.coroutines.runInterruptible
 */
actual suspend fun <R> runInterruptible(context: CoroutineContext, block: () -> R): R {
    return kotlinx.coroutines.runInterruptible(context, block)
}