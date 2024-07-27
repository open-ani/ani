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

@file:Suppress("NOTHING_TO_INLINE")

package me.him188.ani.app.platform

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.dp
import me.him188.ani.app.platform.window.PlatformWindowMP
import java.io.File

expect val LocalContext: ProvidableCompositionLocal<Context>

expect abstract class Context

typealias ContextMP = Context // compose bug, use this in common

val Context.files: ContextFiles get() = filesImpl
internal expect val Context.filesImpl: ContextFiles

interface ContextFiles {
    val cacheDir: File

    /**
     * filesDir on Android.
     */
    val dataDir: File
}

/**
 * 横屏模式. 横屏模式不一定是全屏.
 *
 * PC 一定处于横屏模式.
 *
 * @see isSystemInFullscreenImpl
 */
@Composable
expect fun isInLandscapeMode(): Boolean

@Stable
fun BoxWithConstraintsScope.showTabletUI(): Boolean {
    // https://android-developers.googleblog.com/2023/06/detecting-if-device-is-foldable-tablet.html
    // 99.96% of phones have a built-in screen with a width smaller than 600dp when in portrait, 
    // but that same screen size could be the result of a freeform/split-screen window on a tablet or desktop device.

    return maxWidth >= 600.dp && maxHeight >= 600.dp
}

/**
 * Request to set the fullscreen, landscape mode, hiding status bars and navigation bars.
 *
 * If [fullscreen] is `true`, the app will enter fullscreen mode.
 * Otherwise, the app go back to the users' preferred mode.
 *
 * Note that when [fullscreen] is `false`, the system bars will be visible,
 * but the app may be still in landscape mode if the user's system is in landscape mode.
 */
expect fun Context.setRequestFullScreen(window: PlatformWindowMP, fullscreen: Boolean)

@Composable
inline fun isSystemInFullscreen(): Boolean = isSystemInFullscreenImpl()

/**
 * @see isInLandscapeMode
 */
@Composable
expect fun isSystemInFullscreenImpl(): Boolean
