package me.him188.ani.app.torrent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

interface TorrentDownloaderManager {
    val lastError: SharedFlow<String?>
    val torrentDownloader: SharedFlow<TorrentDownloader>
}

internal class TorrentDownloaderManagerImpl(
    parentCoroutineContext: CoroutineContext
) : TorrentDownloaderManager, KoinComponent {
    private val scope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))

    private val logger = logger(this::class)

    private val torrentDownloaderFactory: TorrentDownloaderFactory by inject()

    override val lastError: MutableSharedFlow<String?> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override val torrentDownloader: SharedFlow<TorrentDownloader> = flow {
        var retryDelay = 2.seconds
        while (currentCoroutineContext().isActive) {
            val downloader = try {
                torrentDownloaderFactory.create()
            } catch (e: Throwable) {
                lastError.tryEmit(e.toString())
                logger.warn(e) { "Failed to create TorrentDownloader, retrying later" }
                kotlinx.coroutines.delay(retryDelay)
                retryDelay = (retryDelay * 2).coerceAtMost(60.seconds)
                continue
            }
            emit(downloader)
            break
        }
    }.shareIn(scope, replay = 1, started = SharingStarted.Eagerly)
}