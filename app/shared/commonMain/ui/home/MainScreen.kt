package me.him188.ani.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import me.him188.ani.app.platform.isInLandscapeMode
import me.him188.ani.app.ui.collection.CollectionPage
import me.him188.ani.app.ui.collection.MyCollectionsViewModel
import me.him188.ani.app.ui.foundation.TabNavigationItem
import me.him188.ani.app.ui.profile.ProfilePage
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator

/**
 * 由 bottom bar 等导致的 paddings
 */
val LocalContentPaddings: ProvidableCompositionLocal<PaddingValues> = androidx.compose.runtime.compositionLocalOf {
    PaddingValues(0.dp)
}

@Composable
fun MainScreen() {
    val myCollectionsViewModel = remember { MyCollectionsViewModel() }
    val searchViewModel = remember { SearchViewModel() }

    if (isInLandscapeMode()) {
        MainScreenLandscape()
    } else {
        MainScreenPortrait(myCollectionsViewModel, searchViewModel)
    }
}

@Composable
fun MainScreenLandscape() {

}

@Composable
fun MainScreenPortrait(
    myCollectionsViewModel: MyCollectionsViewModel,
    searchViewModel: SearchViewModel
) {
    var selectedTab by remember { mutableStateOf("collection") }

    val navigator = rememberNavigator()
    Scaffold(
        Modifier,
        bottomBar = {
            fun closeSearch() {
                searchViewModel.searchActive.value = false
            }

            Column(Modifier.alpha(0.97f)) {
                Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                    Divider(thickness = 1.dp)

                    BottomAppBar(
                        Modifier
                            .navigationBarsPadding()
                            .height(48.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                    ) {
                        TabNavigationItem(
                            selectedTab == "home",
                            {
                                selectedTab = "home"
                                closeSearch()
                            },
                            icon = { Icon(Icons.Outlined.Home, null) },
                            title = { Text(text = "首页") },
                        )
                        TabNavigationItem(
                            selectedTab == "collection",
                            {
                                selectedTab = "collection"
                                closeSearch()
                            },
                            icon = { Icon(Icons.Outlined.StarOutline, null) },
                            title = { Text(text = "追番") },
                        )
                        TabNavigationItem(
                            selectedTab == "profile",
                            {
                                selectedTab = "profile"
                                closeSearch()
                            },
                            icon = { Icon(Icons.Outlined.Person, null) },
                            title = { Text(text = "我的") },
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { contentPadding ->
        CompositionLocalProvider(LocalContentPaddings provides contentPadding) {
            NavHost(navigator, initialRoute = "home") {
                scene("home") {
                    when (selectedTab) {
                        "home" -> HomePage(searchViewModel, contentPadding)
                        "collection" -> CollectionPage(contentPadding, myCollectionsViewModel)
                        "profile" -> ProfilePage()
                    }
                }
            }
        }
    }
}