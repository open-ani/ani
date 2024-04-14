package me.him188.ani.app.ui.subject.cache

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.toList
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.data.repositories.EpisodeRepository
import me.him188.ani.app.data.repositories.SubjectRepository
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.bangumi.processing.nameCNOrName
import me.him188.ani.datasources.bangumi.processing.toCollectionType
import me.him188.ani.utils.coroutines.runUntilSuccess
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.EpType

@Stable
class SubjectCacheViewModel(
    subjectId: Int,
) : AbstractViewModel(), KoinComponent {
    private val subjectRepository: SubjectRepository by inject()
    private val episodeRepository: EpisodeRepository by inject()
    private val cacheRepository: MediaCacheManager by inject()

    val subjectTitle: Flow<String> = flowOf(subjectId).mapLatest { subjectId ->
        runUntilSuccess { subjectRepository.getSubject(subjectId)!! }
            .nameCNOrName()
    }

    val state = flowOf(subjectId).mapLatest { subjectId ->
        runUntilSuccess { episodeRepository.getSubjectEpisodeCollection(subjectId, EpType.MainStory).toList() }
    }.filterNotNull().map { episodes ->
        DefaultSubjectCacheState(
            episodes.map { episodeCollection ->
                val episode = episodeCollection.episode
                EpisodeCacheState(
                    id = episode.id,
                    sort = EpisodeSort(episode.sort),
                    title = episode.nameCn,
                    watchStatus = episodeCollection.type.toCollectionType(),
                    cacheStatus = cacheRepository.cacheStatusForEpisode(subjectId, episode.id),
                )
            }
        )
    }
}

@Composable
fun SubjectCachePage(
    vm: SubjectCacheViewModel,
    onClickGlobalCacheSettings: () -> Unit,
    onClickEpisode: (EpisodeCacheState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by vm.state.collectAsStateWithLifecycle(DefaultSubjectCacheState(emptyList()))
    return SubjectCachePage(
        state,
        title = {
            val title by vm.subjectTitle.collectAsStateWithLifecycle(null)
            Text(title.orEmpty(), Modifier.placeholder(title == null))
        },
        onClickGlobalCacheSettings, onClickEpisode, modifier
    )
}