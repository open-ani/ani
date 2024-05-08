package me.him188.ani.app.data.subject

import androidx.compose.runtime.Stable
import androidx.compose.ui.util.fastDistinctBy
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.him188.ani.app.data.media.EpisodeCacheStatus
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.data.repositories.EpisodeRepository
import me.him188.ani.app.data.repositories.SubjectRepository
import me.him188.ani.app.data.repositories.setSubjectCollectionTypeOrDelete
import me.him188.ani.app.persistent.asDataStoreSerializer
import me.him188.ani.app.persistent.dataStores
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
import me.him188.ani.app.ui.collection.ContinueWatchingStatus
import me.him188.ani.app.ui.collection.EpisodeProgressItem
import me.him188.ani.datasources.api.paging.map
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.bangumi.processing.airSeason
import me.him188.ani.datasources.bangumi.processing.isOnAir
import me.him188.ani.datasources.bangumi.processing.nameCNOrName
import me.him188.ani.datasources.bangumi.processing.toCollectionType
import me.him188.ani.datasources.bangumi.processing.toEpisodeCollectionType
import me.him188.ani.datasources.bangumi.processing.toSubjectCollectionType
import me.him188.ani.utils.coroutines.runUntilSuccess
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.EpType
import org.openapitools.client.models.Episode
import org.openapitools.client.models.EpisodeCollectionType
import org.openapitools.client.models.EpisodeDetail
import org.openapitools.client.models.Subject
import org.openapitools.client.models.SubjectCollectionType
import org.openapitools.client.models.SubjectType
import org.openapitools.client.models.UserEpisodeCollection
import org.openapitools.client.models.UserSubjectCollection

/**
 * 管理收藏条目以及它们的内存缓存.
 */
interface SubjectManager {
    /**
     * 本地 subject 缓存
     */
    val collectionsByType: Map<UnifiedCollectionType, LazyDataCache<SubjectCollectionItem>>

    /**
     * 获取所有条目列表
     */
    fun subjectCollectionsFlow(contentPolicy: ContentPolicy): Flow<List<SubjectCollectionItem>> {
        return combine(collectionsByType.values.map { it.data(contentPolicy) }) { collections ->
            collections.asSequence().flatten().toList()
        }
    }

    /**
     * 获取指定条目的观看进度 flow. 进度还会包含该剧集的缓存状态 [EpisodeProgressItem.cacheStatus].
     */
    @Stable
    fun subjectProgressFlow(
        subjectId: Int,
        contentPolicy: ContentPolicy
    ): Flow<List<EpisodeProgressItem>>

    /**
     * 获取即时更新的收藏条目 flow.
     * @see subjectProgressFlow
     */
    // TODO: 如果 subjectId 没收藏, 这个函数的 flow 就会为空. 需要 (根据 policy) 实现为当未收藏时, 就向服务器请求单个 subjectId 的状态.
    //  这目前不是问题, 但在修改番剧详情页时可能会有问题.
    fun subjectCollectionFlow(
        subjectId: Int,
        contentPolicy: ContentPolicy
    ): Flow<SubjectCollectionItem?> =
        combine(collectionsByType.values.map { it.data(contentPolicy) }) { collections ->
            collections.asSequence().flatten().firstOrNull { it.subjectId == subjectId }
        }

    /**
     * 获取缓存的收藏条目. 注意, 这不会请求网络. 若缓存中不包含则返回 `null`.
     */
    suspend fun findCachedSubjectCollection(subjectId: Int): SubjectCollectionItem? {
        return collectionsByType.values.map { it.cachedDataFlow.first() }.asSequence().flatten()
            .firstOrNull { it.subjectId == subjectId }
    }

    /**
     * 从缓存中获取条目, 若没有则从网络获取.
     */
    suspend fun getSubjectName(subjectId: Int): String

    /**
     * 从缓存中获取剧集, 若没有则从网络获取.
     */
    suspend fun getEpisode(episodeId: Int): Episode

    fun episodeCollectionFlow(subjectId: Int, episodeId: Int, contentPolicy: ContentPolicy): Flow<UserEpisodeCollection>

    suspend fun setSubjectCollectionType(subjectId: Int, type: UnifiedCollectionType)

    suspend fun setAllEpisodesWatched(subjectId: Int)
    suspend fun setEpisodeCollectionType(subjectId: Int, episodeId: Int, collectionType: UnifiedCollectionType)
}

suspend inline fun SubjectManager.setEpisodeWatched(subjectId: Int, episodeId: Int, watched: Boolean) =
    setEpisodeCollectionType(
        subjectId,
        episodeId,
        if (watched) UnifiedCollectionType.DONE else UnifiedCollectionType.WISH
    )

class SubjectManagerImpl(
    context: Context
) : KoinComponent, SubjectManager {
    private val subjectRepository: SubjectRepository by inject()
    private val sessionManager: SessionManager by inject()
    private val episodeRepository: EpisodeRepository by inject()
    private val cacheManager: MediaCacheManager by inject()

    override val collectionsByType: Map<UnifiedCollectionType, LazyDataCache<SubjectCollectionItem>> =
        UnifiedCollectionType.entries.associateWith { type ->
            LazyDataCache(
                createSource = {
                    val username = sessionManager.username.filterNotNull().first()
                    subjectRepository.getSubjectCollections(
                        username,
                        subjectType = SubjectType.Anime,
                        subjectCollectionType = type.toSubjectCollectionType(),
                    ).map {
                        it.convertToItem()
                    }
                },
                sanitize = { list ->
                    list.fastDistinctBy { it.subjectId }
                },
                debugName = "collectionsByType-${type.name}",
                persistentStore = DataStoreFactory.create(
                    LazyDataCacheSave.serializer(SubjectCollectionItem.Serializer)
                        .asDataStoreSerializer(LazyDataCacheSave.empty()),
                    ReplaceFileCorruptionHandler { LazyDataCacheSave.empty() },
                    migrations = listOf(),
                    produceFile = {
                        context.dataStores.resolveDataStoreFile("collectionsByType-${type.name}")
                    }
                )
            )
        }

    @Stable
    override fun subjectProgressFlow(
        subjectId: Int,
        contentPolicy: ContentPolicy
    ): Flow<List<EpisodeProgressItem>> = subjectCollectionFlow(subjectId, contentPolicy)
        .map { it?._episodes ?: emptyList() }
        .distinctUntilChanged()
        .flatMapLatest { episodes ->
            combine(episodes.map { episode ->
                cacheManager.cacheStatusForEpisode(
                    subjectId = subjectId,
                    episodeId = episode.episode.id,
                ).onStart {
                    emit(EpisodeCacheStatus.NotCached)
                }.map { cacheStatus ->
                    EpisodeProgressItem(
                        episodeId = episode.episode.id,
                        episodeSort = episode.episode.sort.toString(),
                        watchStatus = episode.type.toCollectionType(),
                        isOnAir = episode.episode.isOnAir(),
                        cacheStatus = cacheStatus,
                    )
                }
            }) {
                it.toList()
            }
        }
        .flowOn(Dispatchers.Default)

    override suspend fun getSubjectName(subjectId: Int): String {
        findCachedSubjectCollection(subjectId)?.displayName?.let { return it }
        return runUntilSuccess {
            // TODO: we should unify how to compute display name from subject 
            subjectRepository.getSubject(subjectId)?.nameCNOrName() ?: error("Failed to get subject")
        }
    }

    override suspend fun getEpisode(episodeId: Int): Episode {
        collectionsByType.values.map { it.getCachedData() }.asSequence().flatten()
            .flatMap { it._episodes }
            .map { it.episode }
            .firstOrNull { it.id == episodeId }
            ?.let { return it }

        return runUntilSuccess {
            episodeRepository.getEpisodeById(episodeId)?.toEpisode() ?: error("Failed to get episode")
        }
    }

    override fun episodeCollectionFlow(
        subjectId: Int,
        episodeId: Int,
        contentPolicy: ContentPolicy
    ): Flow<UserEpisodeCollection> {
        return subjectCollectionFlow(subjectId, contentPolicy)
            .transform { subject ->
                if (subject == null) {
                    emit(me.him188.ani.utils.coroutines.runUntilSuccess {
                        episodeRepository.getEpisodeCollection(
                            episodeId
                        ) ?: error("Failed to get episode collection")
                    })
                } else {
                    emitAll(
                        subject._episodes.filter { it.episode.id == episodeId }.asFlow()
                    )
                }
            }.flowOn(Dispatchers.Default)
    }

    override suspend fun setSubjectCollectionType(subjectId: Int, type: UnifiedCollectionType) {
        val from = findSubjectCacheById(subjectId) ?: return // not found
        val target = collectionsByType[type] ?: return

        dataTransaction(from, target) { (f, t) ->
            val old = f.removeFirstOrNull { it.subjectId == subjectId } ?: return@dataTransaction
            t.addFirst(old)
        }

        subjectRepository.setSubjectCollectionTypeOrDelete(subjectId, type.toSubjectCollectionType())
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
                    episodes = _episodes.map { episode ->
                        episode.copy(type = EpisodeCollectionType.WATCHED)
                    }
                )
            }
        }

        val ids = episodeRepository.getEpisodesBySubjectId(subjectId, EpType.MainStory).map { it.id }.toList()
        episodeRepository.setEpisodeCollection(
            subjectId,
            ids,
            EpisodeCollectionType.WATCHED,
        )
    }

    override suspend fun setEpisodeCollectionType(
        subjectId: Int,
        episodeId: Int,
        collectionType: UnifiedCollectionType
    ) {
        val cache = findSubjectCacheById(subjectId) ?: return

        cache.mutate {
            setEach({ it.subjectId == subjectId }) {
                copy(episodes = _episodes.replaceAll({ it.episode.id == episodeId }) {
                    copy(type = collectionType.toEpisodeCollectionType())
                })
            }
        }

        episodeRepository.setEpisodeCollection(
            subjectId,
            listOf(episodeId),
            collectionType.toEpisodeCollectionType(),
        )
    }

    private suspend fun UserSubjectCollection.convertToItem() = coroutineScope {
        val subject = async {
            runUntilSuccess { subjectRepository.getSubject(subjectId) ?: error("Failed to get subject") }
        }
        val eps = runUntilSuccess {
            episodeRepository.getSubjectEpisodeCollection(subjectId, EpType.MainStory)
        }.toList()

        createItem(subject.await(), eps)
    }

    private fun UserSubjectCollection.createItem(
        subject: Subject,
        episodes: List<UserEpisodeCollection>,
    ): SubjectCollectionItem {
        if (subject.type != SubjectType.Anime) {
            return SubjectCollectionItem(
                subjectId = subjectId,
                displayName = this.subject?.nameCNOrName() ?: "",
                image = "",
                rate = this.rate,
                date = this.subject?.airSeason,
                totalEps = episodes.size,
                _episodes = episodes,
                collectionType = type.toCollectionType(),
            )
        }

        return SubjectCollectionItem(
            subjectId = subjectId,
            displayName = subject.nameCNOrName(),
            image = subject.images.common,
            rate = this.rate,
            date = subject.airSeason ?: "",
            totalEps = episodes.size,
            _episodes = episodes,
            collectionType = type.toCollectionType(),
        )
    }


}


@Stable
@Serializable
class SubjectCollectionItem(
    val subjectId: Int,
    val displayName: String,
    val image: String,
    val rate: Int?,

    val date: String?,
    val totalEps: Int,

    val _episodes: List<@Serializable(UserEpisodeCollectionSerializer::class) UserEpisodeCollection>,
    val collectionType: UnifiedCollectionType,
) {
    @Transient
    val isOnAir = run {
        _episodes.firstOrNull { it.episode.isOnAir() == true } != null
    }

    @Transient
    val lastWatchedEpIndex = run {
        _episodes.indexOfLast {
            it.type == EpisodeCollectionType.WATCHED || it.type == EpisodeCollectionType.DISCARDED
        }.takeIf { it != -1 }
    }

    @Transient
    val latestEp = run {
        _episodes.lastOrNull { it.episode.isOnAir() == false }
            ?: _episodes.lastOrNull { it.episode.isOnAir() != true }
    }

    /**
     * 是否已经开播了第一集
     */
    @Transient
    val hasStarted = _episodes.firstOrNull()?.episode?.isOnAir() == false

    @Transient
    val episodes: List<UserEpisodeCollection> = _episodes.sortedBy { it.episode.sort }

    @Transient
    val latestEpIndex: Int? = _episodes.indexOfFirst { it.episode.id == latestEp?.episode?.id }
        .takeIf { it != -1 }
        ?: _episodes.lastIndex.takeIf { it != -1 }

    @Transient
    val onAirDescription = if (isOnAir) {
        if (latestEp == null) {
            "连载中"
        } else {
            "连载至第 ${latestEp.episode.sort} 话"
        }
    } else {
        "已完结"
    }

    @Transient
    val serialProgress = "全 $totalEps 话"

    @Transient
    val continueWatchingStatus = run {
        val item = this
        when (item.lastWatchedEpIndex) {
            // 还没看过
            null -> {
                if (item.hasStarted) {
                    ContinueWatchingStatus.Start
                } else {
                    ContinueWatchingStatus.NotOnAir
                }
            }

            // 看了第 n 集并且还有第 n+1 集
            in 0..<item.totalEps - 1 -> {
                if (item.latestEpIndex != null && item.lastWatchedEpIndex < item.latestEpIndex) {
                    // 更新了 n+1 集
                    ContinueWatchingStatus.Continue(
                        item.lastWatchedEpIndex + 1,
                        _episodes.getOrNull(item.lastWatchedEpIndex + 1)?.episode?.sort?.toString() ?: ""
                    )
                } else {
                    // 还没更新
                    ContinueWatchingStatus.Watched(
                        item.lastWatchedEpIndex,
                        _episodes.getOrNull(item.lastWatchedEpIndex)?.episode?.sort?.toString() ?: ""
                    )
                }
            }

            else -> {
                ContinueWatchingStatus.Done
            }
        }
    }

    fun copy(
        subjectId: Int = this.subjectId,
        displayName: String = this.displayName,
        image: String = this.image,
        rate: Int? = this.rate,
        date: String? = this.date,
        totalEps: Int = this.totalEps,
        episodes: List<UserEpisodeCollection> = this._episodes,
        collectionType: UnifiedCollectionType = this.collectionType,
    ) = SubjectCollectionItem(
        subjectId = subjectId,
        displayName = displayName,
        image = image,
        rate = rate,
        date = date,
        totalEps = totalEps,
        _episodes = episodes,
        collectionType = collectionType,
    )

    object Serializer : KSerializer<SubjectCollectionItem> {
        @Serializable
        private class Delegate(
            val subjectId: Int,
            val displayName: String,
            val image: String,
            val rate: Int?,
            val date: String?,
            val totalEps: Int,
            val episodes: List<@Serializable(UserEpisodeCollectionSerializer::class) UserEpisodeCollection>,
            val collectionType: SubjectCollectionType?,
        )

        override val descriptor: SerialDescriptor get() = Delegate.serializer().descriptor

        override fun deserialize(decoder: Decoder): SubjectCollectionItem {
            val delegate = Delegate.serializer().deserialize(decoder)
            return SubjectCollectionItem(
                subjectId = delegate.subjectId,
                displayName = delegate.displayName,
                image = delegate.image,
                rate = delegate.rate,
                date = delegate.date,
                totalEps = delegate.totalEps,
                _episodes = delegate.episodes,
                collectionType = delegate.collectionType.toCollectionType(),
            )
        }

        override fun serialize(encoder: Encoder, value: SubjectCollectionItem) {
            Delegate.serializer().serialize(
                encoder,
                Delegate(
                    subjectId = value.subjectId,
                    displayName = value.displayName,
                    image = value.image,
                    rate = value.rate,
                    date = value.date,
                    totalEps = value.totalEps,
                    episodes = value._episodes,
                    collectionType = value.collectionType.toSubjectCollectionType(),
                )
            )
        }
    }
}


private fun EpisodeDetail.toEpisode(): Episode {
    return Episode(
        id = id,
        type = type,
        name = name,
        nameCn = nameCn,
        sort = sort,
        airdate = airdate,
        comment = comment,
        duration = duration,
        desc = desc,
        disc = disc,
        ep = ep,
    )
}