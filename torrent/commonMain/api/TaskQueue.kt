package me.him188.ani.app.torrent.api

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import me.him188.ani.utils.coroutines.cancellableCoroutineScope
import me.him188.ani.utils.logging.debug
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration.Companion.seconds

internal class TaskQueue<Receiver>(
    private val enableTimeoutWatchdog: Boolean,
    private val onSubmit: () -> Unit = {},
) {
    private val tasks = ConcurrentLinkedQueue<Task<Receiver>>()

    private class Task<Receiver>(
        private val creationStacktrace: Exception? = null,
        private val task: (Receiver) -> Unit
    ) {
        operator fun invoke(handle: Receiver) {
            try {
                logger.debug { "Invoking task: $task" }
                task(handle)
                logger.debug { "Invoked task: $task" }
            } catch (e: Throwable) {
                if (creationStacktrace != null) {
                    e.addSuppressed(creationStacktrace)
                }
                logger.error(e) { "An exception occurred when executing $task" }
            }
        }
    }

    private fun submitImpl(
        task: (Receiver) -> Unit
    ): Task<Receiver> {
        Task(
            if (enableTimeoutWatchdog) Exception("Task creation stacktrace") else null,
            task
        ).also {
            tasks.add(it)
            return it
        }
    }

    fun submit(task: (Receiver) -> Unit) {
        submitImpl(task)
        onSubmit()
    }

    /**
     * 挂起协程, 在 BT 线程执行 [action], 然后返回结果
     */
    suspend fun <R> withHandle(action: (Receiver) -> R): R {
        return suspendCancellableCoroutine { cont ->
            val job: (Receiver) -> Unit = {
                cont.resumeWith(kotlin.runCatching { action(it) })
            }
            val task = submitImpl {
                job(it)
            }
            cont.invokeOnCancellation {
                tasks.remove(task)
            }
        }
    }

    fun invokeAll(handle: Receiver) {
        while (tasks.isNotEmpty()) {
            val job = tasks.poll() ?: break
            if (enableTimeoutWatchdog) {
                runBlocking {
                    cancellableCoroutineScope {
                        launch {
                            delay(5.seconds)
                            logger.warn { "Job $job in handle took too long" }
                        }
                        launch(start = CoroutineStart.UNDISPATCHED) {
                            job(handle)
                            cancelScope()
                        }
                    }
                }
            } else {
                job(handle)
            }
        }
    }

    private companion object {
        private val logger = logger(TaskQueue::class)
    }
}