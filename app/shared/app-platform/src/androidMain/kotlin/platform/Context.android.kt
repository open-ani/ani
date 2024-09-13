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

import androidx.compose.runtime.ProvidableCompositionLocal
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

