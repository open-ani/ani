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

package me.him188.animationgarden.app.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.him188.animationgarden.app.ProvideCompositionLocalsForPreview
import me.him188.animationgarden.app.platform.LocalContext
import me.him188.animationgarden.app.platform.isInLandscapeMode
import me.him188.animationgarden.app.ui.search.SearchViewModel
import me.him188.animationgarden.app.ui.subject.SubjectPreviewColumn

/**
 * 应用
 */
@Composable
fun HomePage() {
    if (isInLandscapeMode()) {
        HomePageLandscape()
    } else {
        HomePagePortrait()
    }
}

@Composable
fun HomePagePortrait() {
    val search = remember { SearchViewModel() }
    LaunchedEffect(true) {
        search.search("葬送的芙莉莲")
    }

    Column(Modifier.fillMaxSize()) {
        val selectedTab = remember { mutableStateOf(PageTab.HOME) }

        val viewModel by search.result.collectAsState()
        viewModel?.let {
            SubjectPreviewColumn(it, Modifier)
        }

        return
        val context by rememberUpdatedState(newValue = LocalContext.current)
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "") {
            composable("home") {
                Column(Modifier.fillMaxSize()) {
                    Text("Home")
                }
            }
            composable("search") {
                Column(Modifier.fillMaxSize()) {
                    Text("Search")
                }
            }
            composable("my") {
                Column(Modifier.fillMaxSize()) {
                    Text("My")
                }
            }
        }

        navController.navigate("home") {
            popUpTo("home") {
                inclusive = true
            }
        }

        TabBar(
            selectedTab = selectedTab,
            Modifier
                .fillMaxWidth()
                .requiredHeight(80.dp)
                .padding(horizontal = 12.dp)
        ) {
            tab(
                PageTab.HOME,
                icon = { Icon(Icons.Default.Home, "Home") },
                text = { Text("首页") },
            )

            tab(
                PageTab.SEARCH,
                icon = { Icon(Icons.Default.Search, "Search") },
                text = { Text("找番") },
            )

            tab(
                PageTab.MY,
                icon = { Icon(Icons.Default.Person, "My") },
                text = { Text("我的") },
            )
        }
    }
}


@Composable
@Preview(backgroundColor = 0xFF000000, showBackground = true)
internal fun PreviewHomePagePortrait() {
    ProvideCompositionLocalsForPreview {
        HomePage()
    }
}
