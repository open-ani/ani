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

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import me.him188.ani.app.platform.window.PlatformWindowMP
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.io.inSystem
import me.him188.ani.utils.io.toKtPath
import java.io.File


actual typealias Context = android.content.Context

actual val LocalContext: ProvidableCompositionLocal<Context>
    get() = androidx.compose.ui.platform.LocalContext



internal actual val Context.filesImpl: ContextFiles
    get() = object : ContextFiles {
        override val cacheDir: SystemPath
            get() = (this@filesImpl.cacheDir ?: File("")).toKtPath().inSystem // can be null when previewing
        override val dataDir: SystemPath
            get() = (this@filesImpl.filesDir ?: File("")).toKtPath().inSystem // can be null when previewing
    }

