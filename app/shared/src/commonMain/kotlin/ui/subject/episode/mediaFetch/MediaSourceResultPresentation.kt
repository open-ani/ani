package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.media.fetch.FilteredMediaSourceResults
import me.him188.ani.app.data.media.fetch.MediaSourceFetchResult
import me.him188.ani.app.data.media.fetch.MediaSourceFetchState
import me.him188.ani.app.data.media.fetch.emptyMediaSourceResults
import me.him188.ani.app.data.media.fetch.isDisabled
import me.him188.ani.app.data.media.fetch.isFailedOrAbandoned
import me.him188.ani.app.data.media.fetch.isWorking
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.rememberBackgroundScope
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.utils.coroutines.onReplacement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 单个数据源的搜索结果
 */
@Stable
class MediaSourceResultPresentation(
    private val delegate: MediaSourceFetchResult,
    parentCoroutineContext: CoroutineContext,
) : AutoCloseable, HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    val mediaSourceId get() = delegate.mediaSourceId
    val state: MediaSourceFetchState by delegate.state.produceState()
    val isWorking by derivedStateOf { state.isWorking }
    val isDisabled by derivedStateOf { state.isDisabled }
    val isFailedOrAbandoned by derivedStateOf { state.isFailedOrAbandoned }

    val kind = delegate.kind

    val totalCount: Int by delegate.resultsIfEnabled
        .map { it.size }
        .produceState(0)

    fun restart() = delegate.restart()
    override fun close() {
        backgroundScope.cancel()
    }
}

@Composable
fun rememberMediaSourceResultPresentation(
    delegate: () -> MediaSourceFetchResult, // will not update
): MediaSourceResultPresentation {
    val background = rememberBackgroundScope() // bind to current composition
    return remember {
        MediaSourceResultPresentation(delegate(), background.backgroundScope.coroutineContext)
    }
}


/**
 * 在 [MediaSelectorView] 使用, 管理多个 [MediaSourceResultPresentation] 的结果
 */
@Stable
class MediaSourceResultsPresentation(
    results: FilteredMediaSourceResults,
    parentCoroutineContext: CoroutineContext,
    flowDispatcher: CoroutineContext = Dispatchers.Default,
) : HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    val list: List<MediaSourceResultPresentation> by results.filteredSourceResults
        .map { list ->
            list.map {
                MediaSourceResultPresentation(it, backgroundScope.coroutineContext)
            }
        }
        .onReplacement { list ->
            list.forEach { it.close() }
        }
        .flowOn(flowDispatcher)
        .produceState(emptyList())

    val anyLoading by derivedStateOf { list.any { it.isWorking } }

    val webSources: List<MediaSourceResultPresentation> by derivedStateOf {
        list.filter { it.kind == MediaSourceKind.WEB }
    }
    val btSources: List<MediaSourceResultPresentation> by derivedStateOf {
        list.filter { it.kind == MediaSourceKind.BitTorrent }
    }

    val enabledSourceCount by derivedStateOf { list.count { !it.isDisabled && it.kind != MediaSourceKind.LocalCache } }
    val totalSourceCount by derivedStateOf { list.count { it.kind != MediaSourceKind.LocalCache } } // 缓存数据源属于内部的, 用户应当无感
}

private val EmptyMediaSourceResultsPresentation by lazy(LazyThreadSafetyMode.NONE) {
    MediaSourceResultsPresentation(emptyMediaSourceResults(), EmptyCoroutineContext)
}

@Stable
fun emptyMediaSourceResultsPresentation() = EmptyMediaSourceResultsPresentation
