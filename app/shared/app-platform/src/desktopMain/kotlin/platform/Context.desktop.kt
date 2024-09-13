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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.WindowState
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.io.inSystem
import me.him188.ani.utils.io.toKtPath
import java.io.File
import kotlin.contracts.contract

actual abstract class Context

@Stable
class DesktopContext(
    val windowState: WindowState,
    val dataDir: File,
    val cacheDir: File,
    val logsDir: File,
    val extraWindowProperties: ExtraWindowProperties,
) : Context() {
    val dataStoreDir = dataDir.resolve("datastore")
}

@Stable
class ExtraWindowProperties(
    initialUndecorated: Boolean = false,
) {
    var undecorated by mutableStateOf(initialUndecorated)
}

actual val LocalContext: ProvidableCompositionLocal<Context> = compositionLocalOf {
    error("No Context provided")
}

object LocalDesktopContext {
    val current: DesktopContext
        @Composable
        inline get() {
            val context = LocalContext.current
            check(context is DesktopContext)
            return context
        }
}

@Stable
inline fun Context.checkIsDesktop(): DesktopContext {
    contract { returns() implies (this@checkIsDesktop is DesktopContext) }
    check(this is DesktopContext) { "Context must be DesktopContext, but had: $this" }
    return this
}

internal actual val Context.filesImpl: ContextFiles
    get() = object : ContextFiles {
        override val cacheDir: SystemPath = (this@filesImpl as DesktopContext).cacheDir.toKtPath().inSystem
        override val dataDir: SystemPath = (this@filesImpl as DesktopContext).dataDir.toKtPath().inSystem
    }
