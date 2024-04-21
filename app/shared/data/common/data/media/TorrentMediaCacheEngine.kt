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
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import me.him188.ani.app.data.media.resolver.TorrentVideoSourceResolver
import me.him188.ani.app.torrent.FilePriority
import me.him188.ani.app.torrent.TorrentDownloader
import me.him188.ani.app.torrent.TorrentFileHandle
import me.him188.ani.app.torrent.model.EncodedTorrentData
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.core.cache.MediaCache
import me.him188.ani.datasources.core.cache.MediaCacheEngine
import me.him188.ani.datasources.core.cache.MediaStats
import me.him188.ani.utils.coroutines.SuspendLazy
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

private const val EXTRA_TORRENT_DATA = "torrentData"

class TorrentMediaCacheEngine(
    private val mediaSourceId: String,
    getTorrentDownloader: suspend () -> TorrentDownloader,
) : MediaCacheEngine {
    private companion object {
        val logger = logger<TorrentMediaCacheEngine>()
    }

    private val downloader = SuspendLazy {
        getTorrentDownloader()
    }

    private inner class TorrentMediaCache(
        override val origin: Media,
        override val metadata: MediaCacheMetadata,
        private val file: Flow<TorrentFileHandle>,
    ) : MediaCache {
        override suspend fun getCachedMedia(): CachedMedia {
            val file = file.first()
            val finished = file.entry.stats.isFinished.first()
            if (finished) {
                val filePath = file.entry.resolveFile()
                if (!filePath.exists()) {
                    error("TorrentFileHandle has finished but file does not exist: $filePath")
                }
                logger.info { "getCachedMedia: Torrent has already finished, returning file $filePath" }
                return CachedMedia(
                    origin,
                    mediaSourceId,
                    download = ResourceLocation.LocalFile(filePath.toString())
                )
            } else {
                logger.info { "getCachedMedia: Torrent has not yet finished, returning torrent" }
                return CachedMedia(
                    origin,
                    mediaSourceId,
                    download = origin.download,
//                download = ResourceLocation.LocalFile(session.first().filePath().toString())
                )
            }
        }

        private val entry get() = file.map { it.entry }

        override val downloadSpeed: Flow<FileSize>
            get() = entry.flatMapLatest { session ->
                session.stats.downloadRate.map {
                    it?.bytes ?: FileSize.Unspecified
                }
            }

        override val uploadSpeed: Flow<FileSize>
            get() = entry.flatMapLatest { session ->
                session.stats.uploadRate.map {
                    it?.bytes ?: FileSize.Unspecified
                }
            }

        override val progress: Flow<Float>
            get() = entry.flatMapLatest { it.stats.progress }

        override val finished: Flow<Boolean>
            get() = entry.flatMapLatest { it.stats.isFinished }

        override val totalSize: Flow<FileSize>
            get() = entry.flatMapLatest { session ->
                session.stats.totalBytes.map { it.bytes }
            }

        @Volatile
        private var deleted = false

        override suspend fun pause() {
            if (deleted) return
            file.first().pause()
        }

        override suspend fun resume() {
            if (deleted) return
            val file = file.first()
            logger.info { "Resuming file: $file" }
            file.resume(FilePriority.NORMAL)
        }

        override suspend fun delete() {
            if (deleted) return
            synchronized(this) {
                if (deleted) return
                deleted = true
            }
            val handle = file.first()
            val file = handle.entry.resolveFile()
            handle.close()
            if (file.exists()) {
                logger.info { "Deleting torrent cache: $file" }
                file.deleteIfExists()
            } else {
                logger.info { "Torrent cache does not exist, ignoring: $file" }
            }
        }
    }

    override val stats: MediaStats = object : MediaStats {
        override val uploaded: Flow<FileSize> =
            flow { emit(downloader.get()) }
                .flatMapLatest { it.totalUploaded }
                .map { it.bytes }
        override val downloaded: Flow<FileSize> =
            flow { emit(downloader.get()) }
                .flatMapLatest { it.totalDownloaded }
                .map { it.bytes }

        override val uploadRate: Flow<FileSize> =
            flow { emit(downloader.get()) }
                .flatMapLatest { it.totalUploadRate }
                .map { it.bytes }
        override val downloadRate: Flow<FileSize> =
            flow { emit(downloader.get()) }
                .flatMapLatest { it.totalDownloadRate }
                .map { it.bytes }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun restore(
        origin: Media,
        metadata: MediaCacheMetadata,
        parentContext: CoroutineContext
    ): MediaCache? {
        val data = metadata.extra[EXTRA_TORRENT_DATA]?.hexToByteArray() ?: return null
        return TorrentMediaCache(
            origin = origin,
            metadata = metadata,
            file = getFileEntryFlow(EncodedTorrentData(data), metadata, parentContext),
        )
    }

    private fun getFileEntryFlow(
        encoded: EncodedTorrentData,
        metadata: MediaCacheMetadata,
        parentContext: CoroutineContext
    ): SharedFlow<TorrentFileHandle> {
        val sessionFlow = flow {
            emit(downloader.get().startDownload(encoded, parentContext))
        }.mapNotNull { session ->
            TorrentVideoSourceResolver.selectVideoFileEntry(
                session.getFiles(),
                listOf(metadata.episodeName),
                metadata.episodeSort,
            )?.createHandle() ?: kotlin.run {
                session.closeIfNotInUse()
                null
            }
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
        val data = downloader.get().fetchTorrent(origin.download.uri)
        val metadata = request.withExtra(
            mapOf(EXTRA_TORRENT_DATA to data.data.toHexString())
        )
        return TorrentMediaCache(
            origin = origin,
            metadata = metadata,
            file = getFileEntryFlow(data, metadata, parentContext),
        )
    }
}