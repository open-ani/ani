package me.him188.ani.app.ui.subject.details

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.repositories.BangumiRelatedCharactersRepository
import me.him188.ani.app.data.subject.RelatedCharacterInfo
import me.him188.ani.app.data.subject.RelatedPersonInfo
import me.him188.ani.app.data.subject.SubjectInfo
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.app.data.subject.subjectInfoFlow
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.ContextMP
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.subject.collection.progress.EpisodeProgressState
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

    val subjectDetailsState = SubjectDetailsState(
        subjectInfo = subjectInfo,
        coverImageUrl = bangumiClient.subjects.getSubjectImageUrl(subjectId, BangumiSubjectImageSize.LARGE),
        selfCollectionType = subjectManager.subjectCollectionType(subjectId),
        persons = bangumiRelatedCharactersRepository.relatedPersonsFlow(subjectId).map {
            RelatedPersonInfo.sortList(it)
        },
        characters = bangumiRelatedCharactersRepository.relatedCharactersFlow(subjectId).map {
            RelatedCharacterInfo.sortList(it)
        },
        parentCoroutineContext = backgroundScope.coroutineContext,
    )

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
}
