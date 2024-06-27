package me.him188.ani.app.ui.subject.details

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.toList
import me.him188.ani.app.data.repositories.BangumiRelatedCharactersRepository
import me.him188.ani.app.data.subject.SubjectInfo
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.app.data.subject.subjectInfoFlow
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.ContextMP
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.subject.collection.progress.EpisodeProgressState
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.client.BangumiEpType
import me.him188.ani.datasources.bangumi.models.subjects.BangumiSubjectImageSize
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.RelatedPerson

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
        persons = bangumiRelatedCharactersRepository.relatedPersonsFlow(subjectId),
        characters = bangumiRelatedCharactersRepository.relatedCharactersFlow(subjectId),
        parentCoroutineContext = backgroundScope.coroutineContext,
    )

//
//    val staff: SharedFlow<Staff> = combine(infoboxList, relatedPersons) { infoboxList, relatedPersons ->
//        infoboxList to relatedPersons
//    }.map { (infoboxList, relatedPersons) ->
//        infoboxList.map { it.key to  }
//        val company = relatedPersons.filter { it.type == "公司" }
//        val selectedRelatedPersons = relatedPersons.filter { it.type != "公司" }
//        Staff(company, selectedRelatedPersons)
//    }.shareInBackground()

    val episodeProgressState by lazy { EpisodeProgressState(subjectId, this) }

    fun setSelfCollectionType(subjectCollectionType: UnifiedCollectionType) {
        launchInBackground { subjectManager.setSubjectCollectionType(subjectId, subjectCollectionType) }
    }

    fun setAllEpisodesWatched() {
        launchInBackground { subjectManager.setAllEpisodesWatched(subjectId) }
    }

    private fun episodesFlow(type: BangumiEpType) = flowOf(this.subjectId).mapLatest { subjectId ->
        PageBasedPagedSource { page ->
            bangumiClient.episodes.getEpisodes(
                subjectId.toLong(),
                type,
                offset = page * 100,
                limit = 100,
            )
        }.results.toList()
    }.shareInBackground()

    fun browseSubjectBangumi(context: ContextMP) {
        browserNavigator.openBrowser(context, "https://bgm.tv/subject/${subjectId}")
    }
}


// "音乐制作"
private val selectedRelations: List<Regex> = listOf(
    Regex("动画制作"),
    Regex("导演|监督"),
    Regex("编剧"),
    Regex("音乐"),
    Regex("人物设定"),
    Regex("系列构成"),
    Regex("动作作画监督|动作导演"),
    Regex("美术设计"),
    Regex("主题歌(.*)?"),
)

private fun List<RelatedPerson>.sortByRelation(): List<RelatedPerson> {
    val original = this
    return buildList {
        for (selectedRelation in selectedRelations) {
            for (relatedPerson in original) {
                if (relatedPerson.relation.matches(selectedRelation)) {
                    add(relatedPerson)
                }
            }
        }
    }
}
