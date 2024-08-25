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

package me.him188.ani.app.ui.foundation.interaction

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.debugInspectorInfo

expect fun Modifier.onClickEx(
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    enabled: Boolean = true,
    onDoubleClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier

/**
 * 仅在 PC 有效. 鼠标右键单击.
 */
expect fun Modifier.onRightClickIfSupported(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier

/**
 * 仅在 PC 有效. 鼠标右键单击.
 */
fun Modifier.onRightClickIfSupported(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "onRightClickIfSupported"
        properties["enabled"] = enabled
        properties["onClick"] = onClick
    },
) {
    this.onRightClickIfSupported(
        interactionSource = remember { MutableInteractionSource() },
        enabled = enabled,
        onClick = onClick,
    )
}

/**
 * [clickable] then [onRightClickIfSupported]
 */
fun Modifier.clickableAndMouseRightClick(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = clickable(enabled = enabled, onClick = onClick)
    .onRightClickIfSupported(enabled = enabled, onClick = onClick)
