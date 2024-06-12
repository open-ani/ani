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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.subject.SubjectPreviewColumn
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun HomePage(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
) {
    val searchViewModel = rememberViewModel { SearchViewModel() }
    val snackBarHostState = remember { SnackbarHostState() }

    val searchResult by searchViewModel.result.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            SubjectSearchBar(
                initialActive = searchViewModel.searchActive,
                initialSearchText = searchViewModel.editingQuery,
                modifier = Modifier.fillMaxWidth(),
                onActiveChange = { active ->
                    searchViewModel.searchActive = active
                },
                onSearch = { query ->
                    searchViewModel.editingQuery = query
                    searchViewModel.search(query)
                },
            )
        },
        contentWindowInsets = WindowInsets(0.dp)

    ) { topBarPadding ->
        Column(Modifier.fillMaxSize()) {
            searchResult?.let { SubjectPreviewColumn(it) }
        }
    }
}


//@Composable
//internal expect fun PreviewHomePage()