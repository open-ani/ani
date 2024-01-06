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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import me.him188.animationgarden.app.platform.isInLandscapeMode
import me.him188.animationgarden.app.ui.home.LocalContentPaddings
import me.him188.animationgarden.app.ui.subject.SubjectPreviewColumn

/**
 * 搜索页面
 */
@Composable
fun SearchPage(viewModel: SearchViewModel) {
    if (isInLandscapeMode()) {
        SearchPageLandscape(viewModel)
    } else {
        SearchPagePortrait(viewModel)
    }
}

@Composable
private fun SearchPageLandscape(viewModel: SearchViewModel) {
    SearchPagePortrait(viewModel)
}

@Composable
private fun SearchPagePortrait(viewModel: SearchViewModel) {
    Column(
        Modifier.padding(
            top = LocalContentPaddings.current.calculateTopPadding(),
            start = LocalContentPaddings.current.calculateStartPadding(LocalLayoutDirection.current),
            end = LocalContentPaddings.current.calculateEndPadding(LocalLayoutDirection.current),
        ).fillMaxSize()
    ) {

        val query by viewModel.editingQuery.collectAsState()
        val searchActive by viewModel.searchActive.collectAsState()
        SearchBar(
            query,
            onQueryChange = { viewModel.editingQuery.value = it },
            onSearch = {
                viewModel.search(it)
                viewModel.searchActive.value = false
            },
            searchActive,
            onActiveChange = {
                viewModel.searchActive.value = it
            },
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp),
            placeholder = { Text("搜索") },
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            trailingIcon = {
                if (searchActive) {
                    IconButton({
                        viewModel.editingQuery.value = ""
                        viewModel.searchActive.value = false
                    }) {
                        Icon(Icons.Outlined.Close, "Cancel")
                    }
                }
            }
        ) {

        }

        val viewModel by viewModel.result.collectAsState()
        viewModel?.let {
            SubjectPreviewColumn(it)
        }
    }
}

//@Composable
//internal expect fun PreviewHomePage()