package me.him188.ani.app.data.media.fetch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.models.MediaSelectorSettings
import me.him188.ani.utils.coroutines.flows.combinedAny
import kotlin.coroutines.CoroutineContext

/**
 * 正在进行中的数据源查询的结果
 */
class MediaSourceResults(
    results: Flow<List<MediaSourceFetchResult>>,
    settings: Flow<MediaSelectorSettings>, // 不可以为 empty flow
    flowDispatcher: CoroutineContext = Dispatchers.Default,
) {
    /**
     * 有至少一个数据源仍然处于 [MediaSourceFetchState.Working] 状态.
     */
    val anyLoading = results.flatMapLatest { results ->
        results
            .map { it.state }
            .combinedAny { it.isWorking }
    }.flowOn(flowDispatcher)

    /**
     * 根据设置, 过滤掉禁用的数据源, 并按照查询到的数量降序排序.
     */
    // Not shared
    val sourceResults: Flow<List<MediaSourceFetchResult>> = results.flatMapLatest { results ->
        settings.flatMapLatest { settings ->
            // 过滤掉禁用的
            val candidates = results.filterTo(mutableListOf()) {
                if (!settings.showDisabled && it.state.value.isDisabled) return@filterTo false
                true
            }

            // 收集它们的 size
            combine(candidates.map { sizes -> sizes.resultsIfEnabled.map { it.size } }) { sizes ->
                // 按照结果数量从大到小, 把禁用的放在最后.
                candidates.sortedWith(
                    compareByDescending<MediaSourceFetchResult> { result ->
                        if (result.state.value.isDisabled) {
                            Int.MIN_VALUE
                        } else {
                            sizes[candidates.indexOf(result)]
                        }
                    }.thenComparing<String> { it.mediaSourceId } // 大小相同的按 ID 排序, 保证稳定
                )
            }
        }
    }.flowOn(flowDispatcher)
}

private val EmptyMediaSourceResults by lazy(LazyThreadSafetyMode.NONE) {
    MediaSourceResults(flowOf(emptyList()), flowOf(MediaSelectorSettings.Default))
}

fun emptyMediaSourceResults() = EmptyMediaSourceResults