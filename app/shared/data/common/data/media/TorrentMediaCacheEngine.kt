package me.him188.ani.app.data.media

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import me.him188.ani.app.torrent.TorrentDownloadSession
import me.him188.ani.app.torrent.TorrentDownloader
import me.him188.ani.app.torrent.model.EncodedTorrentData
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.core.cache.MediaCache
import me.him188.ani.datasources.core.cache.MediaCacheEngine
import me.him188.ani.utils.coroutines.SuspendLazy
import kotlin.coroutines.CoroutineContext

private const val EXTRA_TORRENT_DATA = "torrentData"

class TorrentMediaCacheEngine(
    private val mediaSourceId: String,
    private val getTorrentDownloader: suspend () -> TorrentDownloader,
) : MediaCacheEngine {
    private inner class TorrentMediaCache(
        override val origin: Media,
        override val metadata: MediaCacheMetadata,
        private val session: SharedFlow<TorrentDownloadSession>,
    ) : MediaCache {

        private val cachedMedia: SuspendLazy<CachedMedia> = SuspendLazy {
            CachedMedia(
                origin,
                mediaSourceId,
                download = ResourceLocation.LocalFile(session.first().filePath().toString())
            )
        }

        override suspend fun getCachedMedia(): CachedMedia = cachedMedia.get()

        override val downloadSpeed: Flow<FileSize>
            get() = session.flatMapLatest { session -> session.downloadRate.map { it?.bytes ?: FileSize.Unspecified } }

        override val uploadSpeed: Flow<FileSize>
            get() = session.flatMapLatest { session -> session.uploadRate.map { it?.bytes ?: FileSize.Unspecified } }

        override val progress: Flow<Float>
            get() = session.flatMapLatest { it.progress }

        override val finished: Flow<Boolean>
            get() = session.flatMapLatest { it.isFinished }

        override val totalSize: Flow<FileSize>
            get() = session.flatMapLatest { session ->
                session.totalBytes.map { it.bytes }
            }

        @Volatile
        private var deleted = false

        override suspend fun pause() {
            if (deleted) return
            session.first().pause()
        }

        override suspend fun resume() {
            if (deleted) return
            session.first().resume()
        }

        override suspend fun delete() {
            if (deleted) return
            synchronized(this) {
                if (deleted) return
                deleted = true
            }
            session.first().closeAndDelete()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun restore(
        origin: Media,
        metadata: MediaCacheMetadata,
        parentContext: CoroutineContext
    ): MediaCache? {
        val data = metadata.extra[EXTRA_TORRENT_DATA]?.hexToByteArray() ?: return null
        val sessionFlow = getSessionFlow(
            EncodedTorrentData(data),
            parentContext
        )
        return TorrentMediaCache(
            origin = origin,
            metadata = metadata,
            session = sessionFlow,
        )
    }

    private fun getSessionFlow(
        encoded: EncodedTorrentData,
        parentContext: CoroutineContext
    ): SharedFlow<TorrentDownloadSession> {
        val sessionFlow = flow {
            emit(getTorrentDownloader().startDownload(encoded, parentContext))
        }.shareIn(
            CoroutineScope(parentContext + Job(parentContext[Job])),
            started = SharingStarted.Lazily,
            replay = 1,
        )
        return sessionFlow
    }

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun createCache(
        origin: Media,
        request: MediaCacheMetadata,
        parentContext: CoroutineContext
    ): MediaCache {
        val data = getTorrentDownloader().fetchTorrent(origin.download.uri)
        val metadata = request.withExtra(
            mapOf(EXTRA_TORRENT_DATA to data.data.toHexString())
        )
        return TorrentMediaCache(
            origin = origin,
            metadata = metadata,
            session = getSessionFlow(data, parentContext),
        )
    }
}