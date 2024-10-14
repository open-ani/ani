/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.media.selector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.takeWhile
import me.him188.ani.app.domain.media.fetch.awaitCompletion
import me.him188.ani.app.domain.media.fetch.MediaFetchSession
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.source.MediaSourceKind
import kotlin.concurrent.Volatile
import kotlin.jvm.JvmInline

/**
 * 访问 [MediaSelector] 的自动选择功能
 */
inline val MediaSelector.autoSelect get() = MediaSelectorAutoSelect(this)

/**
 * [MediaSelector] 自动选择功能
 */
@JvmInline
value class MediaSelectorAutoSelect(
    private val mediaSelector: MediaSelector,
) {
    /**
     * 等待所有数据源查询完成, 然后根据用户的偏好设置自动选择.
     *
     * 返回成功选择的 [Media] 对象. 当用户已经手动选择过一个别的 [Media], 或者没有可选的 [Media] 时返回 `null`.
     */
    suspend fun awaitCompletedAndSelectDefault(
        mediaFetchSession: MediaFetchSession,
        preferKind: Flow<MediaSourceKind?> = flowOf(null)
    ): Media? {
        // 等全部加载完成
        mediaFetchSession.awaitCompletion { completedConditions ->
            return@awaitCompletion preferKind.first()?.let {
                completedConditions[it]
            } ?: completedConditions.allCompleted()
        }
        if (mediaSelector.selected.value == null) {
            val selected = mediaSelector.trySelectDefault()
            return selected
        }
        return null
    }

    /**
     * 自动选择第一个 [MediaSourceKind.LocalCache] [Media].
     *
     * 当成功选择了一个 [Media] 时返回它. 若已经选择了一个别的, 或没有 [MediaSourceKind.LocalCache] 类型的 [Media] 供选择, 返回 `null`.
     */
    suspend fun selectCached(
        mediaFetchSession: MediaFetchSession,
        maxAttempts: Int = Int.MAX_VALUE,
    ): Media? {
        val isSuccess = object {
            @Volatile
            var value: Media? = null

            @Volatile
            var attempted = 0
        }
        combine(
            mediaFetchSession.cumulativeResults,
        ) { _ ->
            if (mediaSelector.selected.value != null) {
                // 用户已经选择了
                isSuccess.value = null
                return@combine STOP
            }

            val selected = mediaSelector.trySelectCached()
            if (selected != null) {
                isSuccess.value = selected
                STOP
            } else {
                if (++isSuccess.attempted >= maxAttempts) {
                    // 尝试次数过多
                    STOP
                } else {
                    // 继续等待
                    !STOP
                }
            }
        }.takeWhile { it == !STOP }.collect()
        return isSuccess.value
    }

    // #355 播放时自动启用上次临时启用选择的数据源
    suspend fun autoEnableLastSelected(mediaFetchSession: MediaFetchSession) {
        val lastSelectedId = mediaSelector.mediaSourceId.finalSelected.first()
        val lastSelected = mediaFetchSession.mediaSourceResults.firstOrNull {
            it.mediaSourceId == lastSelectedId
        } ?: return
        lastSelected.enable()
    }
}

private const val STOP = true
