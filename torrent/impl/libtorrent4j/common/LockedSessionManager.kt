package me.him188.ani.app.torrent.libtorrent4j

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.him188.ani.app.torrent.api.handle.EventListener
import me.him188.ani.app.torrent.api.handle.TorrentThread
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import org.libtorrent4j.AlertListener
import org.libtorrent4j.SessionManager
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.TorrentAlert
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors

class LockedSessionManager(
    @PublishedApi
    internal val sessionManager: SessionManager,
) {
    private val listeners = CopyOnWriteArraySet<EventListener>()

    init {
        sessionManager.addListener(
            object : AlertListener {
                override fun types(): IntArray = NeededTorrentEventTypes

                @OptIn(TorrentThread::class)
                override fun alert(alert: Alert<*>) {
                    if (alert !is TorrentAlert<*>) return
                    listeners.forEach { listener ->
                        try {
                            listener.onAlert(alert)
                        } catch (e: Throwable) {
                            logger.error(e) { "An exception occurred in EventListener for alter $alert" }
                        }
                    }
                }
            },
        )
    }

    fun addListener(listener: EventListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: EventListener) {
        listeners.remove(listener)
    }

    suspend inline fun <R> use(
        crossinline block: SessionManager.() -> R
    ) = withContext(dispatcher) {
        block(sessionManager)
    }

    companion object {
        /**
         * Shared dispatcher for all [LockedSessionManager] instances.
         *
         * Libtorrent crashes (the entire VM) if it is called from multiple threads.
         */ // unfortunately, we have to keep this dispatcher live for the entire app lifecycle.
        val dispatcher = Executors.newSingleThreadExecutor {
            Thread(it, "LockedSessionManager.dispatcher")
        }.asCoroutineDispatcher()

        private val logger = logger(LockedSessionManager::class)

        private val scope = CoroutineScope(
            dispatcher + CoroutineExceptionHandler { _, throwable ->
                logger.warn(throwable) { "An exception occurred in LockedSessionManager" }
            },
        )

        fun launch(block: suspend () -> Unit) {
            scope.launch(CoroutineName("LockedSessionManager.launch")) {
                block()
            }
        }
    }
}