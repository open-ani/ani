package me.him188.ani.app.ui.subject.details

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import me.him188.ani.app.data.models.subject.RatingInfo
import me.him188.ani.app.data.models.subject.RelatedCharacterInfo
import me.him188.ani.app.data.models.subject.RelatedPersonInfo
import me.him188.ani.app.data.models.subject.SelfRatingInfo
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.data.models.subject.SubjectManager
import me.him188.ani.app.data.models.subject.subjectInfoFlow
import me.him188.ani.app.data.repository.BangumiRelatedCharactersRepository
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.data.source.session.AuthState
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.ContextMP
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.subject.collection.EditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.collection.progress.EpisodeListState
import me.him188.ani.app.ui.subject.collection.progress.EpisodeListStateFactory
import me.him188.ani.app.ui.subject.collection.progress.SubjectProgressInfo
import me.him188.ani.app.ui.subject.collection.progress.SubjectProgressState
import me.him188.ani.app.ui.subject.collection.progress.SubjectProgressStateFactory
import me.him188.ani.app.ui.subject.episode.list.EpisodeListProgressTheme
import me.him188.ani.app.ui.subject.rating.EditableRatingState
import me.him188.ani.app.ui.subject.rating.RateRequest
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class SubjectDetailsViewModel(
    private val subjectId: Int,
) : AbstractViewModel(), KoinComponent {
    private val subjectManager: SubjectManager by inject()
    private val browserNavigator: BrowserNavigator by inject()
    private val bangumiRelatedCharactersRepository: BangumiRelatedCharactersRepository by inject()
    private val settingsRepository: SettingsRepository by inject()

    private val subjectInfo: SharedFlow<SubjectInfo> = subjectManager.subjectInfoFlow(subjectId).shareInBackground()
    private val subjectCollectionFlow = subjectManager.subjectCollectionFlow(subjectId).shareInBackground()

    lateinit var navigator: AniNavigator

    val authState = AuthState()

    val subjectProgressState = kotlin.run {
        SubjectProgressStateFactory(
            subjectManager,
            onPlay = { subjectId, episodeId ->
                navigator.navigateEpisodeDetails(subjectId, episodeId)
            },
        ).run {
            SubjectProgressState(
                stateOf(subjectId),
                subjectCollectionFlow.map { SubjectProgressInfo.calculate(it) }
                    .produceState(SubjectProgressInfo.Empty),
                episodeProgressInfoList(subjectId).produceState(emptyList()),
                onPlay,
            )
        }
    }

    val subjectDetailsState = kotlin.run {
        SubjectDetailsState(
            subjectInfo = subjectInfo,
            coverImageUrl = subjectInfo.map { it.imageLarge },
            selfCollectionType = subjectCollectionFlow.map { it.collectionType },
            airingInfo = subjectCollectionFlow.map { it.airingInfo },
            persons = bangumiRelatedCharactersRepository.relatedPersonsFlow(subjectId).map {
                RelatedPersonInfo.sortList(it)
            }.onCompletion { if (it != null) emit(emptyList()) },
            characters = bangumiRelatedCharactersRepository.relatedCharactersFlow(subjectId).map {
                RelatedCharacterInfo.sortList(it)
            },
            parentCoroutineContext = backgroundScope.coroutineContext,
        )
    }

    val episodeListState by lazy {
        EpisodeListStateFactory(
            settingsRepository,
            subjectManager,
            backgroundScope,
        ).run {
            EpisodeListState(
                stateOf(subjectId),
                theme.produceState(EpisodeListProgressTheme.Default),
                episodes(subjectId).produceState(emptyList()),
                ::onSetEpisodeWatched,
                backgroundScope,
            )
        }
    }

    val editableSubjectCollectionTypeState = EditableSubjectCollectionTypeState(
        selfCollectionType = subjectCollectionFlow
            .map { it.collectionType }
            .produceState(UnifiedCollectionType.NOT_COLLECTED),
        hasAnyUnwatched = { episodeListState.hasAnyUnwatched },
        onSetSelfCollectionType = { subjectManager.setSubjectCollectionType(subjectId, it) },
        onSetAllEpisodesWatched = {
            subjectManager.setAllEpisodesWatched(subjectId)
        },
        backgroundScope,
    )

    val editableRatingState = EditableRatingState(
        ratingInfo = subjectInfo.map { it.ratingInfo }.produceState(RatingInfo.Empty),
        selfRatingInfo = subjectCollectionFlow.map { it.selfRatingInfo }
            .produceState(SelfRatingInfo.Empty),
        enableEdit = subjectCollectionFlow
            .map { it.collectionType != UnifiedCollectionType.NOT_COLLECTED }
            .produceState(false),
        isCollected = {
            val collection = subjectCollectionFlow.replayCache.firstOrNull() ?: return@EditableRatingState false
            collection.collectionType != UnifiedCollectionType.NOT_COLLECTED
        },
        onRate = { request ->
            subjectManager.updateRating(
                subjectId,
                request,
            )
        },
        backgroundScope,
    )

    fun browseSubjectBangumi(context: ContextMP) {
        browserNavigator.openBrowser(context, "https://bgm.tv/subject/${subjectId}")
    }
}

suspend inline fun SubjectManager.updateRating(subjectId: Int, request: RateRequest) {
    return this.updateRating(subjectId, request.score, request.comment, isPrivate = request.isPrivate)
}
