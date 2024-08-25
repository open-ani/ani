package me.him188.ani.app.data.source.media.cache.storage

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.him188.ani.app.data.source.media.cache.MediaCache
import me.him188.ani.app.data.source.media.cache.engine.MediaCacheEngine
import me.him188.ani.app.data.source.media.cache.engine.MediaStats
import me.him188.ani.app.data.source.media.fetch.MediaFetcher
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.paging.SinglePagePagedSource
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.source.matches
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.utils.coroutines.childScope
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.io.createDirectories
import me.him188.ani.utils.io.delete
import me.him188.ani.utils.io.exists
import me.him188.ani.utils.io.extension
import me.him188.ani.utils.io.moveTo
import me.him188.ani.utils.io.name
import me.him188.ani.utils.io.readText
import me.him188.ani.utils.io.resolve
import me.him188.ani.utils.io.useDirectoryEntries
import me.him188.ani.utils.io.writeText
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import kotlin.concurrent.Volatile
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private const val METADATA_FILE_EXTENSION = "metadata"

/**
 * 本地目录缓存, 管理本地目录以及元数据的存储, 调用 [MediaCacheEngine] 进行缓存的实际创建
 */
class DirectoryMediaCacheStorage(
    override val mediaSourceId: String,
    val metadataDir: SystemPath,
    private val engine: MediaCacheEngine,
    private val dataStore: DataStore<SaveData>,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : MediaCacheStorage {
    @Serializable
    data class SaveData(
        val uploaded: FileSize,
    ) {
        companion object {
            val Initial = SaveData(0.bytes)
        }
    }

    private companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }
        private val logger = logger<DirectoryMediaCacheStorage>()
    }

    private val scope: CoroutineScope = parentCoroutineContext.childScope()

    init {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            val maxUploaded = object {
                @Volatile
                var value: Long = 0L
            }
            engine.stats.collect { stats ->
                val new = stats.downloaded
                if (new.isUnspecified) return@collect

                val diff = (new.inBytes - maxUploaded.value).bytes
                if (diff.inBytes > 0) {
                    maxUploaded.value = maxOf(maxUploaded.value, new.inBytes)

                    if (diff.inMegaBytes < 100) {
                        dataStore.updateData { it.copy(uploaded = it.uploaded + diff) }
                    } else {
                        // 有时候启动时上传量会突增到几百 GB, 不知道是什么原因, 就先直接过滤掉超大的 diff 了
                        // 不太可能有人能 100MB/s 上传
                    }
                }
            }
        }
    }

    @Serializable
    class MediaCacheSave(
        val origin: Media,
        val metadata: MediaCacheMetadata,
    )

    override suspend fun restorePersistedCaches() {
        withContext(Dispatchers.IO) {
            if (!metadataDir.exists()) {
                metadataDir.createDirectories()
            }
        }

        metadataDir.useDirectoryEntries { files ->
            val allRecovered = mutableListOf<MediaCache>()
            val semaphore = Semaphore(8)
            supervisorScope {
                for (file in files) {
                    launch {
                        semaphore.withPermit {
                            restoreFile(
                                file,
                                reportRecovered = { cache ->
                                    lock.withLock {
                                        listFlow.value += cache
                                    }
                                    allRecovered.add(cache)
                                },
                            )
                        }
                    }
                }
            }
            engine.deleteUnusedCaches(allRecovered)
        }
    }

    private suspend fun restoreFile(
        file: SystemPath,
        reportRecovered: suspend (MediaCache) -> Unit,
    ) = withContext(Dispatchers.IO) {
        if (file.extension != METADATA_FILE_EXTENSION) return@withContext

        val save = try {
            json.decodeFromString(MediaCacheSave.serializer(), file.readText())
        } catch (e: Exception) {
            logger.error(e) { "Failed to deserialize metadata file ${file.name}" }
            file.delete()
            return@withContext
        }

        try {
            val cache = engine.restore(save.origin, save.metadata, scope.coroutineContext)
            logger.info { "Cache restored: ${save.origin.mediaId}, result=${cache}" }

            if (cache != null) {
                reportRecovered(cache)
                cache.resume()
                logger.info { "Cache resumed: $cache" }
            }

            // try to migrate
            if (cache != null) {
                val newSaveName = getSaveFilename(cache)
                if (file.name != newSaveName) {
                    logger.warn {
                        "Metadata file name mismatch, renaming: " +
                                "${file.name} -> $newSaveName"
                    }
                    file.moveTo(metadataDir.resolve(newSaveName))
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to restore cache for ${save.origin.mediaId}" }
        }
    }

    override val listFlow: MutableStateFlow<List<MediaCache>> = MutableStateFlow(emptyList())

    override val cacheMediaSource: MediaSource by lazy {
        MediaCacheStorageSource(this, MediaSourceLocation.Local)
    }
    override val stats: Flow<MediaStats> = dataStore.data.combine(engine.stats) { save, stats ->
        MediaStats(
            uploaded = save.uploaded,
            downloaded = stats.downloaded,
            uploadSpeed = stats.uploadSpeed,
            downloadSpeed = stats.downloadSpeed,
        )
    }

    /**
     * Locks accesses to [listFlow]
     */
    private val lock = Mutex()

    override suspend fun cache(media: Media, metadata: MediaCacheMetadata, resume: Boolean): MediaCache {
        return lock.withLock {
            logger.info { "$mediaSourceId creating cache, metadata=$metadata" }
            listFlow.value.firstOrNull {
                cacheEquals(it, media, metadata)
            }?.let { return@withLock it }

            if (!engine.supports(media)) {
                throw UnsupportedOperationException("Engine does not support media: $media")
            }
            val cache = engine.createCache(
                media, metadata,
                scope.coroutineContext,
            )
            withContext(Dispatchers.IO) {
                metadataDir.resolve(getSaveFilename(cache)).writeText(
                    json.encodeToString(
                        MediaCacheSave.serializer(),
                        MediaCacheSave(media, cache.metadata),
                    ),
                )
            }
            listFlow.value += cache
            cache
        }.also {
            if (resume) {
                it.resume()
            }
        }
    }

    private fun cacheEquals(
        it: MediaCache,
        media: Media,
        metadata: MediaCacheMetadata = it.metadata
    ) = it.origin.mediaId == media.mediaId && it.metadata.episodeSort == metadata.episodeSort

    override suspend fun deleteFirst(predicate: (MediaCache) -> Boolean): Boolean {
        lock.withLock {
            val cache = listFlow.value.firstOrNull(predicate) ?: return false
            listFlow.value -= cache
            withContext(Dispatchers.IO) {
                metadataDir.resolve(getSaveFilename(cache)).delete()
            }
            cache.closeAndDeleteFiles()
            return true
        }
    }

    private fun getSaveFilename(cache: MediaCache) = "${cache.cacheId}.$METADATA_FILE_EXTENSION"

    override fun close() {
        if (engine is AutoCloseable) {
            engine.close()
        }
        scope.cancel()
    }
}

/**
 * 将 [MediaCacheStorage] 作为 [MediaSource], 这样可以被 [MediaFetcher] 搜索到以播放.
 */
private class MediaCacheStorageSource(
    private val storage: MediaCacheStorage,
    override val location: MediaSourceLocation = MediaSourceLocation.Local
) : MediaSource {
    override val mediaSourceId: String get() = storage.mediaSourceId
    override val kind: MediaSourceKind get() = MediaSourceKind.LocalCache

    override suspend fun checkConnection(): ConnectionStatus = ConnectionStatus.SUCCESS

    override suspend fun fetch(query: MediaFetchRequest): SizedSource<MediaMatch> {
        return SinglePagePagedSource {
            storage.listFlow.first().mapNotNull { cache ->
                val kind = query.matches(cache.metadata)
                if (kind == null) null
                else MediaMatch(cache.getCachedMedia(), kind)
            }.asFlow()
        }
    }
}
