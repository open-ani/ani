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

package me.him188.ani.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import java.io.File

expect val LocalContext: ProvidableCompositionLocal<Context>

expect abstract class Context

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
 * Returns `true` if the app is in landscape mode.
 */
@Composable
expect fun isInLandscapeMode(): Boolean

/**
 * Request to set the fullscreen, landscape mode, hiding status bars and navigation bars.
 *
 * If [fullscreen] is `true`, the app will enter fullscreen mode.
 * Otherwise, the app go back to the users' preferred mode.
 *
 * Note that when [fullscreen] is `false`, the system bars will be visible,
 * but the app may be still in landscape mode if the user's system is in landscape mode.
 */
expect fun Context.setRequestFullScreen(fullscreen: Boolean)