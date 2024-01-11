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

package me.him188.ani.app.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import me.him188.ani.app.platform.isInLandscapeMode

@Composable
fun HomePage(
    searchViewModel: SearchViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    if (isInLandscapeMode()) {
        HomePageLandscape()
    } else {
        HomePagePortrait(contentPadding, searchViewModel)
    }
}

@Composable
private fun HomePageLandscape() {
}

@Composable
private fun HomePagePortrait(
    contentPadding: PaddingValues,
    searchViewModel: SearchViewModel
) {
    Column(
        Modifier.padding(
            top = contentPadding.calculateTopPadding(),
            start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
            end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
        ).fillMaxSize()
    ) {
    }
}


//@Composable
//internal expect fun PreviewHomePage()