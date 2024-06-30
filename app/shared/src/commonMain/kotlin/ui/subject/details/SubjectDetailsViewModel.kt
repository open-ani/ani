package me.him188.ani.app.ui.subject.details

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.repositories.BangumiRelatedCharactersRepository
import me.him188.ani.app.data.subject.RelatedCharacterInfo
import me.him188.ani.app.data.subject.RelatedPersonInfo
import me.him188.ani.app.data.subject.SelfRatingInfo
import me.him188.ani.app.data.subject.SubjectInfo
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.app.data.subject.subjectInfoFlow
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.ContextMP
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.subject.collection.progress.EpisodeProgressState
import me.him188.ani.app.ui.subject.rating.RateRequest
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.models.subjects.BangumiSubjectImageSize
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class SubjectDetailsViewModel(
    private val subjectId: Int,
) : AbstractViewModel(), KoinComponent {
    private val subjectManager: SubjectManager by inject()
    private val bangumiClient: BangumiClient by inject()
    private val browserNavigator: BrowserNavigator by inject()
    private val bangumiRelatedCharactersRepository: BangumiRelatedCharactersRepository by inject()

    private val subjectInfo: SharedFlow<SubjectInfo> = subjectManager.subjectInfoFlow(subjectId).shareInBackground()

    val subjectDetailsState = kotlin.run {
        val subjectCollectionFlow = subjectManager.subjectCollectionFlow(subjectId)
        SubjectDetailsState(
            subjectInfo = subjectInfo,
            coverImageUrl = bangumiClient.subjects.getSubjectImageUrl(subjectId, BangumiSubjectImageSize.LARGE),
            selfCollectionType = subjectCollectionFlow.map {
                it?.collectionType ?: UnifiedCollectionType.NOT_COLLECTED
            },
            selfRatingInfo = subjectCollectionFlow.map { it?.selfRatingInfo ?: SelfRatingInfo.Empty },
            persons = bangumiRelatedCharactersRepository.relatedPersonsFlow(subjectId).map {
                RelatedPersonInfo.sortList(it)
            },
            characters = bangumiRelatedCharactersRepository.relatedCharactersFlow(subjectId).map {
                RelatedCharacterInfo.sortList(it)
            },
            parentCoroutineContext = backgroundScope.coroutineContext,
        )

    }
    val episodeProgressState by lazy { EpisodeProgressState(subjectId, this) }

    fun setSelfCollectionType(subjectCollectionType: UnifiedCollectionType) {
        launchInBackground { subjectManager.setSubjectCollectionType(subjectId, subjectCollectionType) }
    }

    fun setAllEpisodesWatched() {
        launchInBackground { subjectManager.setAllEpisodesWatched(subjectId) }
    }

    fun browseSubjectBangumi(context: ContextMP) {
        browserNavigator.openBrowser(context, "https://bgm.tv/subject/${subjectId}")
    }


    var showRatingDialog by mutableStateOf(false)
    private val updateRatingTasker = MonoTasker(backgroundScope)
    val isRatingUpdating: Boolean
        get() = updateRatingTasker.isRunning

    fun updateRating(request: RateRequest) {
        updateRatingTasker.launch {
            subjectManager.updateRating(
                subjectId,
                score = request.score,
                comment = request.comment,
                isPrivate = request.isPrivate,
            )
            withContext(Dispatchers.Main) {
                showRatingDialog = false
            }
        }
    }

    fun cancelUpdateRating() {
        updateRatingTasker.cancel()
    }
}
