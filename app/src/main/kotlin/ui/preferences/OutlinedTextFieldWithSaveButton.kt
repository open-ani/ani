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

package me.him188.animationgarden.desktop.ui.preferences

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.him188.animationgarden.desktop.AppTheme
import me.him188.animationgarden.desktop.ui.interaction.onEnterKeyEvent
import me.him188.animationgarden.desktop.ui.widgets.OutlinedTextFieldEx

@Composable
fun OutlinedTextFieldWithSaveButton(
    value: String,
    onValueChange: (String) -> Unit,
    showButton: Boolean,
    onClickSave: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    placeholder: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    onPressEnter: (KeyEvent) -> Boolean = { onClickSave(); true },
) {
    val textFieldFocus = remember { FocusRequester() }
    val buttonFocus = remember { FocusRequester() }
    BoxWithSaveButton(
        showButton, onClickSave,
        {
            if (label != null) (-8).dp else 0.dp
        },
        modifier = Modifier.focusGroup(),
        buttonModifier = Modifier.focusRequester(buttonFocus).focusProperties {
            previous = textFieldFocus
            left = textFieldFocus
            start = textFieldFocus
            end = buttonFocus
        }
    ) { width ->
        SettingsOutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            width = width,
            onPressEnter = onPressEnter,
            modifier = modifier.focusRequester(textFieldFocus).focusProperties {
                next = buttonFocus
                right = buttonFocus
                start = textFieldFocus
                end = buttonFocus
            },
            enabled = enabled,
            label = label,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            placeholder = placeholder,
        )
    }
}

@Composable
fun SettingsOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    width: Dp,
    onPressEnter: (KeyEvent) -> Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    placeholder: @Composable (() -> Unit)? = null
) {
    OutlinedTextFieldEx(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxHeight()
            .width(width)
            .onEnterKeyEvent(onPressEnter),
        enabled = enabled,
        textStyle = AppTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
        singleLine = true,
        maxLines = 1,
        shape = AppTheme.shapes.small,
        label = label,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        placeholder = {
            ProvideTextStyle(
                AppTheme.typography.bodyMedium.copy(
                    color = AppTheme.typography.bodyMedium.color.copy(0.3f),
                    lineHeight = 20.sp
                )
            ) {
                placeholder?.invoke()
            }
        },
        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp)
    )
}
