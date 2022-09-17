/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
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

package me.him188.animationgarden.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun LabelledRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: RadioButtonColors = RadioButtonDefaults.colors(),
    label: @Composable () -> Unit,
) {
    Row(
        modifier.then(
            if (onClick != null)
                Modifier.clickable(
                    interactionSource,
                    null,
                    onClick = onClick
                )
            else Modifier
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected, onClick, enabled = enabled, interactionSource = interactionSource, colors = colors)
        label()
    }
}

@Stable
interface LabelColors {
    @Composable
    fun textColor(checked: Boolean, enabled: Boolean): Color
}

@Immutable
private class DefaultLabelColors(
    private val checkedTextColor: Color,
    private val uncheckedTextColor: Color,
    private val disabledCheckedTextColor: Color,
    private val disabledUncheckedTextColor: Color,
) : LabelColors {
    @Composable
    override fun textColor(checked: Boolean, enabled: Boolean): Color {
        return if (checked) {
            if (enabled) {
                checkedTextColor
            } else {
                disabledCheckedTextColor
            }
        } else {
            if (enabled) {
                uncheckedTextColor
            } else {
                disabledUncheckedTextColor
            }
        }
    }

}

@Immutable
object LabelDefaults {
    /**
     * @see TextFieldDefaults.textFieldColors
     */
    @Composable
    fun labelColors(
        checkedTextColor: Color = MaterialTheme.colorScheme.onSurface,
        uncheckedTextColor: Color = MaterialTheme.colorScheme.onSurface,
        disabledCheckedTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        disabledUncheckedTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    ): LabelColors {
        return DefaultLabelColors(
            checkedTextColor,
            uncheckedTextColor,
            disabledCheckedTextColor,
            disabledUncheckedTextColor
        )
    }
}

@Composable
fun LabelledCheckBox(
    selected: Boolean,
    onSelectedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: CheckboxColors = CheckboxDefaults.colors(),
    textColor: LabelColors = LabelDefaults.labelColors(),
    label: @Composable () -> Unit,
) {
    Row(
        modifier.then(
            if (onSelectedChange != null)
                Modifier.clickable(
                    interactionSource,
                    null,
                    enabled = enabled,
                    onClick = { onSelectedChange(!selected) }
                )
            else Modifier
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(selected, onSelectedChange, enabled = enabled, interactionSource = interactionSource, colors = colors)

        CompositionLocalProvider(LocalContentColor provides textColor.textColor(selected, enabled)) {
            label()
        }
    }
}
