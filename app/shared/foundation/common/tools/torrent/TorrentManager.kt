package me.him188.ani.app.tools.torrent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.job
import me.him188.ani.app.torrent.TorrentDownloader
import me.him188.ani.app.torrent.TorrentDownloaderFactory
import me.him188.ani.utils.coroutines.runUntilSuccess
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import kotlin.coroutines.CoroutineContext

/**
 * Manages the downloads of torrents.
 */
interface TorrentManager {
    val lastError: StateFlow<TorrentDownloaderManagerError?>

    val downloader: Deferred<TorrentDownloader>
}

class TorrentDownloaderManagerError(
    val exception: Throwable,
)

class DefaultTorrentManager(
    parentCoroutineContext: CoroutineContext,
    private val downloaderFactory: TorrentDownloaderFactory,
    downloaderStart: CoroutineStart = CoroutineStart.DEFAULT
) : TorrentManager {
    private val scope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))

    override val lastError: MutableStateFlow<TorrentDownloaderManagerError?> = MutableStateFlow(null)

    override val downloader = scope.async(start = downloaderStart) {
        runUntilSuccess(
            onFailure = { e ->
                lastError.value = TorrentDownloaderManagerError(e)
                logger.warn(e) { "Failed to create TorrentDownloader, retrying later" }
            }
        ) {
            downloaderFactory.create()
        }.also { downloader ->
            scope.coroutineContext.job.invokeOnCompletion {
                downloader.close()
            }
        }
    }

    private companion object {
        private val logger = logger(this::class)
    }
}