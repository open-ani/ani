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
import  androidx.compose.ui.res.painterResource

@Stable
actual object Resources {
    @Stable
    actual val painter: PainterResources = PainterResources
}

@Stable
actual object PainterResources {
    @Stable
    actual val check: Painter @Composable get() = painterResource("drawable/check.svg")

    @Stable
    actual val cog: Painter @Composable get() = painterResource("drawable/cog.svg")

    @Stable
    actual val magnify: Painter @Composable get() = painterResource("drawable/magnify.svg")

    @Stable
    actual val star: Painter @Composable get() = painterResource("drawable/star.svg")

    @Stable
    actual val star_outline: Painter @Composable get() = painterResource("drawable/star-outline.svg")

    @Stable
    actual val star_plus_outline: Painter @Composable get() = painterResource("drawable/star-plus-outline.svg")

    @Stable
    actual val star_remove: Painter @Composable get() = painterResource("drawable/star-remove.svg")
}