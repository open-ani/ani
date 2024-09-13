package me.him188.ani.app.ui.foundation.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

/**
 * Popup specific for modal bottom ime aware sheet.
 */
@Composable
expect fun ModalBottomImeAwareSheetPopup(
    popupPositionProvider: PopupPositionProvider,
    onDismissRequest: () -> Unit,
    properties: PopupProperties,
    content: @Composable () -> Unit
)