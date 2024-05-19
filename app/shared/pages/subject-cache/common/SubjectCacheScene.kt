package me.him188.ani.app.ui.subject.cache

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.ktor.util.logging.error
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.data.repositories.EpisodeRepository
import me.him188.ani.app.data.repositories.SubjectRepository
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.feedback.ErrorDialogHost
import me.him188.ani.app.ui.feedback.ErrorMessage
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.subject.episode.mediaFetch.EpisodeMediaFetchSession
import me.him188.ani.app.ui.subject.episode.mediaFetch.FetcherMediaSelectorConfig
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.bangumi.processing.isOnAir
import me.him188.ani.datasources.bangumi.processing.nameCNOrName
import me.him188.ani.datasources.bangumi.processing.toCollectionType
import me.him188.ani.datasources.core.cache.MediaCacheStorage
import me.him188.ani.utils.coroutines.runUntilSuccess
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.EpType

@Stable
class SubjectCacheViewModel(
    val subjectId: Int,
) : AbstractViewModel(), KoinComponent {
    private val subjectRepository: SubjectRepository by inject()
    private val episodeRepository: EpisodeRepository by inject()
    private val cacheManager: MediaCacheManager by inject()

    private val cacheStoragesPlaceholder = ArrayList<MediaCacheStorage>(0)

    val cacheStorages by cacheManager.enabledStorages.produceState(cacheStoragesPlaceholder)
    val cacheStoragesLoaded by derivedStateOf {
        cacheStorages !== cacheStoragesPlaceholder
    }


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
    val state = episodeCollections.flatMapLatest { episodes ->
        // 每个 episode 都为一个 flow, 然后合并
        combine(episodes.map { episodeCollection ->
            val episode = episodeCollection.episode

            val cacheStatus = cacheManager.cacheStatusForEpisode(subjectId, episode.id)

            cacheStatus.map {
                EpisodeCacheState(
                    episodeId = episode.id,
                    sort = EpisodeSort(episode.sort),
                    title = episode.nameCn,
                    watchStatus = episodeCollection.type.toCollectionType(),
                    cacheStatus = it,
                    hasPublished = episode.isOnAir() != true,
                )
            }
        }) {
            DefaultSubjectCacheState(it.toList())
        }
    }.flowOn(Dispatchers.Default).shareInBackground()

    suspend fun addCache(
        media: Media,
        metadata: suspend () -> MediaFetchRequest,
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
                storage.cache(media, MediaCacheMetadata(metadata()))
                errorMessage.value = null
            } catch (_: CancellationException) {
            } catch (e: Throwable) {
                logger.error(IllegalStateException("Failed to create cache", e))
                errorMessage.emit(ErrorMessage.simple("缓存失败", e))
            }
        }
    }

    fun deleteCache(episodeId: Int) {
        launchInBackground {
            val epString = episodeId.toString()
            for (storage in cacheManager.enabledStorages.first()) {
                for (mediaCache in storage.listFlow.first()) {
                    if (mediaCache.metadata.episodeId == epString) {
                        storage.delete(mediaCache)
                    }
                }
            }
        }
    }
}

@Stable
private val emptyDefaultSubjectCacheState = DefaultSubjectCacheState(emptyList())

@Composable
fun SubjectCacheScene(
    vm: SubjectCacheViewModel,
    onClickGlobalCacheSettings: () -> Unit,
    onClickGlobalCacheManage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by vm.state.collectAsStateWithLifecycle(emptyDefaultSubjectCacheState)

    ErrorDialogHost(vm.errorMessage)

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
        mediaSelector = { episodeCacheState, dismissSelector ->
            val epFetch = remember(vm.subjectId, episodeCacheState.episodeId) {
                EpisodeMediaFetchSession(
                    vm.subjectId,
                    episodeCacheState.episodeId,
                    vm.backgroundScope.coroutineContext,
                    FetcherMediaSelectorConfig.NoAutoSelect
                )
            }

            var selectedMedia by remember(vm.subjectId, episodeCacheState.episodeId) { mutableStateOf<Media?>(null) }

            if (selectedMedia != null) {
                MediaCacheStorageSelector(
                    remember(vm) { MediaCacheStorageSelectorState(vm.cacheStorages) },
                    onSelect = { storage ->
                        selectedMedia?.let { media ->
                            vm.launchInBackground {
                                addCache(
                                    media,
                                    metadata = { epFetch.mediaFetchSession.map { it.request }.first() },
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

            ModalBottomSheet(
                dismissSelector,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                EpisodeCacheMediaSelector(
                    epFetch.mediaSelectorState,
                    onSelect = { media ->
                        if (vm.cacheStorages.size == 1) {
                            val storage = vm.cacheStorages.first()
                            vm.launchInBackground {
                                addCache(
                                    media,
                                    metadata = { epFetch.mediaFetchSession.map { it.request }.first() },
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
                    Modifier.navigationBarsPadding(),
                    progressProvider = { epFetch.mediaFetcherProgress }
                )
            }
        },
        modifier,
    )
}
