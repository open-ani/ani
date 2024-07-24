package me.him188.ani.app.data.models.subject

import androidx.compose.runtime.Stable
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import me.him188.ani.app.data.models.episode.EpisodeCollection
import me.him188.ani.app.data.models.episode.EpisodeCollections
import me.him188.ani.app.data.models.episode.EpisodeInfo
import me.him188.ani.app.data.models.episode.episode
import me.him188.ani.app.data.models.episode.isKnownOnAir
import me.him188.ani.app.data.models.episode.type
import me.him188.ani.app.data.persistent.asDataStoreSerializer
import me.him188.ani.app.data.persistent.dataStores
import me.him188.ani.app.data.repository.BangumiEpisodeRepository
import me.him188.ani.app.data.repository.BangumiSubjectRepository
import me.him188.ani.app.data.repository.setSubjectCollectionTypeOrDelete
import me.him188.ani.app.data.repository.toEpisodeCollection
import me.him188.ani.app.data.repository.toEpisodeInfo
import me.him188.ani.app.data.repository.toSelfRatingInfo
import me.him188.ani.app.data.repository.toSubjectCollectionItem
import me.him188.ani.app.data.repository.toSubjectInfo
import me.him188.ani.app.data.source.media.EpisodeCacheStatus
import me.him188.ani.app.data.source.media.MediaCacheManager
import me.him188.ani.app.platform.Context
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.tools.caching.ContentPolicy
import me.him188.ani.app.tools.caching.LazyDataCache
import me.him188.ani.app.tools.caching.LazyDataCacheSave
import me.him188.ani.app.tools.caching.MutationContext.replaceAll
import me.him188.ani.app.tools.caching.addFirst
import me.him188.ani.app.tools.caching.data
import me.him188.ani.app.tools.caching.dataTransaction
import me.him188.ani.app.tools.caching.getCachedData
import me.him188.ani.app.tools.caching.mutate
import me.him188.ani.app.tools.caching.removeFirstOrNull
import me.him188.ani.app.tools.caching.setEach
import me.him188.ani.app.ui.subject.collection.progress.EpisodeProgressItem
import me.him188.ani.datasources.api.paging.emptyPagedSource
import me.him188.ani.datasources.api.paging.mapNotNull
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.bangumi.models.BangumiEpType
import me.him188.ani.datasources.bangumi.models.BangumiEpisodeCollectionType
import me.him188.ani.datasources.bangumi.models.BangumiSubjectType
import me.him188.ani.datasources.bangumi.models.BangumiUserSubjectCollection
import me.him188.ani.datasources.bangumi.models.BangumiUserSubjectCollectionModifyPayload
import me.him188.ani.datasources.bangumi.processing.toCollectionType
import me.him188.ani.datasources.bangumi.processing.toEpisodeCollectionType
import me.him188.ani.datasources.bangumi.processing.toSubjectCollectionType
import me.him188.ani.utils.coroutines.flows.runOrEmitEmptyList
import me.him188.ani.utils.coroutines.runUntilSuccess
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 管理收藏条目以及它们的缓存.
 */
abstract class SubjectManager {
    ///////////////////////////////////////////////////////////////////////////
    // Subject collections
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 本地 subject 缓存
     */
    abstract val collectionsByType: Map<UnifiedCollectionType, LazyDataCache<SubjectCollection>>

    /**
     * 获取所有收藏的条目列表
     */
    fun subjectCollectionsFlow(contentPolicy: ContentPolicy): Flow<List<SubjectCollection>> {
        return combine(collectionsByType.values.map { it.data(contentPolicy) }) { collections ->
            collections.asSequence().flatten().toList()
        }
    }

    /**
     * 获取某一个收藏条目 flow.
     * @see subjectProgressFlow
     */
    fun cachedSubjectCollectionFlow(
        subjectId: Int,
        contentPolicy: ContentPolicy
    ): Flow<SubjectCollection?> =
        combine(collectionsByType.values.map { it.data(contentPolicy) }) { collections ->
            collections.asSequence().flatten().firstOrNull { it.subjectId == subjectId }
        }

    abstract fun subjectCollectionFlow(
        subjectId: Int,
        contentPolicy: ContentPolicy
    ): Flow<SubjectCollection?>

    /**
     * 获取缓存的收藏条目. 注意, 这不会请求网络. 若缓存中不包含则返回 `null`.
     */
    suspend fun findCachedSubjectCollection(subjectId: Int): SubjectCollection? {
        return collectionsByType.values.map { it.cachedDataFlow.first() }.asSequence().flatten()
            .firstOrNull { it.subjectId == subjectId }
    }

    /**
     * 获取缓存的对该条目的收藏状态, 若没有则从网络获取.
     */
    abstract fun subjectCollectionTypeFlow(subjectId: Int): Flow<UnifiedCollectionType>

    /**
     * 获取缓存的对该条目的收藏信息, 若没有则从网络获取. `null` 表示用户未收藏该条目.
     */
    abstract fun subjectCollectionFlow(subjectId: Int): Flow<SubjectCollection?>

    /**
     * 从缓存中获取条目, 若没有则从网络获取.
     */
    abstract suspend fun getSubjectInfo(subjectId: Int): SubjectInfo // TODO: replace with  subjectInfoFlow


    fun subjectInfoFlow(subjectId: Flow<Int>): Flow<SubjectInfo> {
        return subjectId.mapLatest { getSubjectInfo(it) }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Subject progress
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 获取指定条目的观看进度 flow. 进度还会包含该剧集的缓存状态 [EpisodeProgressItem.cacheStatus].
     */
    @Stable
    abstract fun subjectProgressFlow(
        subjectId: Int,
        contentPolicy: ContentPolicy
    ): Flow<List<EpisodeProgressItem>>

    /**
     * 获取用户该条目的收藏情况, 以及该条目的信息.
     *
     * 将会优先查询缓存, 若本地没有缓存, 则会执行一次网络请求.
     * 返回的 flow 不会完结, 将会跟随缓存更新.
     *
     * 如果用户未收藏该条目, 则返回空列表.
     */
    abstract fun episodeCollectionsFlow(subjectId: Int): Flow<List<EpisodeCollection>>

    /**
     * 获取指定条目下指定剧集的收藏情况 flow.
     */
    abstract fun episodeCollectionFlow(
        subjectId: Int,
        episodeId: Int,
        contentPolicy: ContentPolicy
    ): Flow<EpisodeCollection>

    abstract suspend fun setSubjectCollectionType(subjectId: Int, type: UnifiedCollectionType)

    abstract suspend fun setAllEpisodesWatched(subjectId: Int)

    abstract suspend fun setEpisodeCollectionType(subjectId: Int, episodeId: Int, collectionType: UnifiedCollectionType)

    ///////////////////////////////////////////////////////////////////////////
    // Get info
    ///////////////////////////////////////////////////////////////////////////
    // TODO: extract EpisodeRepository  (remote/mixed(?))

    /**
     * 从缓存中获取剧集, 若没有则从网络获取. 在获取失败时将会抛出异常.
     */
    abstract suspend fun getEpisodeInfo(episodeId: Int): EpisodeInfo // TODO: replace with  episodeInfoFlow

    /**
     * 获取一个 [EpisodeInfo] flow. 将优先从缓存中获取, 若没有则从网络获取.
     *
     * 返回的 flow 只会 emit 唯一一个元素, 或者抛出异常.
     */
    fun episodeInfoFlow(episodeId: Flow<Int>): Flow<EpisodeInfo> = episodeId.mapLatest { getEpisodeInfo(it) }

    ///////////////////////////////////////////////////////////////////////////
    // Rating
    ///////////////////////////////////////////////////////////////////////////

    abstract suspend fun updateRating(
        subjectId: Int,
        score: Int? = null, // 0 to remove rating
        comment: String? = null, // set empty to remove
        tags: List<String>? = null,
        isPrivate: Boolean? = null
    )
}

/**
 * 获取一个 [SubjectInfo] flow. 将优先从缓存中获取, 若没有则从网络获取.
 *
 * 返回的 flow 只会 emit 唯一一个元素, 或者抛出异常.
 */
fun SubjectManager.subjectInfoFlow(subjectId: Int): Flow<SubjectInfo> = subjectInfoFlow(flowOf(subjectId))

/**
 * 获取一个 [EpisodeInfo] flow. 将优先从缓存中获取, 若没有则从网络获取.
 *
 * 返回的 flow 只会 emit 唯一一个元素, 或者抛出异常.
 */
fun SubjectManager.episodeInfoFlow(episodeId: Int): Flow<EpisodeInfo> = episodeInfoFlow(flowOf(episodeId))

/**
 * 获取指定条目是否已经完结. 不是用户是否看完, 只要条目本身完结了就算.
 */
fun SubjectManager.subjectCompletedFlow(subjectId: Int): Flow<Boolean> {
    return episodeCollectionsFlow(subjectId).map { epCollection ->
        EpisodeCollections.isSubjectCompleted(epCollection.map { it.episode })
    }
}

suspend inline fun SubjectManager.setEpisodeWatched(subjectId: Int, episodeId: Int, watched: Boolean) =
    setEpisodeCollectionType(
        subjectId,
        episodeId,
        if (watched) UnifiedCollectionType.DONE else UnifiedCollectionType.WISH,
    )

class SubjectManagerImpl(
    context: Context
) : KoinComponent, SubjectManager() {
    private val bangumiSubjectRepository: BangumiSubjectRepository by inject()
    private val bangumiEpisodeRepository: BangumiEpisodeRepository by inject()

    private val sessionManager: SessionManager by inject()
    private val cacheManager: MediaCacheManager by inject()

    override val collectionsByType: Map<UnifiedCollectionType, LazyDataCache<SubjectCollection>> =
        UnifiedCollectionType.entries.associateWith { type ->
            LazyDataCache(
                createSource = {
                    val username = sessionManager.username.firstOrNull() ?: return@LazyDataCache emptyPagedSource()
                    bangumiSubjectRepository.getSubjectCollections(
                        username,
                        subjectType = BangumiSubjectType.Anime,
                        subjectCollectionType = type.toSubjectCollectionType(),
                    ).mapNotNull {
                        it.fetchToSubjectCollection()
                    }
                },
                getKey = { it.subjectId },
                debugName = "collectionsByType-${type.name}",
                persistentStore = DataStoreFactory.create(
                    LazyDataCacheSave.serializer(SubjectCollection.serializer())
                        .asDataStoreSerializer(LazyDataCacheSave.empty()),
                    ReplaceFileCorruptionHandler { LazyDataCacheSave.empty() },
                    migrations = listOf(),
                    produceFile = {
                        context.dataStores.resolveDataStoreFile("collectionsByType-${type.name}")
                    },
                ),
            )
        }

    override fun subjectCollectionFlow(subjectId: Int, contentPolicy: ContentPolicy): Flow<SubjectCollection?> {
        return flow {
            coroutineScope {
                val cached = findCachedSubjectCollection(subjectId)
                if (cached == null) {
                    emit(fetchSubjectCollection(subjectId))
                }
                emitAll(cachedSubjectCollectionFlow(subjectId, ContentPolicy.CACHE_ONLY)) // subscribe to cache updates
            }
        }
    }

    override fun subjectCollectionFlow(subjectId: Int): Flow<SubjectCollection?> {
        return flow {
            coroutineScope {
                val cached = findCachedSubjectCollection(subjectId)
                // TODO: this is shit 
                if (cached == null) {
                    emit(
                        bangumiSubjectRepository.subjectCollectionById(subjectId).first()
                            ?.fetchToSubjectCollection(),
                    )
                }
                emitAll(
                    cachedSubjectCollectionFlow(subjectId, ContentPolicy.CACHE_ONLY)
                        .filterNotNull(),
                )
            }
        }
    }

    override fun subjectCollectionTypeFlow(subjectId: Int): Flow<UnifiedCollectionType> {
        return flow {
            coroutineScope {
                val cached = findCachedSubjectCollection(subjectId)
                if (cached == null) {
                    emit(bangumiSubjectRepository.subjectCollectionTypeById(subjectId).first())
                }
                emitAll(
                    cachedSubjectCollectionFlow(subjectId, ContentPolicy.CACHE_ONLY)
                        .filterNotNull().map { it.collectionType },
                )
            }
        }
    }

    @Stable
    override fun subjectProgressFlow(
        subjectId: Int,
        contentPolicy: ContentPolicy
    ): Flow<List<EpisodeProgressItem>> = subjectCollectionFlow(subjectId, contentPolicy)
        .map { it?.episodes ?: emptyList() }
        .distinctUntilChanged()
        .flatMapLatest { episodes ->
            combine(
                episodes.map { episode ->
                    cacheManager.cacheStatusForEpisode(
                        subjectId = subjectId,
                        episodeId = episode.episode.id,
                    ).onStart {
                        emit(EpisodeCacheStatus.NotCached)
                    }.map { cacheStatus ->
                        EpisodeProgressItem(
                            episodeId = episode.episode.id,
                            episodeSort = episode.episode.sort.toString(),
                            watchStatus = episode.type,
                            isOnAir = episode.episode.isKnownOnAir,
                            cacheStatus = cacheStatus,
                        )
                    }
                },
            ) {
                it.toList()
            }
        }
        .flowOn(Dispatchers.Default)

    override fun episodeCollectionsFlow(subjectId: Int): Flow<List<EpisodeCollection>> {
        return flow {
            if (findCachedSubjectCollection(subjectId) == null) {
                // 这是网络请求, 无网情况下会一直失败
                emit(
                    runOrEmitEmptyList {
                        fetchEpisodeCollections(subjectId)
                    },
                )
            }
            // subscribe to changes
            emitAll(
                cachedSubjectCollectionFlow(subjectId, ContentPolicy.CACHE_ONLY).filterNotNull().map { it.episodes },
            )
        }
    }

    override suspend fun getSubjectInfo(subjectId: Int): SubjectInfo {
        findCachedSubjectCollection(subjectId)?.info?.let { return it }
        return runUntilSuccess {
            // TODO: we should unify how to compute display name from subject 
            bangumiSubjectRepository.getSubject(subjectId)?.toSubjectInfo() ?: error("Failed to get subject")
        }
    }

    override suspend fun getEpisodeInfo(episodeId: Int): EpisodeInfo {
        collectionsByType.values.map { it.getCachedData() }.asSequence().flatten()
            .flatMap { it.episodes }
            .map { it.episode }
            .firstOrNull { it.id == episodeId }
            ?.let { return it }

        return runUntilSuccess {
            bangumiEpisodeRepository.getEpisodeById(episodeId)?.toEpisodeInfo() ?: error("Failed to get episode")
        }
    }

    override suspend fun updateRating(
        subjectId: Int,
        score: Int?,
        comment: String?,
        tags: List<String>?,
        isPrivate: Boolean?
    ) {
        bangumiSubjectRepository.patchSubjectCollection(
            subjectId,
            BangumiUserSubjectCollectionModifyPayload(
                rate = score,
                comment = comment,
                tags = tags,
                private = isPrivate,
            ),
        )

        findSubjectCacheById(subjectId)?.mutate {
            setEach({ it.subjectId == subjectId }) {
                copy(
                    selfRatingInfo = SelfRatingInfo(
                        score = score ?: selfRatingInfo.score,
                        comment = comment ?: selfRatingInfo.comment,
                        tags = tags ?: selfRatingInfo.tags,
                        isPrivate = isPrivate ?: selfRatingInfo.isPrivate,
                    ),
                )
            }
        }
    }

    override fun episodeCollectionFlow(
        subjectId: Int,
        episodeId: Int,
        contentPolicy: ContentPolicy
    ): Flow<EpisodeCollection> {
        return subjectCollectionFlow(subjectId, contentPolicy)
            .transform { subject ->
                if (subject == null) {
                    emit(
                        runUntilSuccess {
                            bangumiEpisodeRepository.getEpisodeCollection(
                                episodeId,
                            )?.toEpisodeCollection()
                                ?: bangumiEpisodeRepository.getEpisodeById(episodeId)?.let {
                                    EpisodeCollection(it.toEpisodeInfo(), UnifiedCollectionType.NOT_COLLECTED)
                                }
                                ?: error("Failed to get episode collection")
                        },
                    )
                } else {
                    emitAll(
                        subject.episodes.filter { it.episode.id == episodeId }.asFlow(),
                    )
                }
            }
            .flowOn(Dispatchers.Default)
    }

    override suspend fun setSubjectCollectionType(subjectId: Int, type: UnifiedCollectionType) {
        bangumiSubjectRepository.setSubjectCollectionTypeOrDelete(subjectId, type.toSubjectCollectionType())

        val from = findSubjectCacheById(subjectId)
        val target = collectionsByType[type]!!
        if (from != null) {
            // 有缓存, 更新缓存
            dataTransaction(from, target) { (f, t) ->
                val old = f.removeFirstOrNull { it.subjectId == subjectId } ?: return@dataTransaction
                t.addFirst(
                    old.copy(collectionType = type),
                )
            }
        } else {
            // 无缓存, 添加
            target.mutate {
                addFirst(fetchSubjectCollection(subjectId))
            }
        }
    }

    /**
     * Finds the cache that contains the subject.
     */
    private suspend fun findSubjectCacheById(subjectId: Int) =
        collectionsByType.values.firstOrNull { list -> list.getCachedData().any { it.subjectId == subjectId } }

    override suspend fun setAllEpisodesWatched(subjectId: Int) {
        val cache = findSubjectCacheById(subjectId) ?: return
        cache.mutate {
            setEach({ it.subjectId == subjectId }) {
                copy(
                    episodes = episodes.map { episode ->
                        episode.copy(collectionType = UnifiedCollectionType.DONE)
                    },
                )
            }
        }

        val ids =
            bangumiEpisodeRepository.getEpisodesBySubjectId(subjectId, BangumiEpType.MainStory).map { it.id }.toList()
        bangumiEpisodeRepository.setEpisodeCollection(
            subjectId,
            ids,
            BangumiEpisodeCollectionType.WATCHED,
        )
    }

    override suspend fun setEpisodeCollectionType(
        subjectId: Int,
        episodeId: Int,
        collectionType: UnifiedCollectionType
    ) {
        bangumiEpisodeRepository.setEpisodeCollection(
            subjectId,
            listOf(episodeId),
            collectionType.toEpisodeCollectionType(),
        )

        val cache = findSubjectCacheById(subjectId) ?: return

        cache.mutate {
            setEach({ it.subjectId == subjectId }) {
                copy(
                    episodes = episodes.replaceAll({ it.episode.id == episodeId }) {
                        copy(collectionType = collectionType)
                    },
                )
            }
        }
    }

    private suspend fun fetchSubjectCollection(
        subjectId: Int,
    ): SubjectCollection {
        val info = getSubjectInfo(subjectId)

        // 查收藏状态, 没收藏就查剧集, 认为所有剧集都没有收藏
        val episodes: List<EpisodeCollection> = fetchEpisodeCollections(subjectId)

        val collection = bangumiSubjectRepository.subjectCollectionById(subjectId).first()

        return SubjectCollection(
            info, episodes,
            collectionType = collection?.type?.toCollectionType()
                ?: UnifiedCollectionType.NOT_COLLECTED,
            selfRatingInfo = collection?.toSelfRatingInfo() ?: SelfRatingInfo.Empty,
        )
    }

    private suspend fun fetchEpisodeCollections(subjectId: Int): List<EpisodeCollection> {
        return bangumiEpisodeRepository.getSubjectEpisodeCollections(subjectId, BangumiEpType.MainStory)
            .let { collections ->
                collections?.toList()?.map { it.toEpisodeCollection() }
                    ?: bangumiEpisodeRepository.getEpisodesBySubjectId(subjectId, BangumiEpType.MainStory)
                        .toList()
                        .map {
                            EpisodeCollection(it.toEpisodeInfo(), UnifiedCollectionType.NOT_COLLECTED)
                        }
            }
    }

    private suspend fun BangumiUserSubjectCollection.fetchToSubjectCollection(): SubjectCollection? = coroutineScope {
        val subject = async {
            runUntilSuccess { bangumiSubjectRepository.getSubject(subjectId) ?: error("Failed to get subject") }
        }
        val eps = runUntilSuccess {
            fetchEpisodeCollections(subjectId)
        }.toList()

        toSubjectCollectionItem(subject.await(), eps)
    }

}


