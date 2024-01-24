package me.him188.ani.app.ui.subject.episode

import androidx.annotation.UiThread
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.EpisodeRepository
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.Context
import me.him188.ani.app.torrent.TorrentDownloaderManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.videoplayer.PlayerController
import me.him188.ani.app.videoplayer.PlayerControllerFactory
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
import org.openapitools.client.models.EpisodeCollectionType
import org.openapitools.client.models.EpisodeDetail
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration.Companion.seconds

interface EpisodeViewModel : HasBackgroundScope {
    // Subject

    @Stable
    val subjectId: StateFlow<Int>
    fun setSubjectId(subjectId: Int)

    @Stable
    val subjectTitle: SharedFlow<String>

    // Episode

    @Stable
    val episodeId: StateFlow<Int>
    fun setEpisodeId(episodeId: Int)

    @Stable
    val episode: SharedFlow<EpisodeDetail>

    @Stable
    val episodeTitle: Flow<String>

    @Stable
    val episodeEp: Flow<String>

    @Stable
    val isFullscreen: StateFlow<Boolean>

    fun setFullscreen(fullscreen: Boolean)

    // Collection

    val episodeCollectionType: SharedFlow<EpisodeCollectionType>
    suspend fun setEpisodeCollectionType(type: EpisodeCollectionType)

    // episode play position

    val episodePosition: Flow<Long>
    fun setEpisodePosition(position: Long)

    // Video

    /**
     * `true` if a play source is selected by user (or automatically)
     */
    @Stable
    val playSourceSelected: Flow<Boolean>

    /**
     * `true` if the list of play sources are still downloading (e.g. from dmhy).
     */
    @Stable
    val isPlaySourcesLoading: StateFlow<Boolean>

    @Stable
    val playSourceSelector: PlaySourceSelector

    /**
     * `true` if the video is ready to play.
     *
     * This does not guarantee that there is enough buffer to play the video.
     * For torrent videos, `true` means the magnet link is resolved and we can start downloading.
     */
    @Stable
    val isVideoReady: Flow<Boolean>

    /**
     * Play controller for video view. This can be saved even when window configuration changes (i.e. everything recomposes).
     */
    @Stable
    val playerController: PlayerController

    /**
     * `true` if the bottom sheet for choosing play source should be shown.
     */
    val isShowPlaySourceSheet: StateFlow<Boolean>
    fun setShowPlaySourceSheet(show: Boolean)

    @UiThread
    suspend fun copyDownloadLink(clipboardManager: ClipboardManager, snackbar: SnackbarHostState)

    @UiThread
    suspend fun browsePlaySource(context: Context, snackbar: SnackbarHostState)

    @UiThread
    suspend fun browseDownload(context: Context, snackbar: SnackbarHostState)
}

fun EpisodeViewModel(
    initialSubjectId: Int,
    initialEpisodeId: Int,
    initialIsFullscreen: Boolean = false,
    context: Context,
): EpisodeViewModel = EpisodeViewModelImpl(initialSubjectId, initialEpisodeId, initialIsFullscreen, context)

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


private class EpisodeViewModelImpl(
    initialSubjectId: Int,
    initialEpisodeId: Int,
    initialIsFullscreen: Boolean = false,
    context: Context,
) : AbstractViewModel(), KoinComponent, EpisodeViewModel {
    private val bangumiClient by inject<BangumiClient>()
    private val dmhyClient by inject<DownloadProvider>()
    private val browserNavigator: BrowserNavigator by inject()
    private val torrentDownloaderManager: TorrentDownloaderManager by inject()
    private val playerControllerFactory: PlayerControllerFactory by inject()
    private val episodeRepository: EpisodeRepository by inject()

    override val episodeId: MutableStateFlow<Int> = MutableStateFlow(initialEpisodeId)

    override val subjectId: MutableStateFlow<Int> = MutableStateFlow(initialSubjectId)

    private val subjectDetails = subjectId.mapLatest {
        withContext(Dispatchers.IO) { bangumiClient.api.getSubjectById(initialSubjectId) } // TODO: replace with data layer 
    }.shareInBackground()

    @Stable
    override val episode: SharedFlow<EpisodeDetail> = episodeId.mapLatest { episodeId ->
        withContext(Dispatchers.IO) { bangumiClient.api.getEpisodeById(episodeId) }
    }.shareInBackground()

    @Stable
    override val subjectTitle: SharedFlow<String> = subjectDetails.filterNotNull().mapLatest { subject ->
        subject.nameCNOrName()
    }.shareInBackground()

    @Stable
    override val episodeEp = episode.filterNotNull().mapLatest { episode ->
        episode.renderEpisodeSp()
    }.shareInBackground()

    @Stable
    override val episodeTitle = episode.filterNotNull().mapLatest { episode ->
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
    override val isPlaySourcesLoading: StateFlow<Boolean> get() = _isPlaySourcesLoading

    @Stable
    private val playSources: SharedFlow<Collection<PlaySource>?> =
        combine(episode, subjectTitle) { episode, subjectTitle ->
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
    override val playSourceSelector = PlaySourceSelector(
        subjectDetails.map { it.id },
        listOf(),
        playSources.filterNotNull(),
        backgroundScope
    )

    @Stable
    override val playSourceSelected: Flow<Boolean> =
        playSourceSelector.targetPlaySourceCandidate.map { it != null }


    /**
     * The [VideoSource] selected to play.
     *
     * `null` has two possible meanings:
     * - List of video sources are still downloading so user has nothing to select.
     * - The sources are available but user has not yet selected one.
     */
    @Stable
    private val videoSource: SharedFlow<VideoSource<*>?> = playSourceSelector.targetPlaySourceCandidate
        .debounce(1.seconds)
        .combine(torrentDownloaderManager.torrentDownloader) { video, torrentDownloader ->
            video to torrentDownloader
        }
        .transformLatest { (playSource, torrentDownloader) ->
            emit(null)
            playSource?.let {
                try {
                    emit(TorrentVideoSource(torrentDownloader.fetchMagnet(it.playSource.magnetLink)))
                } catch (e: Exception) {
                    emit(null)
                }
            }
        }.shareInBackground()

    @Stable
    override val isVideoReady: Flow<Boolean> = videoSource.map { it != null }

    @Stable
    override val playerController: PlayerController = playerControllerFactory.create(context, videoSource)

    override val isShowPlaySourceSheet = MutableStateFlow(false)
    override fun setShowPlaySourceSheet(show: Boolean) {
        isShowPlaySourceSheet.value = show
    }

    @Stable
    override val isFullscreen: MutableStateFlow<Boolean> = MutableStateFlow(initialIsFullscreen)

    override fun setFullscreen(fullscreen: Boolean) {
        isFullscreen.value = fullscreen
    }

    override val episodeCollectionType: MutableSharedFlow<EpisodeCollectionType> = episodeId.mapNotNull {
        episodeRepository.getEpisodeCollection(it)?.type
    }.localCachedSharedFlow()

    override suspend fun setEpisodeCollectionType(type: EpisodeCollectionType) {
        episodeCollectionType.tryEmit(type)
        episodeRepository.setEpisodeCollection(subjectId.value, listOf(episodeId.value), type)
    }

    override val episodePosition = combine(subjectId, episodeId) { sid, eid ->
        episodeRepository.getEpisodePosition("$sid.$eid").first()
    }

    override fun setEpisodePosition(position: Long) {
        combine(subjectId, episodeId) { sid, eid ->
            episodeRepository.setEpisodePosition("$sid.$eid", position)
        }
    }

    override fun setSubjectId(subjectId: Int) {
        this.subjectId.value = subjectId
    }

    override fun setEpisodeId(episodeId: Int) {
        this.episodeId.value = episodeId
    }

    override suspend fun copyDownloadLink(clipboardManager: ClipboardManager, snackbar: SnackbarHostState) {
        requestPlaySourceCandidate()?.let {
            clipboardManager.setText(AnnotatedString(it.playSource.magnetLink))
            snackbar.showSnackbar("已复制下载链接")
        }
    }

    override suspend fun browsePlaySource(context: Context, snackbar: SnackbarHostState) {
        requestPlaySourceCandidate()?.let {
            browserNavigator.openBrowser(context, it.playSource.originalUrl)
        }
    }

    override suspend fun browseDownload(context: Context, snackbar: SnackbarHostState) {
        requestPlaySourceCandidate()?.let {
            browserNavigator.openMagnetLink(context, it.playSource.magnetLink)
        }
    }

    private suspend fun requestPlaySourceCandidate(): PlaySourceCandidate? {
        val candidate = playSourceSelector.targetPlaySourceCandidate.value
        if (candidate != null) {
            return candidate
        }
        setShowPlaySourceSheet(true)
        isShowPlaySourceSheet.first { !it } // await closed
        return playSourceSelector.targetPlaySourceCandidate.value
    }
}
