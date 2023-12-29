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

package me.him188.animationgarden.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.painter.Painter

@Suppress("unused") // false positive
typealias Res = Resources

@Stable
expect object Resources {
    @Stable
    val painter: PainterResources
}

@Stable
expect object PainterResources {
    @Stable
    val check: Painter @Composable get

    @Stable
    val cog: Painter @Composable get

    @Stable
    val magnify: Painter @Composable get

    @Stable
    val star: Painter @Composable get

    @Stable
    val star_outline: Painter @Composable get

    @Stable
    val star_plus_outline: Painter @Composable get

    @Stable
    val star_remove: Painter @Composable get
}