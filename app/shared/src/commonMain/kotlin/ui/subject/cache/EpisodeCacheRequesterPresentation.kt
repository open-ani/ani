package me.him188.ani.app.ui.subject.cache

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.media.cache.MediaCacheStorage
import me.him188.ani.app.data.media.cache.requester.CacheRequestStage
import me.him188.ani.app.data.media.cache.requester.CacheRequestStage.Working
import me.him188.ani.app.data.media.cache.requester.EpisodeCacheRequester
import me.him188.ani.app.data.media.cache.requester.mediaSourceResults
import me.him188.ani.app.data.media.cache.requester.request
import me.him188.ani.app.data.media.cache.requester.trySelectSingle
import me.him188.ani.app.data.media.fetch.FilteredMediaSourceResults
import me.him188.ani.app.data.media.selector.MediaSelector
import me.him188.ani.app.data.models.MediaSelectorSettings
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSourceResultsPresentation
import me.him188.ani.datasources.api.Media
import kotlin.coroutines.CoroutineContext

/**
 * [EpisodeCacheRequesterHost] 的 UI 状态.
 */
@Stable
class EpisodeCacheRequesterPresentation(
    private val requester: EpisodeCacheRequester,
    settingsNotCached: Flow<MediaSelectorSettings>,
    private val onSelect: suspend (CacheRequestStage.Done) -> Unit,
    parentCoroutineContext: CoroutineContext,
) : HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    private val stage by requester.stage.produceState()
    val workingStage by derivedStateOf { stage as? Working }
    val selectStorageStage by derivedStateOf { stage as? CacheRequestStage.SelectStorage }

    val mediaSourceResults = MediaSourceResultsPresentation(
        FilteredMediaSourceResults(
            requester.mediaSourceResults,
            settingsNotCached,
        ),
        backgroundScope.coroutineContext
    )

    // 每次只执行一个缓存
    private val tasker = MonoTasker(backgroundScope)
    var processingMedia by mutableStateOf<Media?>(null)

    /**
     * 开始请求缓存一个剧集. 将会展示 [MediaSelector].
     */
    fun request(subjectId: Int, episodeId: Int, subjectManager: SubjectManager) {
        tasker.launch { requester.request(subjectId, episodeId, subjectManager) }
    }

    fun cancelRequest() {
        tasker.launch { requester.cancelRequest() }
    }

    /**
     * 当用户从 [MediaSelector] 选择了一个 [Media] 时调用. 将会显示选择存储位置 ([SelectMediaStorageDialog])
     */
    fun selectMedia(media: Media) {
        val stage = workingStage as? CacheRequestStage.SelectMedia ?: return
        tasker.launch {
            stage.select(media).apply {
                trySelectSingle()
            }
        }
    }

    /**
     * 用户从 [SelectMediaStorageDialog] 选择了一个存储位置时调用.
     */
    fun selectStorage(storage: MediaCacheStorage) {
        val stage = selectStorageStage ?: return
        tasker.launch {
            val done = stage.select(storage)
            processingMedia = done.media
            try {
                onSelect(done)
                cancelRequest()
            } finally {
                processingMedia = null
            }
        }
    }
}

@Composable
fun EpisodeCacheRequesterHost(
    state: EpisodeCacheRequesterPresentation,
) {
    SelectMediaStorageDialogHost(state)
    EpisodeCacheMediaSelectorSheetHost(state)
}
