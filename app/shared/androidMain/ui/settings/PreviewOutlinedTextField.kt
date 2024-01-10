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

package me.him188.ani.app.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview
private fun PreviewSettingsOutlinedTextField() {
    Box(
        Modifier
            .padding(16.dp)
            .size(300.dp, 50.dp)) {
        SettingsOutlinedTextField("Test", {}, 200.dp, { true })
    }
}

@Composable
@Preview
private fun PreviewOutlinedTextFieldWithSaveButton() {
    Box(
        Modifier
            .padding(16.dp)
            .size(300.dp, 50.dp)) {
        SettingsOutlinedTextField("Test", {}, 200.dp, { true })
    }
}