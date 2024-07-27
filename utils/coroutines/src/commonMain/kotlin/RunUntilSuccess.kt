package me.him188.ani.utils.coroutines

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


/**
 * Runs the block multiple times, returns when it succeeds the first time. with a delay between each attempt.
 */
suspend inline fun <R, V> V.runUntilSuccess(
    maxAttempts: Int = 5,
    onFailure: (Exception) -> Unit = { it.printStackTrace() },
    block: V.() -> R,
): R {
    contract { callsInPlace(block, InvocationKind.AT_LEAST_ONCE) }
    var failed = 0
    while (currentCoroutineContext().isActive) {
        try {
            return block()
        } catch (e: Exception) {
            onFailure(e)
            failed++
            if (failed >= maxAttempts) {
                throw IllegalStateException("Failed to run block after $maxAttempts attempts", e)
            }
            delay(backoffDelay(failed))
        }
    }
    yield() // throws CancellationException()
    throw CancellationException() // should not reach, defensive
}

/**
 * Runs the block multiple times, returns when it succeeds the first time. with a delay between each attempt.
 */
suspend inline fun <R> runUntilSuccess(
    onError: (Exception) -> Unit = { it.printStackTrace() },
    block: () -> R,
): R {
    contract { callsInPlace(block, InvocationKind.AT_LEAST_ONCE) }
    var failed = 0
    while (currentCoroutineContext().isActive) {
        try {
            return block()
        } catch (e: Exception) {
            onError(e)
            failed++
            delay(backoffDelay(failed))
        }
    }
    yield() // throws CancellationException()
    throw CancellationException() // should not reach, defensive
}

@PublishedApi
internal fun backoffDelay(failureCount: Int): Duration {
    return when (failureCount) {
        0, 1 -> 1.seconds
        2 -> 2.seconds
        3 -> 4.seconds
        else -> 8.seconds
    }
}

// 解决 ios ambiguity 和 common 里不能直接构造
@Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
inline fun CancellationException(
    message: String? = null,
    cause: Throwable? = null
): kotlinx.coroutines.CancellationException {
    return kotlinx.coroutines.CancellationException(
        message = message,
        cause,
    ) // 加名字后就只能 resolve 到 Kotlin, 否则会在 JVM ambiguity
}
