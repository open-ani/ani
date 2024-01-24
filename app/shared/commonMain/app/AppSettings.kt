/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.app.app

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import me.him188.ani.app.app.settings.ProxySettings
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

@Immutable
@Serializable
data class AppSettings(
    /**
     * macOS 窗口沉浸, 把背景画入标题栏
     */
    @Stable
    val windowImmersed: Boolean = true,

    @Stable
    val proxy: ProxySettings = ProxySettings(),

    /**
     * 保存播放进度的时长, 超出时长的记录将被删除
     */
    val keepPlayPosition: Duration = 30.days
)
