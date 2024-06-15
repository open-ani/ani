package me.him188.ani.app.ui.subject.cache

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import io.ktor.util.logging.error
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.data.models.MediaSelectorSettings
import me.him188.ani.app.data.repositories.EpisodeRepository
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.data.repositories.SubjectRepository
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.feedback.ErrorDialogHost
import me.him188.ani.app.ui.foundation.feedback.ErrorMessage
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.rememberBackgroundScope
import me.him188.ani.app.ui.subject.episode.mediaFetch.EpisodeMediaFetchSession
import me.him188.ani.app.ui.subject.episode.mediaFetch.FetcherMediaSelectorConfig
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.rememberMediaSelectorSourceResults
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.topic.contains
import me.him188.ani.datasources.api.unwrapCached
import me.him188.ani.datasources.bangumi.processing.isOnAir
import me.him188.ani.datasources.bangumi.processing.nameCNOrName
import me.him188.ani.datasources.bangumi.processing.toCollectionType
import me.him188.ani.datasources.core.cache.MediaCache
import me.him188.ani.datasources.core.cache.MediaCacheStorage
import me.him188.ani.utils.coroutines.runUntilSuccess
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.EpType
import org.openapitools.client.models.UserEpisodeCollection

@Stable
class SubjectCacheViewModel(
    val subjectId: Int,
) : AbstractViewModel(), KoinComponent {
    private val subjectRepository: SubjectRepository by inject()
    private val episodeRepository: EpisodeRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val cacheManager: MediaCacheManager by inject()

    private val cacheStoragesPlaceholder = ArrayList<MediaCacheStorage>(0)

    val cacheStorages by cacheManager.enabledStorages.produceState(cacheStoragesPlaceholder)
    val cacheStoragesLoaded by derivedStateOf {
        cacheStorages !== cacheStoragesPlaceholder
    }

    suspend fun findStorageByCache(cache: MediaCache?): MediaCacheStorage {
        val vm = this
        if (cache == null) {
            return vm.cacheStorages.first()
        }
        return vm.cacheStorages.singleOrNull()
            ?: vm.cacheStorages.firstOrNull { list ->
                list.listFlow.first().any { it == cache }
            }
            ?: vm.cacheStorages.first()
    }

    val mediaSelectorSettings: MediaSelectorSettings by settingsRepository.mediaSelectorSettings.flow
        .produceState(MediaSelectorSettings.Default)

    val subjectTitle by flowOf(subjectId).mapLatest { subjectId ->
        runUntilSuccess { subjectRepository.getSubject(subjectId)!! }
            .nameCNOrName()
    }.produceState(null)

    // All episodes
    private val episodeCollections = flowOf(subjectId).mapLatest { subjectId ->
        runUntilSuccess { episodeRepository.getSubjectEpisodeCollection(subjectId, EpType.MainStory).toList() }
    }.filterNotNull().shareInBackground()

    val errorMessage: MutableStateFlow<ErrorMessage?> = MutableStateFlow(null)

    /**
     * State of the subject cache page.
     */
    val stateFlow: SharedFlow<SubjectCacheState> = episodeCollections.flatMapLatest { episodes ->
        // 每个 episode 都为一个 flow, 然后合并
        @Suppress("RemoveExplicitTypeArguments") // compiler bug
        combine(episodes.map<UserEpisodeCollection, Flow<EpisodeCacheState>> { episodeCollection ->
            val episode = episodeCollection.episode // TODO: replace with EpisodeInfo 

            val cacheStatusFlow = cacheManager.cacheStatusForEpisode(subjectId, episode.id)

            cacheStatusFlow.transform { cacheStatus ->
                val state = EpisodeCacheState(
                    episodeId = episode.id,
                    sort = EpisodeSort(episode.sort),
                    ep = episode.ep?.let { EpisodeSort(it) },
                    title = episode.nameCn,
                    watchStatus = episodeCollection.type.toCollectionType(),
                    cacheStatus = cacheStatus,
                    hasPublished = episode.isOnAir() != true,
                    originMedia = null,
                )
                emit(state)
                val cachedMedia = cacheStatus.cache?.getCachedMedia()
                emit(state.copy(originMedia = cachedMedia))
            }
        }) {
            SubjectCacheState(it.toList())
        }
    }.flowOn(Dispatchers.Default).shareInBackground()

    /**
     * @see EpisodeCacheState.mediaEpisodeRange
     */
    val firstIncludedCache by stateFlow.map { state ->
        state.episodes.firstOrNull { it.mediaEpisodeRange != null }
    }.flowOn(Dispatchers.Default).produceState(null)

    suspend fun addCache(
        media: Media,
        request: suspend () -> MediaFetchRequest,
        storage: MediaCacheStorage,
    ) {
        return addCache(
            media,
            metadata = MediaCacheMetadata(request()),
            storage,
        )
    }

    suspend fun addCache(
        media: Media,
        metadata: MediaCacheMetadata,
        storage: MediaCacheStorage,
    ) {
        coroutineScope {
            errorMessage.emit(
                ErrorMessage.processing(
                    "正在创建缓存",
                    onCancel = {
                        cancel()
                    }
                )
            )
            try {
                storage.cache(media, metadata)
                errorMessage.value = null
            } catch (_: CancellationException) {
            } catch (e: Throwable) {
                logger.error(IllegalStateException("Failed to create cache", e))
                errorMessage.emit(ErrorMessage.simple("缓存失败", e))
            }
        }
    }

    fun deleteCache(episodeId: Int) {
        if (errorMessage.value != null) return
        val job = launchInBackground(start = CoroutineStart.LAZY) {
            val epString = episodeId.toString()
            try {
                for (storage in cacheManager.enabledStorages.first()) {
                    for (mediaCache in storage.listFlow.first()) {
                        if (mediaCache.metadata.episodeId == epString) {
                            storage.delete(mediaCache)
                        }
                    }
                }
                errorMessage.value = null
            } catch (e: Exception) {
                errorMessage.value = ErrorMessage.simple("删除缓存失败", e)
            }
        }
        errorMessage.value = ErrorMessage.processing("正在删除缓存", onCancel = { job.cancel() })
        job.start()
    }
}

@Stable
private val emptyDefaultSubjectCacheState = SubjectCacheState(emptyList())

@Composable
fun SubjectCacheScene(
    vm: SubjectCacheViewModel,
    onClickGlobalCacheSettings: () -> Unit,
    onClickGlobalCacheManage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by vm.stateFlow.collectAsStateWithLifecycle(emptyDefaultSubjectCacheState)

    ErrorDialogHost(vm.errorMessage)

    var showMediaSelector by remember { mutableStateOf<EpisodeCacheState?>(null) }

    LaunchedEffect(vm) {
        snapshotFlow { state.selectedEpisode }.collect { episodeCacheState ->
            if (episodeCacheState == null) {
                showMediaSelector = null
                return@collect
            }

            val existing = vm.firstIncludedCache
            if (existing == null) { // 没有缓存过季度全集
                showMediaSelector = episodeCacheState
                return@collect
            }

            showMediaSelector = null
            val metadata = existing.mediaCache?.metadata
            if (existing.originMedia == null || metadata == null) { // 无效缓存
                showMediaSelector = episodeCacheState
                return@collect
            }

            val episodeRange = existing.mediaEpisodeRange ?: run {
                showMediaSelector = episodeCacheState
                return@collect
            }
            if (episodeCacheState.sort !in episodeRange
                && (episodeCacheState.ep == null || episodeCacheState.ep !in episodeRange)
            ) {
                // 未找到剧集
                showMediaSelector = episodeCacheState
                return@collect
            }

            // 直接创建
            vm.launchInBackground {
                addCache(
                    existing.originMedia.unwrapCached(),
                    MediaCacheMetadata(
                        subjectId = metadata.subjectId,
                        episodeId = episodeCacheState.episodeId.toString(),
                        subjectNames = metadata.subjectNames,
                        episodeSort = episodeCacheState.sort,
                        episodeName = episodeCacheState.title,
                        episodeEp = episodeCacheState.ep,
                    ),
                    vm.findStorageByCache(existing.mediaCache),
                )
            }
        }
    }

    showMediaSelector?.let { episodeCacheState ->
        val dismissSelector = remember { { showMediaSelector = null } }
        val backgroundScope = rememberBackgroundScope() // 关掉窗口就立即停止查询

        val epFetch = remember(vm.subjectId, episodeCacheState.episodeId) {
            EpisodeMediaFetchSession(
                vm.subjectId,
                episodeCacheState.episodeId,
                backgroundScope.backgroundScope.coroutineContext,
                FetcherMediaSelectorConfig(
                    // 手动缓存的时候要保存设置, 但不要自动选择
                    savePreferenceChanges = true,
                    autoSelectOnFetchCompletion = false,
                    autoSelectLocal = false,
                )
            )
        }
        val mediaSelectorPresentation = remember(epFetch, backgroundScope) {
            MediaSelectorPresentation(epFetch.mediaSelector, backgroundScope.backgroundScope)
        }

        var selectedMedia by remember(vm.subjectId, episodeCacheState.episodeId) { mutableStateOf<Media?>(null) }

        if (selectedMedia != null) {
            BasicAlertDialog(
                {},
                properties = DialogProperties(
                    dismissOnBackPress = false, dismissOnClickOutside = false
                )
            ) {
                MediaCacheStorageSelector(
                    remember(vm) { MediaCacheStorageSelectorState(vm.cacheStorages) },
                    onSelect = { storage ->
                        selectedMedia?.let { media ->
                            vm.launchInBackground {
                                addCache(
                                    media,
                                    request = { epFetch.mediaFetchSession.map { it.request }.first() },
                                    storage
                                )
                                withContext(Dispatchers.Main) {
                                    dismissSelector()
                                }
                            }
                        }
                    },
                    onDismissRequest = {
                        selectedMedia = null
                    }
                )
            }
        }

        ModalBottomSheet(
            dismissSelector,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            EpisodeCacheMediaSelector(
                mediaSelectorPresentation,
                onSelect = { media ->
                    if (vm.cacheStorages.size == 1) {
                        val storage = vm.cacheStorages.first()
                        vm.launchInBackground {
                            addCache(
                                media,
                                request = { epFetch.mediaFetchSession.map { it.request }.first() },
                                storage,
                            )
                            withContext(Dispatchers.Main) {
                                dismissSelector()
                            }
                        }
                    } else {
                        selectedMedia = media
                    }
                },
                onCancel = dismissSelector,
                sourceResults = rememberMediaSelectorSourceResults(
                    settingsProvider = { vm.mediaSelectorSettings }
                ) { epFetch.sourceResults },
                Modifier.fillMaxHeight().navigationBarsPadding() // 防止添加筛选后数量变少导致 bottom sheet 高度变化
            )
        }
    }

    return SubjectCachePage(
        state,
        subjectTitle = {
            val title = vm.subjectTitle
            Text(title.orEmpty(), Modifier.placeholder(title == null))
        },
        onClickGlobalCacheSettings,
        onClickGlobalCacheManage,
        onDeleteCache = { episodeCacheState ->
            vm.deleteCache(episodeCacheState.episodeId)
        },
        modifier,
    )
}
