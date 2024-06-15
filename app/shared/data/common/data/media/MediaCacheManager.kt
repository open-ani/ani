package me.him188.ani.app.data.media

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.him188.ani.app.platform.notification.Notif
import me.him188.ani.app.platform.notification.NotifManager
import me.him188.ani.app.platform.notification.NotifPriority
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.sum
import me.him188.ani.datasources.core.cache.MediaCache
import me.him188.ani.datasources.core.cache.MediaCacheStorage
import me.him188.ani.datasources.core.cache.sum
import me.him188.ani.utils.coroutines.sampleWithInitial
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

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
                                EpisodeCacheStatus.Cached(totalSize, hasAnyCaching)
                            } else {
                                EpisodeCacheStatus.Caching(
                                    progress = progress,
                                    totalSize = totalSize,
                                    cache = hasAnyCaching
                                )
                            }
                        }
                    )
                }

                hasAnyCached != null -> {
                    emitAll(hasAnyCached.totalSize.map {
                        EpisodeCacheStatus.Cached(
                            totalSize = it,
                            cache = hasAnyCached
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
        if (notificationManager.hasPermission()) {
            startNotificationJob()
        }
    }

    private fun startNotificationJob() {
        val startTime = System.currentTimeMillis()
        enabledStorages.mapLatestSupervised { list ->
            val channel = notificationManager.downloadChannel

            val stats = list.map { it.stats }.sum()

            val summaryNotif = channel.newNotif().apply {
                silent = true
                ongoing = true
                setGroup("media-cache")
                setAsGroupSummary(true)
                contentTitle = "正在缓存"
                priority = NotifPriority.MIN
            }

            val visibleCount = AtomicInteger()
            for (storage in list) {
                launch {
                    if (System.currentTimeMillis() - startTime < 5000) {
                        delay(5000)
                    }

                    storage.listFlow.debounce(1000).collectLatest { caches ->
                        @Suppress("NAME_SHADOWING")
                        val caches = caches.toCollection(ConcurrentLinkedQueue())
                        val cacheIds = caches.mapTo(mutableSetOf()) { it.cacheId }
                        val visibleNotifications = ConcurrentHashMap<String, Notif>()
                        try {
                            while (currentCoroutineContext().isActive) {
                                for (cache in caches) {
                                    val progress = cache.progress.first()
                                    if (progress >= 1f) {
                                        caches.remove(cache)
                                        visibleNotifications.remove(cache.cacheId)?.let {
                                            visibleCount.decrementAndGet()
                                            it.release()
                                        }
                                    } else {
                                        visibleNotifications.getOrPut(cache.cacheId) {
                                            visibleCount.incrementAndGet()
                                            channel.newNotif().apply {
                                                setGroup("media-cache")
                                                contentTitle = cache.previewText
                                                silent = true
                                                ongoing = true
                                                priority = NotifPriority.MIN
                                            }
                                        }.run {
                                            val download = cache.downloadSpeed.first()
                                            contentText = "$download/s"
                                            setProgress(100, (progress * 100).toInt())
                                            show()
                                        }
                                    }
                                }

                                // 清除已经完成的通知
                                // toList is required here to avoid ConcurrentModificationException
                                visibleNotifications.keys.toList().forEach { cacheId ->
                                    if (!cacheIds.contains(cacheId)) {
                                        visibleNotifications.remove(cacheId)?.release()
                                    }
                                }

                                delay(3000)
                            }
                        } catch (e: Throwable) {
                            visibleNotifications.values.forEach { it.release() }
                            throw e
                        }
                        visibleNotifications.values.forEach { it.release() }
                    }
                }
            }

            launch {
                if (System.currentTimeMillis() - startTime < 5000) {
                    delay(5000)
                }

                //                    val anyCachingFlow = combine(list.map { storage -> storage.anyCaching }) { array ->
                //                        array.any { it }
                //                    }.sampleWithInitial(1000)
                combine(stats.downloadRate.sampleWithInitial(3000)) { downloadRate ->
                    //                        if (anyCaching) {
                    if (visibleCount.get() == 0) {
                        summaryNotif.cancel()
                    } else {
                        summaryNotif.run {
                            contentText = "下载 ${downloadRate.sum()}/s"
                            show()
                        }
                    }
                    //                        } else {
                    //                            summaryNotif.cancel()
                    //                        }
                }.collect()
            }
        }
            .flowOn(CoroutineName("MediaCacheManager.notifications"))
            .launchIn(backgroundScope)
    }

    private val MediaCache.previewText: String
        get() {
            metadata.subjectNames.firstOrNull()?.let { name ->
                return "$name ${metadata.episodeSort}"
            }
            return origin.originalTitle
        }

    companion object {
        const val LOCAL_FS_MEDIA_SOURCE_ID = "local-file-system"
    }
}

@Stable
sealed class EpisodeCacheStatus {
    abstract val cache: MediaCache?

    /**
     * At least one cache is fully downloaded.
     */
    @Stable
    data class Cached(
        val totalSize: FileSize,
        override val cache: MediaCache,
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
        override val cache: MediaCache,
    ) : EpisodeCacheStatus()

    @Stable
    data object NotCached : EpisodeCacheStatus() {
        override val cache: Nothing? get() = null
    }
}

class MediaCacheManagerImpl(
    storagesIncludingDisabled: List<MediaCacheStorage>,
    backgroundScope: CoroutineScope,
) : MediaCacheManager(storagesIncludingDisabled, backgroundScope)
