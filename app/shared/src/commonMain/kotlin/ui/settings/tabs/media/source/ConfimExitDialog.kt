package me.him188.ani.app.ui.settings.tabs.media.source

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun rememberConfirmDiscardChangeDialogState(
    onConfirm: () -> Unit,
): ConfirmDiscardChangeDialogState {
    val onConfirmState by rememberUpdatedState(onConfirm)
    return remember {
        ConfirmDiscardChangeDialogState(onConfirmState)
    }
}

@Stable
class ConfirmDiscardChangeDialogState(
    val onConfirm: () -> Unit,
) {
    internal var isVisible by mutableStateOf(false)
        private set

    fun show() {
        isVisible = true
    }

    fun dismissDialog() {
        isVisible = false
    }

    fun confirmDiscard() {
        onConfirm()
        dismissDialog()
    }
}

@Composable
fun ConfirmDiscardChangeDialog(
    state: ConfirmDiscardChangeDialogState,
    modifier: Modifier = Modifier,
) {
    if (state.isVisible) {
        AlertDialog(
            onDismissRequest = state::dismissDialog,
            title = { Text("舍弃更改?") },
            confirmButton = {
                TextButton(onClick = state::confirmDiscard) {
                    Text("舍弃", color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = modifier,
            dismissButton = {
                TextButton(onClick = state::dismissDialog) {
                    Text("继续编辑")
                }
            },
        )
    }
}
