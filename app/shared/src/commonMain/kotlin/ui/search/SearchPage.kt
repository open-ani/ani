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

package me.him188.animationgarden.app.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import cafe.adriel.voyager.navigator.Navigator
import me.him188.animationgarden.app.platform.isInLandscapeMode
import me.him188.animationgarden.app.ui.home.LocalContentPaddings
import me.him188.animationgarden.app.ui.subject.SubjectPreviewColumn

/**
 * 搜索页面
 */
@Composable
fun SearchPage(
    subjectDetailsNavigator: Navigator,
) {
    if (isInLandscapeMode()) {
        SearchPageLandscape()
    } else {
        SearchPagePortrait(subjectDetailsNavigator)
    }
}

@Composable
private fun SearchPageLandscape() {
}

@Composable
private fun SearchPagePortrait(
    subjectDetailsNavigator: Navigator,
) {
    val search = remember { SearchViewModel() }
    LaunchedEffect(true) {
        search.search("葬送的芙莉莲")
    }

    Column(
        Modifier.padding(
            top = LocalContentPaddings.current.calculateTopPadding(),
            start = LocalContentPaddings.current.calculateStartPadding(LocalLayoutDirection.current),
            end = LocalContentPaddings.current.calculateEndPadding(LocalLayoutDirection.current),
        ).fillMaxSize()
    ) {
        val viewModel by search.result.collectAsState()
        viewModel?.let {
            SubjectPreviewColumn(it, subjectDetailsNavigator, Modifier)
        }
    }
}

//@Composable
//internal expect fun PreviewHomePage()