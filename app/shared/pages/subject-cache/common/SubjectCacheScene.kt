package me.him188.ani.app.ui.subject.cache

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

    val cacheStorages get() = cacheManager.storages

    val subjectTitle by flowOf(subjectId).mapLatest { subjectId ->
        runUntilSuccess { subjectRepository.getSubject(subjectId)!! }
            .nameCNOrName()
    }.produceState(null)

    // All episodes
    private val episodeCollections = flowOf(subjectId).mapLatest { subjectId ->
        runUntilSuccess { episodeRepository.getSubjectEpisodeCollection(subjectId, EpType.MainStory).toList() }
    }.filterNotNull()

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
    }.flowOn(Dispatchers.Default)

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
                errorMessage.emit(ErrorMessage.simple("缓存失败", e))
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
                            vm.launchInBackground {
                                addCache(
                                    media,
                                    metadata = { epFetch.mediaFetchSession.map { it.request }.first() },
                                    vm.cacheStorages.single()
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
        }, modifier
    )
}
