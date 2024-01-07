package me.him188.ani.app.ui.subject.episode

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import me.him188.ani.app.ui.framework.AbstractViewModel
import me.him188.ani.datasources.bangumi.BangumiClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EpisodeViewModel(
    initialSubjectId: Int,
    initialEpisodeId: Int,
) : AbstractViewModel(), KoinComponent {
    private val bangumiClient by inject<BangumiClient>()

    private val episodeId: MutableStateFlow<Int> = MutableStateFlow(initialEpisodeId)

    private val subjectDetails = flow {
        emit(withContext(Dispatchers.IO) { bangumiClient.api.getSubjectById(initialSubjectId) })
    }.stateInBackground()

    val episode = episodeId.mapLatest { episodeId ->
        withContext(Dispatchers.IO) { bangumiClient.api.getEpisodeById(episodeId) }
    }.stateInBackground()

    fun setEpisodeId(episodeId: Int) {
        this.episodeId.value = episodeId
    }
    
    
}