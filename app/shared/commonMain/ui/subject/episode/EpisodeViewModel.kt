package me.him188.ani.app.ui.subject.episode

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.Context
import me.him188.ani.app.torrent.TorrentDownloaderManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.videoplayer.TorrentVideoSource
import me.him188.ani.app.videoplayer.VideoSource
import me.him188.ani.datasources.api.DownloadProvider
import me.him188.ani.datasources.api.DownloadSearchQuery
import me.him188.ani.datasources.api.SearchSession
import me.him188.ani.datasources.api.topic.FileSize
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
import kotlin.time.Duration.Companion.seconds

class EpisodeViewModel(
    initialSubjectId: Int,
    initialEpisodeId: Int,
) : AbstractViewModel(), KoinComponent {
    private val bangumiClient by inject<BangumiClient>()
    private val dmhyClient by inject<DownloadProvider>()
    private val browserNavigator: BrowserNavigator by inject()
    private val torrentDownloaderManager: TorrentDownloaderManager by inject()

    private val episodeId: MutableStateFlow<Int> = MutableStateFlow(initialEpisodeId)

    private val subjectDetails = flow {
        emit(withContext(Dispatchers.IO) { bangumiClient.api.getSubjectById(initialSubjectId) })
    }.shareInBackground()

    @Stable
    val episode = episodeId.mapLatest { episodeId ->
        withContext(Dispatchers.IO) { bangumiClient.api.getEpisodeById(episodeId) }
    }.shareInBackground()

    @Stable
    val subjectTitle = subjectDetails.filterNotNull().mapLatest { subject ->
        subject.nameCNOrName()
    }.shareInBackground()

    @Stable
    val episodeEp = episode.filterNotNull().mapLatest { episode ->
        episode.renderEpisodeSp()
    }.shareInBackground()

    @Stable
    val episodeTitle = episode.filterNotNull().mapLatest { episode ->
        episode.nameCNOrName()
    }.shareInBackground()


//    private val remoteEpisodeWatched = episode.filterNotNull().map {
//        bangumiClient.api.getUserEpisodeCollection(it.id).type == EpisodeCollectionType.WATCHED
//    }
//    private val localEpisodeWatched = MutableStateFlow(false)
//    val episodeWatched = merge(remoteEpisodeWatched, localEpisodeWatched).stateInBackground(false)

//    fun setEpisodeWatched(
//        collectionType: EpisodeCollectionType,
//    ) {
//        episode.value?.let {
//            bangumiClient.api.putUserEpisodeCollection(
//                it.id, PutUserEpisodeCollectionRequest(type = collectionType)
//            )
//        }
//        localEpisodeWatched.value = watched
//    }


    // 动漫花园等数据源搜搜结果
    private val _isPlaySourcesLoading = MutableStateFlow(true)

    @Stable
    val isPlaySourcesLoading: StateFlow<Boolean> get() = _isPlaySourcesLoading

    @Stable
    val playSources: SharedFlow<Collection<PlaySource>?> = combine(episode, subjectTitle) { episode, subjectTitle ->
        episode to subjectTitle
    }.mapNotNull { (episode, subjectTitle) ->

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
                originalUrl = it.link,
                magnetLink = it.magnetLink,
                originalTitle = it.rawTitle,
                size = it.size,
            )
        }

    @Stable
    val playSourceSelector = PlaySourceSelector(
        subjectDetails.map { it.id },
        listOf(),
        playSources.filterNotNull(),
        backgroundScope
    )

    @Stable
    val videoSource: SharedFlow<VideoSource<*>?> = playSourceSelector.targetPlaySourceCandidate
        .debounce(1.seconds)
        .combine(torrentDownloaderManager.torrentDownloader) { video, torrentDownloader ->
            video to torrentDownloader
        }
        .transformLatest { (playSource, torrentDownloader) ->
            emit(null)
            playSource?.let {
                emit(TorrentVideoSource(torrentDownloader.fetchMagnet(it.playSource.magnetLink)))
            }
        }.shareInBackground()

    var showPlaySourceSheet by mutableStateOf(false)

    fun setEpisodeId(episodeId: Int) {
        this.episodeId.value = episodeId
    }

    suspend fun copyDownloadLink(clipboardManager: ClipboardManager, snackbar: SnackbarHostState) {
        playSourceSelector.targetPlaySourceCandidate.value?.let {
            clipboardManager.setText(AnnotatedString(it.playSource.magnetLink))
            snackbar.showSnackbar("已复制下载链接")
        } ?: run {
            snackbar.showSnackbar("请先选择数据源")
        }
    }

    suspend fun browsePlaySource(context: Context, snackbar: SnackbarHostState) {
        playSourceSelector.targetPlaySourceCandidate.value?.let {
            browserNavigator.openBrowser(context, it.playSource.originalUrl)
        } ?: run {
            snackbar.showSnackbar("请先选择数据源")
            showPlaySourceSheet = true
        }
    }

    suspend fun browseDownload(context: Context, snackbar: SnackbarHostState) {
        playSourceSelector.targetPlaySourceCandidate.value?.let {
            browserNavigator.openMagnetLink(context, it.playSource.originalUrl)
        } ?: run {
            snackbar.showSnackbar("请先选择数据源")
            showPlaySourceSheet = true
        }
    }
}

@Immutable
data class PlaySource(
    val id: String, // must be unique
    val alliance: String,
    val subtitleLanguage: String, // null means raw
    val resolution: Resolution,
    val dataSource: String, // dmhy
    val originalUrl: String,
    val magnetLink: String,
    val originalTitle: String,
    val size: FileSize,
)