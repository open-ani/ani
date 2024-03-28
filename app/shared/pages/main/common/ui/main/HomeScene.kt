package me.him188.ani.app.ui.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.collection.CollectionPage
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.home.HomePage
import me.him188.ani.app.ui.home.SearchViewModel
import me.him188.ani.app.ui.profile.ProfilePage


@Preview
@Composable
fun HomeScene() {
    var selectedTab by remember { mutableStateOf("collection") }
    Scaffold(
        Modifier,
        bottomBar = {
            val searchViewModel = rememberViewModel { SearchViewModel() }
            fun closeSearch() {
                searchViewModel.searchActive.value = false
            }

            Column(Modifier.alpha(0.97f)) {
                Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                    HorizontalDivider(thickness = 1.dp)

                    NavigationBar {
                        NavigationBarItem(
                            selectedTab == "home",
                            {
                                selectedTab = "home"
                                closeSearch()
                            },
                            icon = { Icon(Icons.Outlined.Home, null) },
                            label = { Text(text = "首页") }
                        )
                        NavigationBarItem(
                            selectedTab == "collection",
                            {
                                selectedTab = "collection"
                                closeSearch()
                            },
                            icon = { Icon(Icons.Outlined.StarOutline, null) },
                            label = { Text(text = "追番") }
                        )
                        NavigationBarItem(
                            selectedTab == "profile",
                            {
                                selectedTab = "profile"
                                closeSearch()
                            },
                            icon = { Icon(Icons.Outlined.Person, null) },
                            label = { Text(text = "我的") }
                        )
                    }
//                    BottomAppBar(
//                        Modifier
//                            .navigationBarsPadding(),
//                        containerColor = MaterialTheme.colorScheme.surface,
//                        tonalElevation = 0.dp,
//                    ) {
//                        TabNavigationItem(
//                            selectedTab == "home",
//                            {
//                                selectedTab = "home"
//                                closeSearch()
//                            },
//                            icon = { Icon(Icons.Outlined.Home, null) },
//                            title = { Text(text = "首页") },
//                        )
//                        TabNavigationItem(
//                            selectedTab == "collection",
//                            {
//                                selectedTab = "collection"
//                                closeSearch()
//                            },
//                            icon = { Icon(Icons.Outlined.StarOutline, null) },
//                            title = { Text(text = "追番") },
//                        )
//                        TabNavigationItem(
//                            selectedTab == "profile",
//                            {
//                                selectedTab = "profile"
//                                closeSearch()
//                            },
//                            icon = { Icon(Icons.Outlined.Person, null) },
//                            title = { Text(text = "我的") },
//                        )
//                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { contentPadding ->
        CompositionLocalProvider(LocalContentPaddings provides contentPadding) {
            when (selectedTab) {
                "home" -> HomePage(contentPadding)
                "collection" -> CollectionPage(contentPadding)
                "profile" -> ProfilePage()
            }
        }
    }
}
