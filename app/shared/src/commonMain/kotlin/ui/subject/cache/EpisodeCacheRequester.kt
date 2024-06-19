package me.him188.ani.app.ui.subject.cache

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.media.cache.EpisodeCacheRequester
import me.him188.ani.app.data.media.cache.MediaCacheStorage
import me.him188.ani.app.data.media.selector.MediaSelector
import me.him188.ani.app.data.models.MediaSelectorSettings
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSourceResultsPresentation
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.utils.coroutines.onReplacement
import kotlin.coroutines.CoroutineContext

/**
 * [EpisodeCacheRequesterView] 的 UI 状态.
 */
@Stable
class EpisodeCacheRequesterPresentation(
    private val requester: EpisodeCacheRequester,
    private val storageNotCached: Flow<List<MediaCacheStorage>>,
    private val settingsNotCached: Flow<MediaSelectorSettings>,
    private val onComplete: suspend (CompletedEpisodeCacheRequest) -> Unit,
    parentCoroutineContext: CoroutineContext,
) : HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    private val fetchSession by requester.fetchSession.flatMapLatest { it.mediaFetchSession }.produceState(null)
    val mediaSourceResults = MediaSourceResultsPresentation(requester.sourceResults, backgroundScope.coroutineContext)

    val mediaSelectorSettings by settingsNotCached.produceState(null)

    val ongoingSelection: MediaSelectorPresentation? by requester.fetchSession.map { session ->
        MediaSelectorPresentation(
            session.mediaSelector, backgroundScope.coroutineContext
        )
    }.onReplacement {
        it.close()
    }.produceState(null)

    val cacheStorages by storageNotCached.produceState(emptyList())

    // 当 mediaSelectorPresentation 不为 `null` 时不一定是用户正在请求
    val isMediaSelectorVisible by requester.request.map { it != null }.produceState(false)

    /**
     * 开始请求缓存一个剧集. 将会展示 [MediaSelector].
     */
    fun request(subjectId: Int, episodeId: Int) {
        selectedMedia = null
        requester.request(subjectId, episodeId)
    }

    fun cancelRequest() {
        selectedMedia = null
        showStorageDialog = false
        requester.cancelRequest()
//        cacheTasker.cancel() // 不停止这个
    }

    /**
     * 用户从 [MediaSelector] 选择的 [Media].
     *
     * 不为 `null` 则需要展示选择存储位置 ([SelectMediaStorageDialog]).
     */
    var selectedMedia by mutableStateOf<Media?>(null)
        private set

    /**
     * 当用户从 [MediaSelector] 选择了一个 [Media] 时调用. 将会显示选择存储位置 ([SelectMediaStorageDialog])
     */
    fun selectMedia(media: Media) {
        cacheTasker.cancel()
        selectedMedia = media

        val singleStorage = cacheStorages.singleOrNull()
        if (singleStorage != null) {
            showStorageDialog = false
            selectStorage(singleStorage) // 只有一个, 就不用弹选择了
        } else {
            showStorageDialog = true
        }
    }

    /**
     * 显示 [SelectMediaStorageDialog]
     */
    var showStorageDialog by mutableStateOf(false)

    // 每次只执行一个缓存
    private val cacheTasker = MonoTasker(backgroundScope)

    var processingMedia by mutableStateOf<Media?>(null)
        private set

    /**
     * 用户从 [SelectMediaStorageDialog] 选择了一个存储位置时调用.
     */
    fun selectStorage(storage: MediaCacheStorage) {
        val selectedMedia = selectedMedia ?: return
        val fetchSession = fetchSession ?: return
        showStorageDialog = false
        processingMedia = selectedMedia

        cacheTasker.launch {
            try {
                onComplete(
                    CompletedEpisodeCacheRequest(
                        selectedMedia,
                        storage,
                        MediaCacheMetadata(fetchSession.request)
                    )
                )
                cancelRequest()
            } finally {
                processingMedia = null
            }
        }
    }
}

class CompletedEpisodeCacheRequest(
    val media: Media,
    val storage: MediaCacheStorage,
    val mediaCacheMetadata: MediaCacheMetadata,
)

@Composable
fun EpisodeCacheRequesterView(
    state: EpisodeCacheRequesterPresentation,
    episodeCacheState: EpisodeCacheState
) {
    if (state.showStorageDialog) {
        SelectMediaStorageDialog(
            options = state.cacheStorages,
            onSelect = { state.selectStorage(it) },
            onDismissRequest = { state.cancelRequest() }
        )
    }

    state.ongoingSelection?.let { mediaSelectorPresentation ->
        ModalBottomSheet(
            { state.cancelRequest() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            EpisodeCacheMediaSelector(
                mediaSelectorPresentation,
                onSelect = { media ->
                    state.selectMedia(media)
                },
                onCancel = { state.cancelRequest() },
                sourceResults = state.mediaSourceResults,
                Modifier.fillMaxHeight().navigationBarsPadding() // 防止添加筛选后数量变少导致 bottom sheet 高度变化
            )
        }
    }
}
