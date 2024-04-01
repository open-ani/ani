package me.him188.ani.app.ui.collection

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import me.him188.ani.app.ViewModelAuthSupport
import me.him188.ani.app.data.EpisodeRepository
import me.him188.ani.app.data.SubjectRepository
import me.him188.ani.app.data.setSubjectCollectionTypeOrDelete
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.tools.caching.LazyDataCache
import me.him188.ani.app.tools.caching.cached
import me.him188.ani.app.tools.caching.value
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.runUntilSuccess
import me.him188.ani.datasources.api.UnifiedCollectionType
import me.him188.ani.datasources.api.map
import me.him188.ani.datasources.bangumi.processing.airSeason
import me.him188.ani.datasources.bangumi.processing.isOnAir
import me.him188.ani.datasources.bangumi.processing.nameCNOrName
import me.him188.ani.datasources.bangumi.processing.toCollectionType
import me.him188.ani.datasources.bangumi.processing.toSubjectCollectionType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.EpType
import org.openapitools.client.models.EpisodeCollectionType
import org.openapitools.client.models.Subject
import org.openapitools.client.models.SubjectCollectionType
import org.openapitools.client.models.SubjectType
import org.openapitools.client.models.UserEpisodeCollection
import org.openapitools.client.models.UserSubjectCollection

interface MyCollectionsViewModel : HasBackgroundScope, ViewModelAuthSupport {
    @Stable
    fun collectionsByType(type: UnifiedCollectionType): LazyDataCache<SubjectCollectionItem>

    suspend fun setCollectionType(subjectId: Int, type: UnifiedCollectionType)

    suspend fun setAllEpisodesWatched(subjectId: Int)

    suspend fun setEpisodeWatched(subjectId: Int, episodeId: Int, watched: Boolean)
}

fun MyCollectionsViewModel(): MyCollectionsViewModel = MyCollectionsViewModelImpl()

class MyCollectionsViewModelImpl : AbstractViewModel(), KoinComponent, MyCollectionsViewModel {
    private val sessionManager: SessionManager by inject()
    private val subjectRepository: SubjectRepository by inject()
    private val episodeRepository: EpisodeRepository by inject()

    @Stable
    val collectionsByType = UnifiedCollectionType.entries.associateWith { type ->
        sessionManager.username.filterNotNull().map { username ->
            subjectRepository.getSubjectCollections(
                username,
                subjectCollectionType = type.toSubjectCollectionType(),
            ).map {
                it.convertToItem()
            }
        }.cached()
    }

    @Stable
    override fun collectionsByType(type: UnifiedCollectionType) = collectionsByType[type]!!

    private suspend fun UserSubjectCollection.convertToItem() = coroutineScope {
        val subject = async {
            runUntilSuccess { subjectRepository.getSubject(subjectId) }
        }
        val eps = runUntilSuccess {
            episodeRepository.getSubjectEpisodeCollection(subjectId, EpType.MainStory)
        }.toList()
        val isOnAir = async {
            eps.firstOrNull { it.episode.isOnAir() == true } != null
        }
        val lastWatchedEp = async {
            eps.indexOfLast {
                it.type == EpisodeCollectionType.WATCHED || it.type == EpisodeCollectionType.DISCARDED
            }.takeIf { it != -1 }
        }
        val latestEp = async {
            eps.lastOrNull { it.episode.isOnAir() == false }
                ?: eps.lastOrNull { it.episode.isOnAir() != true }
        }

        createItem(subject.await(), isOnAir.await(), latestEp.await(), lastWatchedEp.await(), eps)
    }

    override suspend fun setCollectionType(subjectId: Int, type: UnifiedCollectionType) {
        val cache = findContainingCache(subjectId) ?: return // not found
        cache.mutate {
            map { item ->
                if (item.subjectId == subjectId) {
                    item.copy(collectionType = type.toSubjectCollectionType())
                } else {
                    item
                }
            }
        }
        subjectRepository.setSubjectCollectionTypeOrDelete(subjectId, type.toSubjectCollectionType())
    }

    override suspend fun setAllEpisodesWatched(subjectId: Int) {
        val cache = findContainingCache(subjectId) ?: return
        cache.value.find { it.subjectId == subjectId }?.let { collection ->
            collection.episodes = collection.episodes.map { episode ->
                episode.copy(type = EpisodeCollectionType.WATCHED)
            }
        }
        val ids = episodeRepository.getEpisodesBySubjectId(subjectId, EpType.MainStory).map { it.id }.toList()
        episodeRepository.setEpisodeCollection(
            subjectId,
            ids,
            EpisodeCollectionType.WATCHED,
        )
    }

    override suspend fun setEpisodeWatched(subjectId: Int, episodeId: Int, watched: Boolean) {
        val cache = findContainingCache(subjectId) ?: return

        val newType = if (watched) EpisodeCollectionType.WATCHED else EpisodeCollectionType.WATCHLIST
        cache.value.find { it.subjectId == subjectId }?.let { collection ->
            collection.episodes = collection.episodes.map { episode ->
                if (episode.episode.id == episodeId) {
                    episode.copy(type = newType)
                } else {
                    episode
                }
            }
        }

        episodeRepository.setEpisodeCollection(
            subjectId,
            listOf(episodeId),
            newType,
        )
    }

    /**
     * Finds the cache that contains the subject.
     */
    private fun findContainingCache(subjectId: Int) =
        collectionsByType.values.firstOrNull { list -> list.value.any { it.subjectId == subjectId } }
}

sealed class ContinueWatchingStatus {
    data object Start : ContinueWatchingStatus()

    /**
     * 还未开播
     */
    data object NotOnAir : ContinueWatchingStatus()

    /**
     * 继续看
     */
    class Continue(
        val episodeIndex: Int,
        val episodeSort: String, // "12.5"
    ) : ContinueWatchingStatus()

    /**
     * 看到了, 但是下一集还没更新
     */
    class Watched(
        val episodeIndex: Int,
        val episodeSort: String, // "12.5"
    ) : ContinueWatchingStatus()

    data object Done : ContinueWatchingStatus()
}

@Stable
class SubjectCollectionItem(
    val subjectId: Int,
    val displayName: String,
    val image: String,
    val rate: Int?,

    val date: String?,
    val totalEps: Int,
    /**
     * 是否正在播出, 或者在未来会播出
     */
    val isOnAir: Boolean,
    /**
     * 最新更新到
     */
    val latestEp: UserEpisodeCollection?,
    val lastWatchedEpIndex: Int?,

    episodes: List<UserEpisodeCollection>,
    collectionType: SubjectCollectionType?,
) {
    /**
     * 是否已经开播了第一集
     */
    val hasStarted = episodes.firstOrNull()?.episode?.isOnAir() == false

    val collectionType: UnifiedCollectionType = collectionType.toCollectionType()
    var episodes by mutableStateOf(episodes)

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
        isOnAir: Boolean = this.isOnAir,
        latestEp: UserEpisodeCollection? = this.latestEp,
        lastWatchedEpIndex: Int? = this.lastWatchedEpIndex,
        episodes: List<UserEpisodeCollection> = this.episodes,
        collectionType: SubjectCollectionType? = this.collectionType.toSubjectCollectionType(),
    ) = SubjectCollectionItem(
        subjectId = subjectId,
        displayName = displayName,
        image = image,
        rate = rate,
        date = date,
        totalEps = totalEps,
        isOnAir = isOnAir,
        latestEp = latestEp,
        lastWatchedEpIndex = lastWatchedEpIndex,
        episodes = episodes,
        collectionType = collectionType,
    )
}

private fun UserSubjectCollection.createItem(
    subject: Subject?,
    isOnAir: Boolean,
    latestEp: UserEpisodeCollection?,
    lastWatchedEpIndex: Int?,
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
            isOnAir = isOnAir,
            latestEp = latestEp,
            lastWatchedEpIndex = null,
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
        isOnAir = isOnAir,
        latestEp = latestEp,
        lastWatchedEpIndex = lastWatchedEpIndex,
        episodes = episodes,
        collectionType = type,
    )
}
