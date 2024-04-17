package me.him188.ani.app.data.subject

import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.data.repositories.EpisodeRepository
import me.him188.ani.app.data.repositories.SubjectRepository
import me.him188.ani.app.data.repositories.setSubjectCollectionTypeOrDelete
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.tools.caching.LazyDataCache
import me.him188.ani.app.tools.caching.MutationContext.replaceAll
import me.him188.ani.app.tools.caching.addFirst
import me.him188.ani.app.tools.caching.dataTransaction
import me.him188.ani.app.tools.caching.mutate
import me.him188.ani.app.tools.caching.removeFirstOrNull
import me.him188.ani.app.tools.caching.setEach
import me.him188.ani.app.tools.caching.value
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
import org.openapitools.client.models.EpisodeCollectionType
import org.openapitools.client.models.Subject
import org.openapitools.client.models.SubjectCollectionType
import org.openapitools.client.models.SubjectType
import org.openapitools.client.models.UserEpisodeCollection
import org.openapitools.client.models.UserSubjectCollection

/**
 * 管理收藏条目以及它们的内存缓存.
 */
interface SubjectManager {
    val collectionsByType: Map<UnifiedCollectionType, LazyDataCache<SubjectCollectionItem>>

    @Stable
    fun subjectProgressFlow(item: SubjectCollectionItem): Flow<List<EpisodeProgressItem>>

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

class SubjectManagerImpl : KoinComponent, SubjectManager {
    private val subjectRepository: SubjectRepository by inject()
    private val sessionManager: SessionManager by inject()
    private val episodeRepository: EpisodeRepository by inject()
    private val cacheManager: MediaCacheManager by inject()

    override val collectionsByType: Map<UnifiedCollectionType, LazyDataCache<SubjectCollectionItem>> =
        UnifiedCollectionType.entries.associateWith { type ->
            LazyDataCache(
                {
                    val username = sessionManager.username.filterNotNull().first()
                    subjectRepository.getSubjectCollections(
                        username,
                        subjectType = SubjectType.Anime,
                        subjectCollectionType = type.toSubjectCollectionType(),
                    ).map {
                        it.convertToItem()
                    }
                },
                debugName = "collectionsByType-${type.name}"
            )
        }


    @Stable
    override fun subjectProgressFlow(item: SubjectCollectionItem): Flow<List<EpisodeProgressItem>> {
        return snapshotFlow { item.episodes }
            .flowOn(Dispatchers.Main)
            .flatMapLatest { episodes ->
                combine(episodes.map { episode ->
                    cacheManager.cacheStatusForEpisode(
                        subjectId = item.subjectId,
                        episodeId = episode.episode.id,
                    ).map { cacheStatus ->
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
    private fun findSubjectCacheById(subjectId: Int) =
        collectionsByType.values.firstOrNull { list -> list.value.any { it.subjectId == subjectId } }

    override suspend fun setAllEpisodesWatched(subjectId: Int) {
        val cache = findSubjectCacheById(subjectId) ?: return
        cache.mutate {
            setEach({ it.subjectId == subjectId }) {
                copy(
                    episodes = episodes.map { episode ->
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
                copy(episodes = episodes.replaceAll({ it.episode.id == episodeId }) {
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
            runUntilSuccess { subjectRepository.getSubject(subjectId) }
        }
        val eps = runUntilSuccess {
            episodeRepository.getSubjectEpisodeCollection(subjectId, EpType.MainStory)
        }.toList()

        createItem(subject.await(), eps)
    }

    private fun UserSubjectCollection.createItem(
        subject: Subject?,
        episodes: List<UserEpisodeCollection>,
    ): SubjectCollectionItem {
        if (subject == null || subject.type != SubjectType.Anime) {
            return SubjectCollectionItem(
                subjectId = subjectId,
                displayName = this.subject?.nameCNOrName() ?: "",
                image = "",
                rate = this.rate,
                date = this.subject?.airSeason,
                totalEps = episodes.size,
                episodes = episodes,
                collectionType = type,
            )
        }

        return SubjectCollectionItem(
            subjectId = subjectId,
            displayName = this.subject?.nameCNOrName() ?: "",
            image = this.subject?.images?.common ?: "",
            rate = this.rate,
            date = subject.airSeason ?: "",
            totalEps = episodes.size,
            episodes = episodes,
            collectionType = type,
        )
    }


}


@Stable
class SubjectCollectionItem(
    val subjectId: Int,
    val displayName: String,
    val image: String,
    val rate: Int?,

    val date: String?,
    val totalEps: Int,

    episodes: List<UserEpisodeCollection>,
    collectionType: SubjectCollectionType?,
) {
    val isOnAir = run {
        episodes.firstOrNull { it.episode.isOnAir() == true } != null
    }
    val lastWatchedEpIndex = run {
        episodes.indexOfLast {
            it.type == EpisodeCollectionType.WATCHED || it.type == EpisodeCollectionType.DISCARDED
        }.takeIf { it != -1 }
    }
    val latestEp = run {
        episodes.lastOrNull { it.episode.isOnAir() == false }
            ?: episodes.lastOrNull { it.episode.isOnAir() != true }
    }

    /**
     * 是否已经开播了第一集
     */
    val hasStarted = episodes.firstOrNull()?.episode?.isOnAir() == false

    val collectionType: UnifiedCollectionType = collectionType.toCollectionType()
    val episodes: List<UserEpisodeCollection> = episodes.sortedBy { it.episode.sort }

    val latestEpIndex: Int? = episodes.indexOfFirst { it.episode.id == latestEp?.episode?.id }
        .takeIf { it != -1 }
        ?: episodes.lastIndex.takeIf { it != -1 }

    val onAirDescription = if (isOnAir) {
        if (latestEp == null) {
            "连载中"
        } else {
            "连载至第 ${latestEp.episode.sort} 话"
        }
    } else {
        "已完结"
    }

    val serialProgress = "全 $totalEps 话"

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
                        episodes.getOrNull(item.lastWatchedEpIndex + 1)?.episode?.sort?.toString() ?: ""
                    )
                } else {
                    // 还没更新
                    ContinueWatchingStatus.Watched(
                        item.lastWatchedEpIndex,
                        episodes.getOrNull(item.lastWatchedEpIndex)?.episode?.sort?.toString() ?: ""
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
        episodes: List<UserEpisodeCollection> = this.episodes,
        collectionType: SubjectCollectionType? = this.collectionType.toSubjectCollectionType(),
    ) = SubjectCollectionItem(
        subjectId = subjectId,
        displayName = displayName,
        image = image,
        rate = rate,
        date = date,
        totalEps = totalEps,
        episodes = episodes,
        collectionType = collectionType,
    )
}
