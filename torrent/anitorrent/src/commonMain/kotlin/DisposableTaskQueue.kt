package me.him188.ani.app.torrent.anitorrent

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

class DisposableTaskQueue<T>(
    private val receiver: T // 可以让 lambda 少捕获一个对象, 减少内存开销
) : SynchronizedObject() {
    private var queue: MutableList<T.() -> Unit>? = mutableListOf()

    fun add(task: T.() -> Unit): Boolean {
        synchronized(this) {
            val queue = queue ?: return false
            queue.add(task)
            return true
        }
    }

    fun runAndDispose(): Int {
        synchronized(this) {
            val queue = queue
            checkNotNull(queue) {
                "TaskQueue is already closed"
            }
            for (task in queue) {
                task(receiver)
            }
            val size = queue.size
            this.queue = null // completely free
            return size
        }
    }
}
