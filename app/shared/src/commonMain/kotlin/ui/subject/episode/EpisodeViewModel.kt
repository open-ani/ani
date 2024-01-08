package me.him188.ani.app.ui.subject.episode

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import me.him188.ani.app.ui.framework.AbstractViewModel
import me.him188.ani.app.ui.framework.launchInBackground
import me.him188.ani.datasources.api.DownloadProvider
import me.him188.ani.datasources.api.DownloadSearchQuery
import me.him188.ani.datasources.api.SearchSession
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.Topic
import me.him188.ani.datasources.api.topic.TopicCategory
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.processing.nameCNOrName
import me.him188.ani.datasources.bangumi.processing.renderEpisodeSp
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.EpisodeDetail
import java.util.concurrent.ConcurrentLinkedQueue

@Stable
class EpisodeViewModel(
    initialSubjectId: Int,
    initialEpisodeId: Int,
) : AbstractViewModel(), KoinComponent {
    private val bangumiClient by inject<BangumiClient>()
    private val dmhyClient by inject<DownloadProvider>()

    private val episodeId: MutableStateFlow<Int> = MutableStateFlow(initialEpisodeId)

    private val subjectDetails = flow {
        emit(withContext(Dispatchers.IO) { bangumiClient.api.getSubjectById(initialSubjectId) })
    }.stateInBackground()

    val episode = episodeId.mapLatest { episodeId ->
        withContext(Dispatchers.IO) { bangumiClient.api.getEpisodeById(episodeId) }
    }.stateInBackground()

    val subjectTitle = subjectDetails.filterNotNull().mapLatest { subject ->
        subject.nameCNOrName()
    }.stateInBackground()

    val episodeEp = episode.filterNotNull().mapLatest { episode ->
        episode.renderEpisodeSp()
    }.stateInBackground()

    val episodeTitle = episode.filterNotNull().mapLatest { episode ->
        episode.nameCNOrName()
    }.stateInBackground()

    // 动漫花园等数据源搜搜结果
    private val _isPlaySourcesLoading = MutableStateFlow(true)
    val isPlaySourcesLoading: StateFlow<Boolean> get() = _isPlaySourcesLoading

    val playSources: SharedFlow<Collection<PlaySource>?> = combine(episode, subjectTitle) { episode, subjectTitle ->
        episode to subjectTitle
    }.mapNotNull { (episode, subjectTitle) ->
        if (episode == null || subjectTitle == null) {
            return@mapNotNull null
        }

        _isPlaySourcesLoading.emit(true)
        val session = dmhyClient.startSearch(
            DownloadSearchQuery(
                keywords = subjectTitle,
                category = TopicCategory.ANIME,
            )
        )
        // 等完成时将 _isPlaySourcesLoading 设置为 false
        launchInBackground {
            select {
                session.onFinish {
                    _isPlaySourcesLoading.emit(false)
                }
            }
        }

        processDmhyResults(session, episode)
    }.transformLatest { flow ->
        val list = ConcurrentLinkedQueue<PlaySource>()
        flow.collect {
            list.add(it)
            emit(list)
        }
    }.shareInBackground()

    private fun processDmhyResults(
        session: SearchSession<Topic>,
        currentEpisode: EpisodeDetail
    ) = session.results
        .filter { it.details != null }
        .filter {
            it.details!!.episode?.toString()?.removePrefix("0") == currentEpisode.ep?.toString()?.removePrefix("0")
        }
        .map {
            val details = it.details!!
            PlaySource(
                id = "dmhy-${it.id}",
                alliance = it.alliance,
                subtitleLanguage = details.subtitleLanguages.firstOrNull()?.toString() ?: "生肉",
                resolution = details.resolution ?: Resolution.R1080P, // 默认 1080P, 因为目前大概都是 1080P
                dataSource = "动漫花园",
            )
        }

    @Stable
    val playSourceSelector = PlaySourceSelector(
        listOf(),
        playSources.filterNotNull(),
        backgroundScope
    )

    private val _currentPlaySource = MutableStateFlow<PlaySource?>(null)
    val currentPlaySource: StateFlow<PlaySource?> get() = _currentPlaySource

    fun setCurrentPlaySource(playSource: PlaySource?) {
        _currentPlaySource.value = playSource
    }

    fun setEpisodeId(episodeId: Int) {
        this.episodeId.value = episodeId
    }
}

@Immutable
data class PlaySource(
    val id: String, // must be unique
    val alliance: String,
    val subtitleLanguage: String, // null means raw
    val resolution: Resolution,
    val dataSource: String,
)