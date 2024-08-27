package me.him188.ani.danmaku.api

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn

interface DanmakuSession {
    val events: Flow<DanmakuEvent>

    /**
     * 尝试在下一逻辑帧重新填充弹幕
     */
    fun requestRepopulate()
}

/**
 * Merge list of danmaku session, emitting all [DanmakuSession.events].
 *
 * - For [DanmakuEvent.Add], just emit to final flow.
 * - For [DanmakuEvent.Repopulate], only emits to final flow when all upstream flow emits and merged.
 */
fun List<DanmakuSession>.merge(): DanmakuSession {
    val self = this
    return object : DanmakuSession {
        private val logger = logger<DanmakuSession>()
        
        override val events: Flow<DanmakuEvent> = channelFlow {
            // If repopulateDeferred is not null, it means there is a ongoing awaiting process.
            var repopulateDeferred: List<CompletableDeferred<DanmakuEvent.Repopulate>>? = null
            val deferredLock = Mutex()
            val repopulateTasker = MonoTasker(this)
            
            fun launchAwaitTask() = repopulateTasker.launch {
                val deferred = repopulateDeferred ?: return@launch
                val results = withTimeout(100) { deferred.awaitAll() }
                    .asSequence()
                    .map { it.list }
                    .flatten()
                    .sortedBy { it.playTimeMillis }
                    .toList()
                send(DanmakuEvent.Repopulate(results))
            }.invokeOnCompletion { ex ->
                // We should emit collected event if repopulate task is cancelled.
                if (ex is TimeoutCancellationException || ex is CancellationException) { 
                    val deferred = repopulateDeferred
                    check(deferred != null)
                    val results = deferred
                        .asSequence()
                        .filter {
                            if (it.isActive) {
                                it.cancel(CancellationException("timeout on awaiting repopulate event"))
                                false
                            } else it.isCompleted
                        }
                        .map { it.getCompleted().list }
                        .flatten()
                        .sortedBy { it.playTimeMillis }
                        .toList()
                    trySend(DanmakuEvent.Repopulate(results))
                } else if(ex != null) {
                    logger.warn(ex) { "failed to repopulate merged danmaku." }
                }
                repopulateDeferred = null
            }

            self.forEachIndexed { index, session ->
                launch {
                    session.events.collect { event ->
                        when (event) {
                            is DanmakuEvent.Add -> send(event) // Directly emit Add event to final flow
                            is DanmakuEvent.Repopulate -> {
                                val deferred = deferredLock.withLock {
                                    repopulateDeferred ?: MutableList(self.size) {
                                        CompletableDeferred<DanmakuEvent.Repopulate>()
                                    }.also {
                                        repopulateDeferred = it
                                        launchAwaitTask()
                                    }
                                }

                                deferred[index].complete(event)
                            }
                        }
                    }
                }
            }
        }
        override fun requestRepopulate() {
            self.forEach { it.requestRepopulate() }
        }
    }
}

private object EmptyDanmakuSession : DanmakuSession {
    override val events: Flow<DanmakuEvent> get() = flowOf(DanmakuEvent.Repopulate(emptyList()))
    override fun requestRepopulate() {
    }
}

fun emptyDanmakuSession(): DanmakuSession = EmptyDanmakuSession
