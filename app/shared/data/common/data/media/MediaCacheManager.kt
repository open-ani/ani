package me.him188.ani.app.data.media

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.transformLatest
import me.him188.ani.app.platform.notification.NotifManager
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.core.cache.MediaCache
import me.him188.ani.datasources.core.cache.MediaCacheStorage
import me.him188.ani.datasources.core.cache.sum
import me.him188.ani.utils.coroutines.sampleWithInitial
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class MediaCacheManager(
    val storagesIncludingDisabled: List<MediaCacheStorage>,
    final override val backgroundScope: CoroutineScope,
) : KoinComponent, HasBackgroundScope { // available via inject
    private val notificationManager: NotifManager by inject()

    val enabledStorages: Flow<List<MediaCacheStorage>> = kotlin.run {
        combine(storagesIncludingDisabled.map { it.isEnabled.map { enabled -> if (enabled) it else null } }) {
            it.filterNotNull()
        }
    }

    val storages: List<Flow<MediaCacheStorage?>> by lazy {
        storagesIncludingDisabled.map { storage ->
            storage.isEnabled.map {
                if (it) storage else null
            }
        }
    }

    private val cacheListFlow: Flow<List<MediaCache>> by lazy {
        combine(storagesIncludingDisabled.map { it.listFlow }) {
            it.asSequence().flatten().toList()
        }
    }

    @Stable
    fun listCacheForEpisode(
        subjectId: Int,
        episodeId: Int,
    ): Flow<List<MediaCache>> {
        val subjectIdString = subjectId.toString()
        val episodeIdString = episodeId.toString()
        return cacheListFlow.map { list ->
            list.filter { cache ->
                cache.metadata.subjectId == subjectIdString && cache.metadata.episodeId == episodeIdString
            }
        }
    }

    /**
     * Returns the cache status for the episode, updated lively and sampled for 1000ms.
     */
    @Stable
    fun cacheStatusForEpisode(
        subjectId: Int,
        episodeId: Int,
    ): Flow<EpisodeCacheStatus> {
        val subjectIdString = subjectId.toString()
        val episodeIdString = episodeId.toString()
        return cacheListFlow.transformLatest { list ->
            var hasAnyCached: MediaCache? = null
            var hasAnyCaching: MediaCache? = null

            for (mediaCache in list) {
                if (mediaCache.metadata.subjectId == subjectIdString && mediaCache.metadata.episodeId == episodeIdString) {
                    hasAnyCached = mediaCache
                    if (mediaCache.finished.firstOrNull() != true) {
                        hasAnyCaching = mediaCache
                    }
                }
            }

            when {
                hasAnyCaching != null -> {
                    emitAll(
                        combine(
                            hasAnyCaching.progress
                                .sampleWithInitial(1000) // Sample might not emit the last value
                                .onCompletion { if (it == null) emit(1f) }, // Always emit 1f on finish
                            hasAnyCaching.totalSize
                        ) { progress, totalSize ->
                            if (progress == 1f) {
                                EpisodeCacheStatus.Cached(totalSize)
                            } else {
                                EpisodeCacheStatus.Caching(
                                    progress = progress,
                                    totalSize = totalSize
                                )
                            }
                        }
                    )
                }

                hasAnyCached != null -> {
                    emitAll(hasAnyCached.totalSize.map {
                        EpisodeCacheStatus.Cached(
                            totalSize = it
                        )
                    })
                }

                else -> {
                    emit(EpisodeCacheStatus.NotCached)
                }
            }
        }.flowOn(Dispatchers.Default)
    }

    init {
        enabledStorages
            .mapLatest { list ->
                val stats = list.map { it.stats }.sum()
                val notif = notificationManager.downloadChannel.notif

                var lastFile: MediaCache? = null
                stats.progress
                    .sampleWithInitial(1000)
                    .collectLatest { progress ->
                        if (progress >= 1f) {
                            notif.ongoing = false
                            notif.cancel()
                            return@collectLatest
                        }
                        val downloadRate = stats.downloadRate.first()
                        notif.contentTitle = "正在下载 $downloadRate/s"

                        val file = lastFile
                        if (file == null ||
                            file.isDeleted.firstOrNull() == true ||
                            file.finished.firstOrNull() != false
                        ) {
                            lastFile = list.findFirstDownloadingFile()
                        }
                        notif.contentText = lastFile?.previewText
                        notif.setProgress(100, (progress * 100).toInt())
                        notif.silent = true
                        notif.ongoing = true
                        notif.show()
                    }
            }
            .flowOn(CoroutineName("MediaCacheManager.notifications"))
            .launchIn(backgroundScope)
    }

    private suspend fun List<MediaCacheStorage>.findFirstDownloadingFile(): MediaCache? {
        return firstNotNullOfOrNull { storage ->
            storage.listFlow.first().find {
                !it.finished.first()
            }
        }
    }

    private val MediaCache.previewText: String
        get() {
            metadata.subjectNames.firstOrNull()?.let { name ->
                return "$name ${metadata.episodeName}"
            }
            return origin.originalTitle
        }

    companion object {
        const val LOCAL_FS_MEDIA_SOURCE_ID = "local-file-system"
    }
}

@Stable
sealed class EpisodeCacheStatus {
    /**
     * At least one cache is fully downloaded.
     */
    @Stable
    data class Cached(
        val totalSize: FileSize,
    ) : EpisodeCacheStatus()

    /**
     * No cache is fully downloaded, but at least one cache is downloading.
     */
    @Stable
    data class Caching(
        /**
         * This will not be 1f (on which it will become [Cached]).
         */
        val progress: Float?, // null means still connecting
        val totalSize: FileSize,
    ) : EpisodeCacheStatus()

    @Stable
    data object NotCached : EpisodeCacheStatus()
}

class MediaCacheManagerImpl(
    storagesIncludingDisabled: List<MediaCacheStorage>,
    backgroundScope: CoroutineScope,
) : MediaCacheManager(storagesIncludingDisabled, backgroundScope)
