package me.him188.ani.app.ui.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.ui.collection.CollectionPage
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.home.HomePage
import me.him188.ani.app.ui.home.SearchViewModel
import me.him188.ani.app.ui.profile.ProfilePage


@Preview
@Composable
fun HomeScene() {
    val pages = rememberPagerState(1) { 3 }
    val scope = rememberCoroutineScope()
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
                            pages.currentPage == 0,
                            onClick = {
                                scope.launch {
                                    pages.scrollToPage(0)
                                }
                                closeSearch()
                            },
                            icon = { Icon(Icons.Rounded.Home, null) },
                            label = { Text(text = "首页") }
                        )
                        NavigationBarItem(
                            pages.currentPage == 1,
                            onClick = {
                                scope.launch {
                                    pages.scrollToPage(1)
                                }
                                closeSearch()
                            },
                            icon = { Icon(Icons.Rounded.Star, null) },
                            label = { Text(text = "追番") }
                        )
                        NavigationBarItem(
                            pages.currentPage == 2,
                            onClick = {
                                scope.launch {
                                    pages.scrollToPage(2)
                                }
                                closeSearch()
                            },
                            icon = { Icon(Icons.Rounded.Person, null) },
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
        val navigator by rememberUpdatedState(LocalNavigator.current)

        CompositionLocalProvider(LocalContentPaddings provides contentPadding) {
            HorizontalPager(pages, userScrollEnabled = false) {
                when (it) {
                    0 -> HomePage(contentPadding)
                    1 -> CollectionPage(contentPadding)
                    2 -> {
                        ProfilePage(
                            onClickSettings = {
                                navigator.navigatePreferences()
                            }
                        )
                    }
                }
            }
        }
    }
}
