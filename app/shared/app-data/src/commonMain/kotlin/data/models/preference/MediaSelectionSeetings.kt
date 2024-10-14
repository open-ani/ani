/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.models.preference

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.datasources.api.source.MediaSourceKind

/**
 * 数据源选择器 (播放页面点击 "数据源" 按钮弹出的) 的设置
 * @see MediaPreference
 */
@Serializable
@Immutable
data class MediaSelectorSettings
@Deprecated("Use Default instead", level = DeprecationLevel.ERROR)
constructor(
    /**
     * 即使数据源禁用, 也在选择器中以灰色显示, 方便临时启用
     */
    val showDisabled: Boolean = true,
    /**
     * 完结后隐藏单集资源
     */
    val hideSingleEpisodeForCompleted: Boolean = true,
    /**
     * 优先选择季度全集资源
     */
    val preferSeasons: Boolean = true,
    /**
     * 优先选择季度全集资源
     * @since 3.2.0-beta04
     */
    val autoEnableLastSelected: Boolean = true,
    /**
     * 优先选择在线数据源
     * @see MediaSourceKind.WEB
     * @since 3.5
     */
    val preferKind: MediaSourceKind? = null, // 旧用户不 prefer
    @Suppress("PropertyName") @Transient val _placeholder: Int = 0,
) {
    companion object {

        // 新用户会使用的默认设置
        @Stable
        @Suppress("DEPRECATION_ERROR")
        val Default = MediaSelectorSettings(
            preferKind = MediaSourceKind.WEB, // 新用户
        )

        /**
         * 用于测试, 显示所有 media
         */
        @Stable
        @Suppress("DEPRECATION_ERROR")
        val AllVisible = Default.copy(
            hideSingleEpisodeForCompleted = false,
        )
    }
}