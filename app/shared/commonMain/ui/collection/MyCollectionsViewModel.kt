package me.him188.ani.app.ui.collection

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import me.him188.ani.app.data.CollectionRepository
import me.him188.ani.app.data.EpisodeRepository
import me.him188.ani.app.data.SubjectRepository
import me.him188.ani.app.navigation.AuthorizationNavigator
import me.him188.ani.app.navigation.SubjectNavigator
import me.him188.ani.app.platform.Context
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.datasources.bangumi.processing.airSeason
import me.him188.ani.datasources.bangumi.processing.isOnAir
import me.him188.ani.datasources.bangumi.processing.nameCNOrName
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.EpType
import org.openapitools.client.models.EpisodeCollectionType
import org.openapitools.client.models.Subject
import org.openapitools.client.models.SubjectCollectionType
import org.openapitools.client.models.SubjectType
import org.openapitools.client.models.UserEpisodeCollection
import org.openapitools.client.models.UserSubjectCollection
import kotlin.time.Duration.Companion.seconds

class MyCollectionsViewModel : AbstractViewModel(), KoinComponent {
    private val sessionManager: SessionManager by inject()
    private val collectionRepository: CollectionRepository by inject()
    private val subjectRepository: SubjectRepository by inject()
    private val episodeRepository: EpisodeRepository by inject()
    private val authorizationNavigator: AuthorizationNavigator by inject()
    private val subjectNavigator: SubjectNavigator by inject()

    val isLoggedIn = sessionManager.isSessionValid.filterNotNull().shareInBackground()

    val collections = sessionManager.username.filterNotNull().flatMapLatest { username ->
        collectionRepository.getCollections(username).map { raw ->
            raw.convertToItem()
        }.runningList()
    }.shareInBackground()

    val isEmpty = collections.map { it.isEmpty() }.debounce(0.5.seconds).shareInBackground()

    private suspend fun UserSubjectCollection.convertToItem() = coroutineScope {
        val subject = async {
            subjectRepository.getSubject(subjectId)
        }
        val eps = episodeRepository.getSubjectEpisodeCollection(subjectId, EpType.MainStory).toList()
        val isOnAir = async {
            eps.firstOrNull { it.episode.isOnAir() == true } != null
        }
        val lastWatchedEp = async {
            eps.indexOfLast {
                it.type == EpisodeCollectionType.WATCHED || it.type == EpisodeCollectionType.DISCARDED
            }
        }
        val latestEp = async {
            eps.lastOrNull { it.episode.isOnAir() == false }
                ?: eps.lastOrNull { it.episode.isOnAir() != true }
        }

        createItem(subject.await(), isOnAir.await(), latestEp.await(), lastWatchedEp.await(), eps)
    }

    suspend fun navigateToAuth(context: Context) {
        authorizationNavigator.navigateToAuthorization(context, true)
    }


    fun navigateToSubject(context: Context, subjectId: Int) {
        subjectNavigator.navigateToSubjectDetails(context, subjectId)
    }

    fun navigateToEpisode(context: Context, subjectId: Int, episodeId: Int) {
        subjectNavigator.navigateToEpisode(context, subjectId, episodeId)
    }

    suspend fun updateCollection(subjectId: Int, action: SubjectCollectionAction) {
        if (action.type == null) {
            collectionRepository.removeCollection(subjectId)
        } else {
            collectionRepository.updateCollection(subjectId, action.type)
        }
    }
}

@Immutable
class SubjectCollectionItem(
    val subjectId: Int,
    val displayName: String,
    val image: String,
    val rate: Int?,

    val date: String?,
    val totalEps: Int,
    val isOnAir: Boolean,
    /**
     * 最新更新到
     */
    val latestEp: UserEpisodeCollection?,
    val lastWatchedEpIndex: Int?,

    val episodes: List<UserEpisodeCollection>,
    val collectionType: SubjectCollectionType,
) {
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
