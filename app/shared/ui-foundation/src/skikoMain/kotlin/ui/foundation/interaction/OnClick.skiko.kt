package me.him188.ani.app.ui.foundation.interaction

import androidx.compose.foundation.Indication
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.onClick
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerType

actual fun Modifier.onClickEx(
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    enabled: Boolean,
    onDoubleClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    onClick: () -> Unit
): Modifier {
    return onClick(
        enabled = enabled,
        interactionSource = interactionSource,
        onDoubleClick = onDoubleClick,
        onLongClick = onLongClick,
        onClick = onClick,
    ).indication(interactionSource, indication)
}

/**
 * 仅在 PC 有效. 鼠标右键单击.
 */
actual fun Modifier.onRightClickIfSupported(
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    onClick: () -> Unit
): Modifier = onClick(
    enabled = enabled,
    interactionSource = interactionSource,
    matcher = PointerMatcher.pointer(PointerType.Mouse, button = PointerButton.Secondary),
    onClick = onClick,
)
