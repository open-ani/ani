package me.him188.ani.app.ui.subject.details

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.models.subject.RelatedCharacterInfo
import me.him188.ani.app.data.models.subject.RelatedPersonInfo
import me.him188.ani.app.data.models.subject.SelfRatingInfo
import me.him188.ani.app.data.models.subject.SubjectAiringInfo
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.data.models.subject.SubjectManager
import me.him188.ani.app.data.models.subject.subjectInfoFlow
import me.him188.ani.app.data.repository.BangumiRelatedCharactersRepository
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.ContextMP
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.subject.collection.progress.EpisodeProgressState
import me.him188.ani.app.ui.subject.rating.EditableRatingState
import me.him188.ani.app.ui.subject.rating.RateRequest
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.bangumi.BangumiClient
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
            coverImageUrl = subjectInfo.map { it.imageLarge },
            selfCollectionType = subjectCollectionFlow.map {
                it?.collectionType ?: UnifiedCollectionType.NOT_COLLECTED
            },
            selfRatingInfo = subjectCollectionFlow.map { it?.selfRatingInfo ?: SelfRatingInfo.Empty },
            airingInfo = subjectCollectionFlow.map {
                it?.airingInfo
                    ?: SubjectAiringInfo.computeFromSubjectInfo(this.subjectInfo.first())
            },
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

    private val setSelfCollectionTypeTasker = MonoTasker(backgroundScope)
    val isSetSelfCollectionTypeWorking get() = setSelfCollectionTypeTasker.isRunning
    fun setSelfCollectionType(subjectCollectionType: UnifiedCollectionType) {
        setSelfCollectionTypeTasker.launch { subjectManager.setSubjectCollectionType(subjectId, subjectCollectionType) }
    }

    fun setAllEpisodesWatched() {
        launchInBackground { subjectManager.setAllEpisodesWatched(subjectId) }
    }

    fun browseSubjectBangumi(context: ContextMP) {
        browserNavigator.openBrowser(context, "https://bgm.tv/subject/${subjectId}")
    }


    val editableRatingState = EditableRatingState(
        ratingInfo = subjectInfo.map { it.ratingInfo },
        selfRatingInfo = flowOf(subjectDetailsState.selfRatingInfo),
        setSelfRating = { request ->
            subjectManager.updateRating(
                subjectId,
                request,
            )
        },
        backgroundScope.coroutineContext,
    )
}

private suspend inline fun SubjectManager.updateRating(subjectId: Int, request: RateRequest) {
    return this.updateRating(subjectId, request.score, request.comment, isPrivate = request.isPrivate)
}
