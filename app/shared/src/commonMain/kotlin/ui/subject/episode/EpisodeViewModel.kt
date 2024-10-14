/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.episode

import androidx.annotation.UiThread
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import me.him188.ani.app.data.models.ApiFailure
import me.him188.ani.app.data.models.ApiResponse
import me.him188.ani.app.data.models.episode.episode
import me.him188.ani.app.data.models.episode.renderEpisodeEp
import me.him188.ani.app.data.models.episode.type
import me.him188.ani.app.data.models.preference.VideoScaffoldConfig
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.data.models.subject.SubjectManager
import me.him188.ani.app.data.models.subject.SubjectProgressInfo
import me.him188.ani.app.data.models.subject.episodeInfoFlow
import me.him188.ani.app.data.models.subject.subjectInfoFlow
import me.him188.ani.app.data.repository.CommentRepository
import me.him188.ani.app.data.repository.DanmakuRegexFilterRepository
import me.him188.ani.app.data.repository.EpisodePlayHistoryRepository
import me.him188.ani.app.data.repository.EpisodePreferencesRepository
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.domain.danmaku.DanmakuManager
import me.him188.ani.app.domain.media.cache.EpisodeCacheStatus
import me.him188.ani.app.domain.media.cache.MediaCacheManager
import me.him188.ani.app.domain.media.fetch.FilteredMediaSourceResults
import me.him188.ani.app.domain.media.fetch.MediaFetchSession
import me.him188.ani.app.domain.media.fetch.MediaSourceManager
import me.him188.ani.app.domain.media.fetch.create
import me.him188.ani.app.domain.media.fetch.createFetchFetchSessionFlow
import me.him188.ani.app.domain.media.resolver.VideoSourceResolver
import me.him188.ani.app.domain.media.selector.MediaSelector
import me.him188.ani.app.domain.media.selector.MediaSelectorAutoSelect
import me.him188.ani.app.domain.media.selector.MediaSelectorFactory
import me.him188.ani.app.domain.media.selector.autoSelect
import me.him188.ani.app.domain.media.selector.eventHandling
import me.him188.ani.app.domain.session.AuthState
import me.him188.ani.app.platform.Context
import me.him188.ani.app.tools.ldc.ContentPolicy
import me.him188.ani.app.ui.comment.BangumiCommentSticker
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.AuthState
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.launchInMain
import me.him188.ani.app.ui.settings.danmaku.DanmakuRegexFilterState
import me.him188.ani.app.ui.subject.collection.components.AiringLabelState
import me.him188.ani.app.ui.subject.collection.components.EditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.components.comment.CommentContext
import me.him188.ani.app.ui.subject.components.comment.CommentEditorState
import me.him188.ani.app.ui.subject.components.comment.CommentLoader
import me.him188.ani.app.ui.subject.components.comment.CommentMapperContext
import me.him188.ani.app.ui.subject.components.comment.CommentState
import me.him188.ani.app.ui.subject.components.comment.EditCommentSticker
import me.him188.ani.app.ui.subject.components.comment.TurnstileState
import me.him188.ani.app.ui.subject.episode.details.EpisodeCarouselState
import me.him188.ani.app.ui.subject.episode.details.EpisodeDetailsState
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSourceInfoProvider
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSourceResultsPresentation
import me.him188.ani.app.ui.subject.episode.statistics.VideoStatistics
import me.him188.ani.app.ui.subject.episode.video.DanmakuLoaderImpl
import me.him188.ani.app.ui.subject.episode.video.DanmakuStatistics
import me.him188.ani.app.ui.subject.episode.video.DelegateDanmakuStatistics
import me.him188.ani.app.ui.subject.episode.video.LoadDanmakuRequest
import me.him188.ani.app.ui.subject.episode.video.PlayerLauncher
import me.him188.ani.app.ui.subject.episode.video.PlayerSkipOpEdState
import me.him188.ani.app.ui.subject.episode.video.VideoDanmakuState
import me.him188.ani.app.ui.subject.episode.video.VideoDanmakuStateImpl
import me.him188.ani.app.ui.subject.episode.video.sidesheet.EpisodeSelectorState
import me.him188.ani.app.videoplayer.ui.ControllerVisibility
import me.him188.ani.app.videoplayer.ui.VideoControllerState
import me.him188.ani.app.videoplayer.ui.state.PlaybackState
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import me.him188.ani.app.videoplayer.ui.state.PlayerStateFactory
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuEvent
import me.him188.ani.danmaku.api.DanmakuPresentation
import me.him188.ani.danmaku.ui.DanmakuConfig
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.api.topic.isDoneOrDropped
import me.him188.ani.datasources.bangumi.BANGUMI_NEXT_API_HOST
import me.him188.ani.utils.coroutines.CancellationException
import me.him188.ani.utils.coroutines.cancellableCoroutineScope
import me.him188.ani.utils.coroutines.runUntilSuccess
import me.him188.ani.utils.coroutines.sampleWithInitial
import me.him188.ani.utils.logging.info
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds


@Stable
abstract class EpisodeViewModel : AbstractViewModel(), HasBackgroundScope {
    abstract val videoSourceResolver: VideoSourceResolver

    abstract val subjectId: Int
    abstract val episodeId: StateFlow<Int>

    abstract val subjectPresentation: SubjectPresentation // by state
    abstract val episodePresentation: EpisodePresentation // by state

    abstract val authState: AuthState

    abstract val episodeDetailsState: EpisodeDetailsState

    /**
     * 剧集列表
     */
    abstract val episodeCarouselState: EpisodeCarouselState

    abstract val editableSubjectCollectionTypeState: EditableSubjectCollectionTypeState

    abstract var isFullscreen: Boolean

    abstract val commentLazyListState: LazyListState

    /**
     * 播放器内切换剧集
     */
    abstract val episodeSelectorState: EpisodeSelectorState

    // Media Fetching

    /**
     * "数据源" bottom sheet 内容
     */
    abstract val mediaSelectorPresentation: MediaSelectorPresentation

    /**
     * "数据源" bottom sheet 中的每个数据源的结果
     */
    abstract val mediaSourceResultsPresentation: MediaSourceResultsPresentation

    /**
     * "视频统计" bottom sheet 显示内容
     */
    abstract val videoStatistics: VideoStatistics

    // Media Selection

    /**
     * 是否显示数据源选择器
     */
    abstract var mediaSelectorVisible: Boolean

    abstract val mediaSourceInfoProvider: MediaSourceInfoProvider

    // Video
    abstract val videoControllerState: VideoControllerState
    abstract val videoScaffoldConfig: VideoScaffoldConfig

    // DanmakuRegexFilterState
    abstract val danmakuRegexFilterState: DanmakuRegexFilterState


    /**
     * Play controller for video view. This can be saved even when window configuration changes (i.e. everything recomposes).
     */
    abstract val playerState: PlayerState

    // Danmaku

    abstract val danmaku: VideoDanmakuState

    abstract val danmakuStatistics: DanmakuStatistics

    abstract val episodeCommentState: CommentState

    abstract val turnstileState: TurnstileState
    
    abstract val commentEditorState: CommentEditorState

    abstract val playerSkipOpEdState: PlayerSkipOpEdState

    @UiThread
    abstract fun stopPlaying()
}

fun EpisodeViewModel(
    initialSubjectId: Int,
    initialEpisodeId: Int,
    initialIsFullscreen: Boolean = false,
    context: Context,
): EpisodeViewModel = EpisodeViewModelImpl(initialSubjectId, initialEpisodeId, initialIsFullscreen, context)


@Stable
private class EpisodeViewModelImpl(
    override val subjectId: Int,
    initialDanmakuId: Int,
    initialIsFullscreen: Boolean = false,
    context: Context,
) : KoinComponent, EpisodeViewModel() {
    override val episodeId: MutableStateFlow<Int> = MutableStateFlow(initialDanmakuId)
    private val playerStateFactory: PlayerStateFactory by inject()
    private val subjectManager: SubjectManager by inject()
    private val mediaCacheManager: MediaCacheManager by inject()
    private val danmakuManager: DanmakuManager by inject()
    override val videoSourceResolver: VideoSourceResolver by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val danmakuRegexFilterRepository: DanmakuRegexFilterRepository by inject()
    private val mediaSourceManager: MediaSourceManager by inject()
    private val episodePreferencesRepository: EpisodePreferencesRepository by inject()
    private val commentRepository: CommentRepository by inject()
    private val episodePlayHistoryRepository: EpisodePlayHistoryRepository by inject()

    private val subjectInfo = subjectManager.subjectInfoFlow(subjectId).shareInBackground()
    private val episodeInfo =
        episodeId.transformLatest { episodeId ->
            emit(null) // 清空前端
            emitAll(subjectManager.episodeInfoFlow(episodeId))
        }.stateInBackground(null)

    // Media Selection

    // 会在更换 ep 时更换
    private val mediaFetchSession = episodeInfo.flatMapLatest { episodeInfo ->
        mediaSourceManager.createFetchFetchSessionFlow(
            if (episodeInfo == null) {
                emptyFlow()
            } else {
                subjectInfo.map { subjectInfo ->
                    MediaFetchRequest.create(subjectInfo, episodeInfo)
                }
            },
        )
    }.shareInBackground(started = SharingStarted.Lazily)

    override val videoControllerState = VideoControllerState(ControllerVisibility.Invisible)

    /**
     * 更换 EP 是否已经完成了.
     *
     * 之所以需要这个状态, 是因为当切换 EP 时, [mediaFetchSession] 会变更,
     * 随后 [MediaFetchSession.cumulativeResults] 会传递给 [mediaSelector],
     * 但是 [MediaSelector.mediaList] 是 share 在后台的, 也就是说它可能会在任意时间之后才会发现 [mediaFetchSession] 有更新.
     *
     * 这就导致当切换 EP 后, [MediaSelector.mediaList] 会有一段时间仍然是旧的值.
     *
     * 问题在于, [mediaFetchSession] 的变更会触发 [MediaSelectorAutoSelect.selectCached].
     * 自动选择可能比 [MediaSelector.mediaList] 更新更早, 所以自动选择就会用久的 `mediaList` 选择缓存, 导致将会播放旧的视频.
     *
     *
     * 因此我们增加 [switchEpisodeCompleted], 在操作 EP 时, 先将其设置为 `false`, 然后再修改 [episodeId],
     * 并在 [mediaSelector] 更新时设置为 true.
     *
     * 这样也并不是一定安全的, 有可能在我们修改 [episodeId] 后, 正好旧的查询触发了 [MediaSelector.mediaList] 更新,
     * 就导致 [switchEpisodeCompleted] 被设置为 `true`, [MediaSelectorAutoSelect.selectCached] 仍然会参考旧的 `mediaList` 选择缓存.
     *
     * 但是这种情况发生的概率比较小, 仅限于后台还有一个查询正在进行的时候用户切换了 EP, 并且旧的 EP 要至少有一个缓存, 而且恰好在一个比较短的时间内旧的查询完成了.
     *
     *
     * 一个更恰当的解决方法可能是把 [mediaSelector] 变成 flow. 当切换 EP 时直接把 [mediaSelector] 重新创建, 就不可能访问到旧的状态了.
     * 但是这会导致所有依赖 [mediaSelector] 的客户都更换为 flow 方式, 这很可能会导致更多问题. 因为我们先使用这个临时解决方案.
     */
    private val switchEpisodeCompleted = MutableStateFlow(false)

    private suspend inline fun awaitSwitchEpisodeCompleted() {
        switchEpisodeCompleted.first { it }
    }

    private val mediaSelector = MediaSelectorFactory.withKoin(getKoin())
        .create(
            subjectId,
            mediaFetchSession.flatMapLatest { it.cumulativeResults },
        )
        .apply {
            autoSelect.run {

                launchInBackground {
                    mediaFetchSession.collectLatest {
                        awaitSwitchEpisodeCompleted()
                        awaitCompletedAndSelectDefault(
                            it,
                            settingsRepository.mediaSelectorSettings.flow.map { it.preferKind },
                        )
                    }
                }
                launchInBackground {
                    mediaFetchSession.collectLatest {
                        awaitSwitchEpisodeCompleted()
                        selectCached(it)
                    }
                }

                launchInBackground {
                    mediaFetchSession.collectLatest {
                        awaitSwitchEpisodeCompleted()
                        if (settingsRepository.mediaSelectorSettings.flow.first().autoEnableLastSelected) {
                            autoEnableLastSelected(it)
                        }
                    }
                }
            }
            eventHandling.run {
                launchInBackground {
                    savePreferenceOnSelect {
                        episodePreferencesRepository.setMediaPreference(subjectId, it)
                    }
                }
            }
            launchInBackground {
                mediaList.collect {
                    switchEpisodeCompleted.value = true
                }
            }
        }

    override val mediaSourceInfoProvider: MediaSourceInfoProvider = MediaSourceInfoProvider(
        getSourceInfoFlow = { mediaSourceManager.infoFlowByMediaSourceId(it) },
    )

    override val mediaSelectorPresentation: MediaSelectorPresentation =
        MediaSelectorPresentation(mediaSelector, mediaSourceInfoProvider, backgroundScope.coroutineContext)

    override val mediaSourceResultsPresentation: MediaSourceResultsPresentation =
        MediaSourceResultsPresentation(
            FilteredMediaSourceResults(
                results = mediaFetchSession.mapLatest { it.mediaSourceResults },
                settings = settingsRepository.mediaSelectorSettings.flow,
            ),
            backgroundScope.coroutineContext,
        )

    override val playerState: PlayerState =
        playerStateFactory.create(context, backgroundScope.coroutineContext)

    /**
     * 保存播放进度的入口有4个：退出播放页，切换剧集，同集切换数据源，暂停播放
     * 其中 切换剧集 和 同集切换数据源 虽然都是切换数据源，但它们并不能合并成一个入口，
     * 因为 切换数据源 是依赖 PlayerLauncher collect mediaSelector.selected 实现的，
     * 它会在 mediaSelector.unselect() 任意时间后发现 selected 已经改变，导致 episodeId 可能已经改变，从而将当前集的播放进度保存到新的剧集中
     */
    private fun savePlayProgress() {
        if (playerState.state.value == PlaybackState.FINISHED) return
        val positionMillis = playerState.currentPositionMillis.value
        val epId = episodeId.value
        val durationMillis = playerState.videoProperties.value?.durationMillis.let {
            if (it == null) return@let 0L
            return@let max(0, it - 1000) // 最后一秒不会保存进度
        }
        if (positionMillis in 0..<durationMillis) {
            launchInBackground {
                episodePlayHistoryRepository.saveOrUpdate(epId, positionMillis)
            }
        }
    }

    private val playerLauncher: PlayerLauncher = PlayerLauncher(
        mediaSelector, videoSourceResolver, playerState, mediaSourceInfoProvider,
        episodeInfo,
        mediaFetchSession.flatMapLatest { it.hasCompleted }.map { !it.allCompleted() },
        backgroundScope.coroutineContext,
    )

    override val videoStatistics: VideoStatistics get() = playerLauncher.videoStatistics

    override var mediaSelectorVisible: Boolean by mutableStateOf(false)
    override val videoScaffoldConfig: VideoScaffoldConfig by settingsRepository.videoScaffoldConfig
        .flow.produceState(VideoScaffoldConfig.Default)

    override val danmakuRegexFilterState = DanmakuRegexFilterState(
        list = danmakuRegexFilterRepository.flow.produceState(emptyList()),
        add = {
            launchInBackground { danmakuRegexFilterRepository.add(it) }
        },
        edit = { regex, filter ->
            launchInBackground {
                danmakuRegexFilterRepository.update(filter.id, filter.copy(regex = regex))
            }
        },
        remove = {
            launchInBackground { danmakuRegexFilterRepository.remove(it) }
        },
        switch = {
            launchInBackground {
                danmakuRegexFilterRepository.update(it.id, it.copy(enabled = !it.enabled))
            }
        },
    )

    override val subjectPresentation: SubjectPresentation by subjectInfo
        .map {
            SubjectPresentation(title = it.displayName, info = it)
        }
        .produceState(SubjectPresentation.Placeholder)

    private val episodePresentationFlow =
        episodeId
            .flatMapLatest { episodeId ->
                subjectManager.episodeCollectionFlow(subjectId, episodeId, ContentPolicy.CACHE_ONLY)
            }.map {
                it.toPresentation()
            }
            .shareInBackground(SharingStarted.Eagerly)

    override val episodePresentation: EpisodePresentation by episodePresentationFlow
        .produceState(EpisodePresentation.Placeholder)
    override val authState: AuthState = AuthState()

    private val episodeCollectionsFlow = subjectManager.episodeCollectionsFlow(subjectId)
        .shareInBackground()

    private val subjectCollection = subjectManager.subjectCollectionFlow(subjectId)
        .shareInBackground()

    override val episodeDetailsState: EpisodeDetailsState = kotlin.run {
        EpisodeDetailsState(
            episodePresentation = episodePresentationFlow.filterNotNull().produceState(EpisodePresentation.Placeholder),
            subjectInfo = subjectInfo.produceState(SubjectInfo.Empty),
            airingLabelState = AiringLabelState(
                subjectCollection.map { it.airingInfo }.produceState(null),
                subjectCollection.map { SubjectProgressInfo.calculate(it) }.produceState(null),
            ),
        )
    }

    override val episodeCarouselState: EpisodeCarouselState = kotlin.run {
        val episodeCacheStatusListState by episodeCollectionsFlow.flatMapLatest { list ->
            combine(
                list.map { collection ->
                    mediaCacheManager.cacheStatusForEpisode(subjectId, collection.episode.id).map {
                        collection.episode.id to it
                    }
                },
            ) {
                it.toList()
            }
        }.produceState(emptyList())

        val collectionButtonEnabled = MutableStateFlow(false)
        EpisodeCarouselState(
            episodes = episodeCollectionsFlow.produceState(emptyList()),
            playingEpisode = episodeId.combine(episodeCollectionsFlow) { id, collections ->
                collections.firstOrNull { it.episode.id == id }
            }.produceState(null),
            cacheStatus = {
                episodeCacheStatusListState.firstOrNull { status ->
                    status.first == it.episode.id
                }?.second ?: EpisodeCacheStatus.NotCached
            },
            onSelect = {
                switchEpisode(it.episode.id)
            },
            onChangeCollectionType = { episode, it ->
                collectionButtonEnabled.value = false
                launchInBackground {
                    try {
                        subjectManager.setEpisodeCollectionType(
                            subjectId,
                            episodeId = episode.episode.id,
                            collectionType = it,
                        )
                    } finally {
                        collectionButtonEnabled.value = true
                    }
                }
            },
            backgroundScope = backgroundScope,
        )
    }

    override val editableSubjectCollectionTypeState: EditableSubjectCollectionTypeState =
        EditableSubjectCollectionTypeState(
            selfCollectionType = subjectCollection
                .map { it.collectionType }
                .produceState(UnifiedCollectionType.NOT_COLLECTED),
            hasAnyUnwatched = {
                val collections =
                    episodeCollectionsFlow.firstOrNull() ?: return@EditableSubjectCollectionTypeState true
                collections.any { !it.type.isDoneOrDropped() }
            },
            onSetSelfCollectionType = { subjectManager.setSubjectCollectionType(subjectId, it) },
            onSetAllEpisodesWatched = {
                subjectManager.setAllEpisodesWatched(subjectId)
            },
            backgroundScope,
        )

    override var isFullscreen: Boolean by mutableStateOf(initialIsFullscreen)
    override val commentLazyListState: LazyListState = LazyListState()

    fun switchEpisode(episodeId: Int) {
        savePlayProgress()
        episodeDetailsState.showEpisodes = false // 选择后关闭弹窗
        mediaSelector.unselect() // 否则不会自动选择
        playerState.stop()
        switchEpisodeCompleted.value = false // 要在修改 episodeId 之前才安全, 但会有极小的概率在 fetchSession 更新前有 mediaList 更新
        this.episodeId.value = episodeId // ep 要在取消选择 media 之后才能变, 否则会导致使用旧的 media
    }

    override val episodeSelectorState: EpisodeSelectorState = EpisodeSelectorState(
        itemsFlow = episodeCollectionsFlow.map { list -> list.map { it.toPresentation() } },
        onSelect = {
            switchEpisode(it.episodeId)
        },
        currentEpisodeId = episodeId,
        parentCoroutineContext = backgroundScope.coroutineContext,
    )

    private val danmakuLoader = DanmakuLoaderImpl(
        requestFlow = mediaFetchSession.transformLatest {
            emit(null)
            emitAll(
                playerState.videoData.mapLatest {
                    if (it == null) {
                        return@mapLatest null
                    }
                    LoadDanmakuRequest(
                        subjectInfo.first(),
                        episodeInfo.filterNotNull().first(),
                        episodeId.value,
                        it.filename,
                        it.fileLength,
                    )
                },
            )
        },
        currentPosition = playerState.currentPositionMillis.map { it.milliseconds },
        danmakuFilterConfig = settingsRepository.danmakuFilterConfig.flow,
        danmakuRegexFilterList = danmakuRegexFilterRepository.flow,
        onFetch = {
            danmakuManager.fetch(it)
        },
        backgroundScope.coroutineContext,
    )

    override val danmakuStatistics: DanmakuStatistics = DelegateDanmakuStatistics(
        danmakuLoader.state.produceState(),
    )

    override val danmaku = VideoDanmakuStateImpl(
        danmakuEnabled = settingsRepository.danmakuEnabled.flow.produceState(false),
        danmakuConfig = settingsRepository.danmakuConfig.flow.produceState(DanmakuConfig.Default),
        onSend = { info ->
            danmakuManager.post(episodeId.value, info)
        },
        onSetEnabled = {
            settingsRepository.danmakuEnabled.set(it)
        },
        onHideController = {
            videoControllerState.toggleFullVisible(false)
        },
        backgroundScope,
    )

    private val episodeCommentLoader = CommentLoader.createForEpisode(
        episodeId = episodeId,
        coroutineContext = backgroundScope.coroutineContext,
        episodeCommentSource = { commentRepository.getSubjectEpisodeComments(it) },
    )

    override val episodeCommentState: CommentState = CommentState(
        sourceVersion = episodeCommentLoader.sourceVersion.produceState(null),
        list = episodeCommentLoader.list.produceState(emptyList()),
        hasMore = episodeCommentLoader.hasFinished.map { !it }.produceState(true),
        onReload = { episodeCommentLoader.reload() },
        onLoadMore = { episodeCommentLoader.loadMore() },
        onSubmitCommentReaction = { _, _ -> },
        backgroundScope = backgroundScope,
    )
    
    override val turnstileState = TurnstileState(
        "${BANGUMI_NEXT_API_HOST}/turnstile?redirect_uri=$${TurnstileState.CALLBACK_INTERCEPTION_PREFIX}"
    )

    override val commentEditorState: CommentEditorState = CommentEditorState(
        showExpandEditCommentButton = true,
        initialEditExpanded = false,
        panelTitle = subjectInfo
            .combine(episodeInfo) { sub, epi -> "${sub.displayName} ${epi?.renderEpisodeEp()}" }
            .produceState(null),
        stickers = flowOf(BangumiCommentSticker.map { EditCommentSticker(it.first, it.second) })
            .produceState(emptyList()),
        richTextRenderer = { text ->
            withContext(Dispatchers.Default) {
                with(CommentMapperContext) { parseBBCode(text) }
            }
        },
        // 最多尝试 3 次发送, 如果都失败了就抛出一个异常
        onSend = { context, content ->
            var turnstileResponse: CompletableDeferred<String>? = null
            var triedCount = 0
            
            coroutineScope { 
                val tokenCollectJob = launch { 
                    turnstileState.tokenFlow.collectLatest { 
                        turnstileResponse?.complete(it)
                    }
                }
                
                launch {
                    while (triedCount < 3) {
                        withContext(Dispatchers.Main) { turnstileState.reload() }
                        val turnstileToken = CompletableDeferred<String>()
                            .also { turnstileResponse = it }
                            .run { withTimeoutOrNull(10000L.milliseconds) { await() } }
                        
                        if (turnstileToken == null) {
                            triedCount ++
                            continue
                        }
                        
                        val response = when (context) {
                            is CommentContext.Episode ->
                                commentRepository.postEpisodeComment(episodeId.value, content, turnstileToken)

                            is CommentContext.Reply ->
                                commentRepository.postEpisodeComment(
                                    episodeId.value, 
                                    content,
                                    turnstileToken, 
                                    context.commentId
                                )

                            is CommentContext.Subject -> ApiResponse.success(Unit) // TODO: send subject comment
                        }
                        
                        val error = response.failureOrNull()
                        if (error == null) { // cancel all jobs in this scope to complete.
                            tokenCollectJob.cancel()
                            return@launch
                        }
                        if (error is ApiFailure.Unauthorized) {
                            triedCount ++
                            continue
                        } else {
                            throw CancellationException("sending command is cancelled, because api returned an error $error")
                        }
                    }
                    throw CancellationException("sending command is cancelled, because the maximum number of attempts is reached.")
                }
            }
        },
        backgroundScope = backgroundScope,
    )

    override val playerSkipOpEdState: PlayerSkipOpEdState = PlayerSkipOpEdState(
        chapters = playerState.chapters.produceState(),
        onSkip = {
            playerState.seekTo(it)
        },
        videoLength = playerState.videoProperties.mapNotNull { it?.durationMillis?.milliseconds }
            .produceState(0.milliseconds),
    )

    override fun stopPlaying() {
        // 退出播放页前保存播放进度
        savePlayProgress()
        playerState.stop()
    }

    private val selfUserId = danmakuManager.selfId

    init {
        launchInMain { // state changes must be in main thread
            playerState.state.collect {
                danmaku.danmakuHostState.setPaused(!it.isPlaying)
            }
        }

        launchInBackground {
            cancellableCoroutineScope {
                val selfId = selfUserId.stateIn(this)
                danmakuLoader.eventFlow.collect { event ->
                    when (event) {
                        is DanmakuEvent.Add -> {
                            val data = event.danmaku
                            if (data.text.isBlank()) return@collect
                            danmaku.danmakuHostState.trySend(
                                createDanmakuPresentation(data, selfId.value),
                            )
                        }

                        is DanmakuEvent.Repopulate -> {
                            danmaku.danmakuHostState.repopulate(
                                event.list
                                    .filter { it.text.any { c -> !c.isWhitespace() } }
                                    .map { createDanmakuPresentation(it, selfId.value) },
                            )

                        }
                    }
                }
                cancelScope()
            }
        }

        // 自动标记看完
        launchInBackground {
            settingsRepository.videoScaffoldConfig.flow
                .map { it.autoMarkDone }
                .distinctUntilChanged()
                .debounce(1000)
                .collectLatest { enabled ->
                    if (!enabled) return@collectLatest

                    // 设置启用

                    mediaFetchSession.collectLatest {
                        cancellableCoroutineScope {
                            combine(
                                playerState.currentPositionMillis.sampleWithInitial(5000),
                                playerState.videoProperties.map { it?.durationMillis }.debounce(5000),
                                playerState.state,
                            ) { pos, max, playback ->
                                if (max == null || !playback.isPlaying) return@combine
                                if (episodePresentationFlow.first().collectionType == UnifiedCollectionType.DONE) {
                                    cancelScope() // 已经看过了
                                }
                                if (pos > max.toFloat() * 0.9) {
                                    logger.info { "观看到 90%, 标记看过" }
                                    runUntilSuccess(maxAttempts = 5) {
                                        subjectManager.setEpisodeCollectionType(
                                            subjectId,
                                            episodeId.value,
                                            UnifiedCollectionType.DONE,
                                        )
                                    }
                                    cancelScope() // 标记成功一次后就不要再检查了
                                }
                            }.collect()
                        }
                    }
                }
        }

        launchInBackground {
            settingsRepository.videoScaffoldConfig.flow
                .map { it.autoPlayNext }
                .distinctUntilChanged()
                .collectLatest { enabled ->
                    if (!enabled) return@collectLatest

                    playerState.state.collect { playback ->
                        if (playback != PlaybackState.FINISHED) return@collect
                        launchInMain {// state changes must be in main thread
                            logger.info("播放完毕，切换下一集")
                            episodeSelectorState.takeIf { it.hasNextEpisode }?.selectNext()
                        }
                    }
                }
        }

        // 跳过 OP 和 ED
        launchInBackground {
            settingsRepository.videoScaffoldConfig.flow
                .map { it.autoSkipOpEd }
                .distinctUntilChanged()
                .debounce(1000)
                .collectLatest { enabled ->
                    if (!enabled) return@collectLatest

                    // 设置启用
                    combine(
                        playerState.currentPositionMillis.sampleWithInitial(1000),
                        episodeId,
                        episodeCollectionsFlow,
                    ) { pos, id, collections ->
                        // 不止一集并且当前是第一集时不跳过
                        if (collections.size > 1 && collections.getOrNull(0)?.episode?.id == id) return@combine
                        playerSkipOpEdState.update(pos)
                    }.collect()
                }
        }

        launchInBackground {
            mediaSelector.events.onBeforeSelect.collect {
                // 切换 数据源 前保存播放进度
                savePlayProgress()
            }
        }
        launchInBackground {
            playerState.state.collect {
                when (it) {
                    // 加载播放进度
                    PlaybackState.READY -> {
                        val positionMillis =
                            episodePlayHistoryRepository.getPositionMillisByEpisodeId(episodeId = episodeId.value)
                        positionMillis?.let {
                            withContext(Dispatchers.Main) { // android must call in main thread
                                playerState.seekTo(positionMillis)
                            }
                        }
                    }

                    PlaybackState.PAUSED -> savePlayProgress()

                    PlaybackState.FINISHED ->
                        episodePlayHistoryRepository.remove(episodeId.value)

                    else -> Unit
                }
            }
        }
    }

    private fun createDanmakuPresentation(
        data: Danmaku,
        selfId: String?,
    ) = DanmakuPresentation(
        data,
        isSelf = selfId == data.senderId,
    )
}
