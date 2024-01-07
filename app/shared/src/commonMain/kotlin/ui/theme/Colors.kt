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

package me.him188.ani.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver


@Composable
fun Color.weaken(): Color {
    return if (isSystemInDarkTheme()) {
        Color.Gray.compositeOver(this)
    } else {
        copy(alpha = 0.5f)
    }
}

@Composable
fun Color.darken(): Color {
    return if (isSystemInDarkTheme()) {
        Color.DarkGray.compositeOver(this)
    } else {
        copy(alpha = 0.38f)
    }
}

fun aniDarkColorTheme(): ColorScheme = darkColorScheme()

fun aniLightColorTheme(): ColorScheme = lightColorScheme(
    background = Color(0xfff1f2f4),
    surface = Color.White,
    surfaceVariant = Color.White,
)