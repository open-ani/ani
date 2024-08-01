package me.him188.ani.utils.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Calls the specified [block] with a given coroutine context in
 * [an interruptible manner](https://docs.oracle.com/javase/tutorial/essential/concurrency/interrupt.html).
 * The blocking code block will be interrupted and this function will throw [CancellationException]
 * if the coroutine is cancelled.
 *
 * Example:
 *
 * ```
 * withTimeout(500L) {            // Cancels coroutine on timeout
 *     runInterruptible {         // Throws CancellationException if interrupted
 *         doSomethingBlocking()  // Interrupted on coroutines cancellation
 *     }
 * }
 * ```
 *
 * There is an optional [context] parameter to this function working just like [withContext].
 * It enables single-call conversion of interruptible Java methods into suspending functions.
 * With one call here we are moving the call to [Dispatchers.IO] and supporting interruption:
 *
 * ```
 * suspend fun <T> BlockingQueue<T>.awaitTake(): T =
 *         runInterruptible(Dispatchers.IO) { queue.take() }
 * ```
 *
 * `runInterruptible` uses [withContext] as an underlying mechanism for switching context,
 * meaning that the supplied [block] is invoked in an [undispatched][CoroutineStart.UNDISPATCHED]
 * manner directly by the caller if [CoroutineDispatcher] from the current [coroutineContext][currentCoroutineContext]
 * is the same as the one supplied in [context].
 */
expect suspend fun <R> runInterruptible(context: CoroutineContext = EmptyCoroutineContext, block: () -> R): R
