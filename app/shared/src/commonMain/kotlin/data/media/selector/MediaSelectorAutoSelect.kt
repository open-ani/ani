package me.him188.ani.app.data.media.selector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.takeWhile
import me.him188.ani.app.data.media.fetch.MediaFetchSession
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.source.MediaSourceKind

/**
 * 访问 [MediaSelector] 的自动选择功能
 */
inline val MediaSelector.autoSelect get() = MediaSelectorAutoSelect(this)

/**
 * [MediaSelector] 自动选择功能
 */
// TODO: add test for MediaSelectorAutoSelect
@JvmInline
value class MediaSelectorAutoSelect(
    private val mediaSelector: MediaSelector,
) {
    /**
     * 等待所有数据源查询完成, 然后根据用户的偏好设置自动选择.
     *
     * 返回成功选择的 [Media] 对象. 当用户已经手动选择过一个别的 [Media], 或者没有可选的 [Media] 时返回 `null`.
     */
    suspend fun awaitCompletedAndSelectDefault(mediaFetchSession: Flow<MediaFetchSession>): Media? {
        // 等全部加载完成
        mediaFetchSession.flatMapLatest { it.hasCompleted }.filter { it }.first()
        if (mediaSelector.selected.value == null) {
            val selected = mediaSelector.trySelectDefault()
            return selected
        }
        return null
    }

    suspend inline fun awaitCompletedAndSelectDefault(mediaFetchSession: MediaFetchSession): Media? =
        awaitCompletedAndSelectDefault(flowOf(mediaFetchSession))

    /**
     * 自动选择第一个 [MediaSourceKind.LocalCache] [Media]
     */
    suspend fun selectCached(mediaFetchSession: Flow<MediaFetchSession>): Boolean {
        val isSuccess = object {
            @Volatile
            var value = false
        }
        val stop = true
        combine(
            mediaFetchSession.flatMapLatest { it.cumulativeResults },
        ) { _ ->
            if (mediaSelector.selected.value != null) {
                // 用户已经选择了
                isSuccess.value = false
                return@combine stop
            }

            if (mediaSelector.trySelectCached() != null) {
                isSuccess.value = true
                stop
            } else {
                !stop
            }
        }.takeWhile { !stop }.collect()
        return isSuccess.value
    }
}
