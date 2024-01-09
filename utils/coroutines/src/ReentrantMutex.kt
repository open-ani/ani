package me.him188.ani.utils.coroutines

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

sealed interface ReentrantMutex {
    val isLocked: Boolean
}

suspend inline fun <T> ReentrantMutex.withLock(crossinline block: suspend () -> T): T {
    when (this) {
        is ReentrantMutexImpl -> {
            val currentHolding = currentCoroutineContext()[HoldingReentrantMutex]
            if (currentHolding != null) {
                if (currentHolding.mutex === this) { // holding same lock
                    return block()
                }
            }

            try {
                lock()
                return withContext(HoldingReentrantMutex(this)) { block() }
            } finally {
                unlock()
            }
        }
    }
}

fun ReentrantMutex(): ReentrantMutex = ReentrantMutexImpl()

@PublishedApi
internal class ReentrantMutexImpl : ReentrantMutex {
    private val mutex: Mutex = Mutex()

    override val isLocked: Boolean = mutex.isLocked

    suspend fun lock() {
        mutex.lock()
    }

    fun unlock() {
        mutex.unlock()
    }
}

@PublishedApi
internal class HoldingReentrantMutex(
    val mutex: ReentrantMutexImpl,
) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    companion object Key : CoroutineContext.Key<HoldingReentrantMutex>
}
