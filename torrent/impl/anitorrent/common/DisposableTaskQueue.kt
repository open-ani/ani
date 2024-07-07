package me.him188.ani.app.torrent.anitorrent

class DisposableTaskQueue<T>(
    private val receiver: T // 可以让 lambda 少捕获一个对象, 减少内存开销
) {
    private var queue: MutableList<T.() -> Unit>? = mutableListOf()

    @Synchronized
    fun add(task: T.() -> Unit): Boolean {
        val queue = queue ?: return false
        queue.add(task)
        return true
    }

    @Synchronized
    fun runAndDispose() {
        val queue = queue
        checkNotNull(queue) {
            "TaskQueue is already closed"
        }
        for (task in queue) {
            task(receiver)
        }
        this.queue = null // completely free
    }
}
