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

package me.him188.ani.app.ux

import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import me.him188.ani.app.platform.Context
import kotlin.coroutines.Continuation

suspend inline fun <T> Context.showDialog(crossinline builderAction: AlertDialog.Builder.(cont: Continuation<T>) -> Unit): T {
    val context = this
    return withContext(Dispatchers.Main.immediate) {
        @Suppress("RemoveExplicitTypeArguments")
        suspendCancellableCoroutine<T> { cont ->
            val dialog = AlertDialog.Builder(context).apply { builderAction(cont) }.create()
            dialog.show()
            cont.invokeOnCancellation {
                dialog.hide()
            }
        }
    }
}