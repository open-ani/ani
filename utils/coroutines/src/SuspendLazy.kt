package me.him188.ani.utils.coroutines

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface SuspendLazy<T> {
    val isInitialized: Boolean

    /**
     * Returns the value immediately without suspending if the value is already initialized.
     * Otherwise, initialize the value in the current coroutine context and return it.
     *
     * This function supports coroutine cancellation if the initializer do.
     * When current coroutine is cancelled, the initializer is cancelled and the value is not initialized.
     * When [get] is called the next time, the initializer is called again.
     */
    suspend fun get(): T
}

fun <T> SuspendLazy(
    initializer: suspend () -> T
): SuspendLazy<T> = SuspendLazyImpl(initializer)

class SuspendLazyImpl<T>(
    initializer: suspend () -> T,
) : SuspendLazy<T> {
    @Volatile
    private var initialized = false
    private var initializer: (suspend () -> T)? = initializer

    override val isInitialized: Boolean get() = initialized

    @Volatile
    private var value: T? = null

    private val lock = Mutex()

    @Suppress("UNCHECKED_CAST")
    override suspend fun get(): T {
        if (initialized) return value as T

        lock.withLock {
            if (initialized) return value as T
            val initializer = initializer ?: error("initializer should not be null")
            value = initializer()
            initialized = true
        }
        return value as T
    }
}