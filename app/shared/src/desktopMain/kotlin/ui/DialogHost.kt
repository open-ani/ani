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

package me.him188.ani.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class DialogInfo<T>(
    open val title: @Composable () -> String,
    open val content: @Composable () -> Unit,
    val onCloseWindow: () -> Unit,
    val result: CompletableDeferred<T>,
)

class DialogHost internal constructor(
    private val dialogInfoFlow: MutableSharedFlow<DialogInfo<*>?>
) {
    private val lock = Mutex()

    suspend fun <T> showDialog(info: DialogInfo<T>): T {
        lock.withLock {
            dialogInfoFlow.emit(info) // notify ui to display it
            return info.result.await().also {
                dialogInfoFlow.emit(null)
            }
        }
    }
}

@Composable
fun rememberDialogHost(): DialogHost {
    val dialogInfoFlow = remember { MutableSharedFlow<DialogInfo<*>?>() }
    val currentDialogInfo by dialogInfoFlow.collectAsState(null)
    Dialog(
        {
            currentDialogInfo?.let { info ->
                info.onCloseWindow.invoke()
                info.result.completeExceptionally(CancellationException())
            }
            dialogInfoFlow.tryEmit(null)
        },
        rememberDialogState(),
        visible = currentDialogInfo != null,
        title = currentDialogInfo?.title?.invoke() ?: ""
    ) {
        currentDialogInfo?.run {
            content()
        }
    }
    return remember {
        DialogHost(
            dialogInfoFlow
        )
    }
}

enum class DialogResult {
    CONFIRMED,
    CANCELED,
    DISMISSED,
}

suspend fun DialogHost.showConfirmationDialog(
    title: @Composable () -> String,
    confirmButtonText: @Composable () -> Unit,
    cancelButtonText: @Composable () -> Unit,
    content: @Composable () -> Unit,
): DialogResult {
    val result = CompletableDeferred<DialogResult>()
    showDialog(
        DialogInfo(
            title = title,
            content = {
                Column(Modifier.fillMaxSize().padding(all = 16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Row {
                        content()
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(onClick = { result.complete(DialogResult.CONFIRMED) }) {
                                confirmButtonText()
                            }
                            Button(onClick = { result.complete(DialogResult.CANCELED) }) {
                                cancelButtonText()
                            }
                        }
                    }
                }
            },
            onCloseWindow = {
                result.complete(DialogResult.DISMISSED)
            },
            result = result
        )
    )
    result.invokeOnCompletion {

    }
    return result.await()
}

suspend fun DialogHost.showInformationDialog(
    title: @Composable () -> String,
    buttonText: @Composable () -> Unit,
    content: @Composable () -> Unit,
): DialogResult {
    val result = CompletableDeferred<DialogResult>()
    showDialog(
        DialogInfo(
            title = title,
            content = {
                Column(Modifier.fillMaxSize().padding(all = 16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Row {
                        content()
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = { result.complete(DialogResult.CONFIRMED) }) {
                            buttonText()
                        }
                    }
                }
            },
            onCloseWindow = {
                result.complete(DialogResult.DISMISSED)
            },
            result
        )
    )
    return result.await()
}