package me.him188.ani.app.data.media.cache.requester

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.him188.ani.app.data.media.cache.MediaCache
import me.him188.ani.app.data.media.cache.MediaCacheStorage
import me.him188.ani.app.data.media.cache.contains
import me.him188.ani.app.data.media.fetch.MediaFetchSession
import me.him188.ani.app.data.media.fetch.MediaFetcher
import me.him188.ani.app.data.media.fetch.MediaSourceFetchResult
import me.him188.ani.app.data.media.fetch.create
import me.him188.ani.app.data.media.selector.MediaSelector
import me.him188.ani.app.data.media.selector.MediaSelectorFactory
import me.him188.ani.app.data.media.selector.autoSelect
import me.him188.ani.app.data.subject.EpisodeInfo
import me.him188.ani.app.data.subject.SubjectInfo
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.topic.contains
import me.him188.ani.datasources.api.topic.isSingleEpisode
import me.him188.ani.datasources.api.unwrapCached


/**
 * 剧集缓存请求工具.
 *
 * @see CacheRequestStage
 */
interface EpisodeCacheRequester {
    // 为什么不叫 MediaCacheRequester: 
    // 因为这个类依赖 [EpisodeCacheRequest], which 需要使用 subject 和 episode 的信息. 
    // 它实际上超出了 Media 的范围.

    /**
     * 当前请求进行到的阶段
     */
    val stage: StateFlow<CacheRequestStage>

    /**
     * 取消已有的请求, 并开始一个新的请求.
     */
    suspend fun request(request: EpisodeCacheRequest): CacheRequestStage.SelectMedia

    /**
     * 取消当前请求. 若没有请求则不做任何事情.
     */
    suspend fun cancelRequest()
}

/**
 * 当前进行中的或已经完成的请求, 为 `null` 时表示没有请求.
 */
val EpisodeCacheRequester.request: Flow<EpisodeCacheRequest?>
    get() = stage.map {
        when (it) {
            is CacheRequestStage.Done -> it.request
            CacheRequestStage.Idle -> null
            is CacheRequestStage.Working -> it.request
        }
    }

val EpisodeCacheRequester.mediaSourceResults: Flow<List<MediaSourceFetchResult>>
    get() = stage.map { it.mediaSourceResults }

data class EpisodeCacheRequest(
    val subjectInfo: SubjectInfo,
    val episodeInfo: EpisodeInfo,
)

suspend fun EpisodeCacheRequester.request(
    subjectId: Int, episodeId: Int, subjectManager: SubjectManager,
): CacheRequestStage.SelectMedia {
    val subjectInfo = subjectManager.getSubjectInfo(subjectId)
    val episodeInfo = subjectManager.getEpisodeInfo(episodeId)
    return request(EpisodeCacheRequest(subjectInfo, episodeInfo))
}

fun EpisodeCacheRequester(
    mediaFetcherLazy: Flow<MediaFetcher>,
    mediaSelectorFactory: MediaSelectorFactory,
    storagesLazy: Flow<List<MediaCacheStorage>>,
//    flowContext: CoroutineContext = Dispatchers.Default,
): EpisodeCacheRequester = EpisodeCacheRequesterImpl(
    mediaFetcherLazy, mediaSelectorFactory, storagesLazy,
//    flowContext
)

class EpisodeCacheRequesterImpl(
    private val mediaFetcherLazy: Flow<MediaFetcher>,
    private val mediaSelectorFactory: MediaSelectorFactory,
    private val storagesLazy: Flow<List<MediaCacheStorage>>,
//    private val flowContext: CoroutineContext = EmptyCoroutineContext,
//    private val enableCaching: Boolean = true,
) : EpisodeCacheRequester { // TODO: consider lifecycle of EpisodeCacheRequesterImpl
    override val stage = MutableStateFlow<CacheRequestStage>(CacheRequestStage.Idle)

    sealed class AbstractWorkingStage(
        override val request: EpisodeCacheRequest,
    ) : CacheRequestStage.Working

    sealed interface CloseableStage : CacheRequestStage, AutoCloseable {
        override fun close()
    }

    inner class SelectMedia(
        request: EpisodeCacheRequest,
        override val fetchSession: MediaFetchSession,
    ) : CacheRequestStage.SelectMedia, AbstractWorkingStage(request), CloseableStage {
        override val mediaSelector: MediaSelector by lazy {
            mediaSelectorFactory.create(
                request.subjectInfo.id,
                fetchSession.cumulativeResults
            )
        }

        override suspend fun select(media: Media): CacheRequestStage.SelectStorage {
            stageLock.withLock {
                checkStageLocked()
                mediaSelector.select(media) // always success
                return switchStageLocked {
                    SelectStorage(
                        this,
                        media,
                        storagesLazy.first()
                    )
                }
            }
        }

        override suspend fun tryAutoSelectByCachedSeason(existingCaches: List<MediaCache>): CacheRequestStage.SelectStorage? {
            val existing = existingCaches.firstOrNull {
                val episodeRange = it.origin.episodeRange
                if (episodeRange == null || episodeRange.isSingleEpisode()) return@firstOrNull false

                request.episodeInfo.ep != null && episodeRange.contains(request.episodeInfo.ep)
                        || episodeRange.contains(request.episodeInfo.sort)
            } ?: return null

            return stageLock.withLock {
                checkStageLocked()
                switchStageLocked {
                    SelectStorage(
                        this,
                        existing.origin.unwrapCached(),
                        storagesLazy.first()
                    )
                }
            }.apply {
                try {
                    trySelectByCache(existing)
                } catch (_: StaleStageException) {
                    // This can happen because we left lock before attempting trySelectByCache.
                    // It means someone else has already changed the stage. So we just ignore the exception.
                }
            }
        }

        override suspend fun tryAutoSelectByPreference(): CacheRequestStage.SelectStorage? {
            stageLock.withLock {
                checkStageLocked()
                val selected = mediaSelector.autoSelect.awaitCompletedAndSelectDefault(fetchSession)

                if (selected != null) {
                    return switchStageLocked {
                        SelectStorage(
                            this,
                            selected,
                            storagesLazy.first()
                        )
                    }
                }
                return null
            }
        }

        override suspend fun cancel(): CacheRequestStage.Idle {
            stageLock.withLock {
                checkStageLocked()
                return cancelRequestLocked(CacheRequestStage.Idle)
            }
        }

        override fun close() {
        }
    }

    inner class SelectStorage(
        private val previous: SelectMedia,
        private val selectedMedia: Media,
        override val storages: List<MediaCacheStorage>,
    ) : CacheRequestStage.SelectStorage, AbstractWorkingStage(previous.request), CloseableStage {

        override val fetchSession: MediaFetchSession get() = previous.fetchSession
        override val mediaSelector: MediaSelector get() = previous.mediaSelector

        override suspend fun select(storage: MediaCacheStorage): CacheRequestStage.Done {
            stageLock.withLock {
                checkStageLocked()
                return switchStageLocked {
                    CacheRequestStage.Done(
                        request, selectedMedia, storage,
                        MediaCacheMetadata(fetchSession.request.first())
                    )
                }.also {
                    this.close()
                }
            }
        }

        override suspend fun trySelectByCache(mediaCache: MediaCache): CacheRequestStage.Done? {
            stageLock.withLock {
                checkStageLocked()
                val storage = storages.firstOrNull { it.contains(mediaCache) } ?: return null
                return switchStageLocked {
                    CacheRequestStage.Done(
                        request, selectedMedia, storage,
                        MediaCacheMetadata(fetchSession.request.first())
                    )
                }.also {
                    this.close()
                }
            }
        }

        override suspend fun cancel(): CacheRequestStage.SelectMedia {
            stageLock.withLock {
                checkStageLocked()
                return cancelRequestLocked(previous)
            }
        }

        override fun close() {
            previous.close()
        }
    }

    private val stageLock = Mutex()

    override suspend fun request(request: EpisodeCacheRequest): CacheRequestStage.SelectMedia {
        stageLock.withLock {
            val current = stage.value
            current.close()
            val new = SelectMedia(
                request,
                mediaFetcherLazy.first().newSession(
                    MediaFetchRequest.Companion.create(request.subjectInfo, request.episodeInfo),
                )
            )
            stage.value = new
            return new
        }
    }

    override suspend fun cancelRequest() {
        stageLock.withLock {
            cancelRequestLocked(CacheRequestStage.Idle)
        }
    }

    private fun CacheRequestStage.close() {
        if (this is CloseableStage) close()
    }

    private fun <T : CacheRequestStage> cancelRequestLocked(previous: T): T {
        val current = stage.value
        current.close()
        stage.value = previous
        return previous
    }

    private fun CacheRequestStage.checkStageLocked() {
        val curr = stage.value
        if (curr !== this) throw StaleStageException(this, curr)
    }

    private inline fun <R : CacheRequestStage> CacheRequestStage.switchStageLocked(
        block: () -> R,
    ): R {
        checkStageLocked()
        return block().also {
            stage.value = it
        }
    }
}
