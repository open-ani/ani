package me.him188.ani.app.data.media

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.him188.ani.app.torrent.TorrentDownloadSession
import me.him188.ani.app.torrent.TorrentDownloader
import me.him188.ani.app.torrent.model.EncodedTorrentData
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.core.cache.MediaCache
import me.him188.ani.datasources.core.cache.MediaCacheEngine
import kotlin.coroutines.CoroutineContext

class TorrentMediaCacheEngine(
    private val mediaSourceId: String,
    private val getTorrentDownloader: suspend () -> TorrentDownloader,
) : MediaCacheEngine {
    private class TorrentMediaCache(
        override val media: CachedMedia,
        override val metadata: MediaCacheMetadata,
        private val download: TorrentDownloadSession,
    ) : MediaCache {
        override val progress: Flow<Float> get() = download.progress
        override val totalSize: Flow<FileSize> get() = download.totalBytes.map { it.bytes }

        override suspend fun pause() {
            download.pause()
        }

        override suspend fun resume() {
            download.resume()
        }

        override fun delete() {
            download.close()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun restore(
        origin: Media,
        metadata: MediaCacheMetadata,
        parentContext: CoroutineContext
    ): MediaCache? {
        val data = metadata.extra["torrentData"]?.hexToByteArray() ?: return null
        return TorrentMediaCache(
            media = CachedMedia(
                origin,
                mediaSourceId,
                download = origin.download,
            ),
            metadata = metadata,
            download = getTorrentDownloader().startDownload(EncodedTorrentData(data))
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun createCache(
        origin: Media,
        request: MediaFetchRequest,
        parentContext: CoroutineContext
    ): MediaCache {
        val data = getTorrentDownloader().fetchTorrent(origin.mediaId)
        val metadata = MediaCacheMetadata(
//            cacheMediaSourceId = this.mediaSourceId,
            episodeId = request.episodeId,
            subjectNames = request.subjectNames,
            episodeSort = request.episodeSort,
            episodeName = request.episodeName,
            extra = mapOf(
                "torrentData" to data.data.toHexString()
            )
        )
        return TorrentMediaCache(
            media = CachedMedia(
                origin,
                mediaSourceId,
                download = origin.download,
            ),
            metadata = metadata,
            download = getTorrentDownloader().startDownload(data)
        )
    }
}