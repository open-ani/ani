package me.him188.ani.app.ui.subject.cache

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import io.ktor.util.logging.error
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.transform
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.data.media.cache.EpisodeCacheRequester
import me.him188.ani.app.data.media.cache.MediaCache
import me.him188.ani.app.data.media.cache.MediaCacheStorage
import me.him188.ani.app.data.media.fetch.EpisodeMediaFetchSession
import me.him188.ani.app.data.models.MediaSelectorSettings
import me.him188.ani.app.data.repositories.BangumiEpisodeRepository
import me.him188.ani.app.data.repositories.BangumiSubjectRepository
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.app.data.subject.episode
import me.him188.ani.app.data.subject.isKnownBroadcast
import me.him188.ani.app.data.subject.nameCnOrName
import me.him188.ani.app.data.subject.type
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.feedback.ErrorDialogHost
import me.him188.ani.app.ui.foundation.feedback.ErrorMessage
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.topic.contains
import me.him188.ani.datasources.api.unwrapCached
import me.him188.ani.utils.coroutines.runUntilSuccess
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class SubjectCacheViewModel(
    val subjectId: Int,
) : AbstractViewModel(), KoinComponent {
    private val bangumiSubjectRepository: BangumiSubjectRepository by inject()
    private val bangumiEpisodeRepository: BangumiEpisodeRepository by inject()
    private val subjectManager: SubjectManager by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val cacheManager: MediaCacheManager by inject()

    private val cacheStoragesPlaceholder = ArrayList<MediaCacheStorage>(0)

    val cacheStorages by cacheManager.enabledStorages.produceState(cacheStoragesPlaceholder)

    private val episodeCacheRequester = EpisodeCacheRequester(
        settings = settingsRepository.mediaSelectorSettings.flow,
        createFetchSession = ::EpisodeMediaFetchSession,
        backgroundScope.coroutineContext
    )

    val episodeCacheRequesterState = EpisodeCacheRequesterPresentation(
        episodeCacheRequester,
        cacheManager.enabledStorages,
        settingsNotCached = settingsRepository.mediaSelectorSettings.flow,
        onComplete = { completed ->
            launchInBackground {
                addCache(completed.media, completed.mediaCacheMetadata, completed.storage)
            }
        },
        backgroundScope.coroutineContext,
    )

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

    private val subjectInfoFlow = flowOf(subjectId).mapLatest { subjectId ->
        runUntilSuccess(Int.MAX_VALUE) { subjectManager.getSubjectInfo(subjectId) }
    }.shareInBackground()

    val subjectTitle by subjectInfoFlow.map { it.nameCnOrName }.produceState(null)

    val errorMessage: MutableStateFlow<ErrorMessage?> = MutableStateFlow(null)

    private val episodeCollectionsFlowNotCached = flowOf(subjectId).mapLatest { subjectId ->
        runUntilSuccess(Int.MAX_VALUE) { subjectManager.episodeCollectionsFlow(subjectId).first() }
    }

    /**
     * State of the subject cache page.
     */
    val stateFlow: SharedFlow<SubjectCacheState> = episodeCollectionsFlowNotCached.flatMapLatest { episodes ->
        // 每个 episode 都为一个 flow, 然后合并
        val f = episodes.map { episodeCollection ->
            val episode = episodeCollection.episode // TODO: replace with EpisodeInfo 

            val cacheStatusFlow = cacheManager.cacheStatusForEpisode(subjectId, episode.id)

            cacheStatusFlow.transform { cacheStatus ->
                val state = EpisodeCacheState(
                    episodeId = episode.id,
                    sort = episode.sort,
                    ep = episode.ep,
                    title = episode.nameCn,
                    watchStatus = episodeCollection.type,
                    cacheStatus = cacheStatus,
                    hasPublished = episode.isKnownBroadcast,
                    originMedia = null,
                )
                emit(state)
                val cachedMedia = cacheStatus.cache?.getCachedMedia()
                emit(state.copy(originMedia = cachedMedia))
            }
        }
        // f extracted because of compiler inference bug
        combine(f) {
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

    suspend fun addCacheFromExisting(
        existing: EpisodeCacheState,
        metadata: MediaCacheMetadata,
        new: EpisodeCacheState,
    ) {
        val subjectInfo = subjectInfoFlow.first()
        addCache(
            existing.originMedia!!.unwrapCached(),
            MediaCacheMetadata(
                subjectId = metadata.subjectId,
                episodeId = new.episodeId.toString(),
                subjectNames = subjectInfo.allNames.toSet(), // update names
                episodeSort = new.sort,
                episodeName = new.title,
                episodeEp = new.ep,
            ),
            findStorageByCache(existing.mediaCache),
        )
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
                addCacheFromExisting(existing, metadata, episodeCacheState)
            }
        }
    }

    showMediaSelector?.let { episodeCacheState ->
        EpisodeCacheRequesterView(
            state = vm.episodeCacheRequesterState,
            episodeCacheState = episodeCacheState,
        )
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
