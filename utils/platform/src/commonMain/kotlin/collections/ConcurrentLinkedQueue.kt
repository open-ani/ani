package me.him188.ani.utils.platform.collections

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

class ConcurrentQueue<E> {
    private val delegate = ArrayDeque<E>()
    private val lock = ReentrantLock()

    fun add(element: E): Boolean {
        lock.withLock {
            return delegate.add(element)
        }
    }

    fun remove(element: E): Boolean {
        lock.withLock {
            return delegate.remove(element)
        }
    }

    fun removeFirst(): E {
        lock.withLock {
            return delegate.removeFirst()
        }
    }

    fun removeFirstOrNull(): E? {
        lock.withLock {
            return delegate.removeFirstOrNull()
        }
    }

    fun firstOrNull(): E? {
        lock.withLock {
            return delegate.firstOrNull()
        }
    }

    fun addLast(element: E) {
        lock.withLock {
            return delegate.addLast(element)
        }
    }

    fun isEmpty(): Boolean {
        lock.withLock {
            return delegate.isEmpty()
        }
    }

    fun isNotEmpty(): Boolean {
        lock.withLock {
            return delegate.isNotEmpty()
        }
    }
}