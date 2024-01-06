package me.him188.animationgarden.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
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
import me.him188.animationgarden.app.platform.isInLandscapeMode
import me.him188.animationgarden.app.ui.auth.AccountViewModel
import me.him188.animationgarden.app.ui.auth.AuthPage
import me.him188.animationgarden.app.ui.foundation.TabNavigationItem
import me.him188.animationgarden.app.ui.search.SearchPage
import me.him188.animationgarden.app.ui.search.SearchViewModel

/**
 * 由 bottom bar 等导致的 paddings
 */
val LocalContentPaddings: ProvidableCompositionLocal<PaddingValues> = androidx.compose.runtime.compositionLocalOf {
    error("LocalContentPaddings is not provided")
}

@Composable
fun MainScreen() {
    val accountViewModel = remember { AccountViewModel(isRegister = false) }
    val searchViewModel = remember { SearchViewModel("") }

    if (isInLandscapeMode()) {
        MainScreenLandscape()
    } else {
        MainScreenPortrait(accountViewModel, searchViewModel)
    }
}

@Composable
fun MainScreenLandscape() {

}

@Composable
fun MainScreenPortrait(
    accountViewModel: AccountViewModel,
    searchViewModel: SearchViewModel
) {
    var selectedTab by remember { mutableStateOf("search") }

    Scaffold(
        Modifier.statusBarsPadding(),
        bottomBar = {
            Column(Modifier.alpha(0.97f)) {
                Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                    Divider(thickness = 1.dp)

                    androidx.compose.material3.BottomAppBar(
                        Modifier
                            .navigationBarsPadding()
                            .height(48.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                    ) {
                        TabNavigationItem(
                            selectedTab == "home",
                            { selectedTab = "home" },
                            icon = { Icon(Icons.Outlined.Home, null) },
                            title = { Text(text = "首页") },
                        )
                        TabNavigationItem(
                            selectedTab == "search",
                            { selectedTab = "search" },
                            icon = { Icon(Icons.Outlined.Search, null) },
                            title = { Text(text = "找番") },
                        )
                        TabNavigationItem(
                            selectedTab == "profile",
                            { selectedTab = "profile" },
                            icon = { Icon(Icons.Outlined.Person, null) },
                            title = { Text(text = "我的") },
                        )
                    }
                }
            }
        }
    ) { paddingValues ->

        CompositionLocalProvider(LocalContentPaddings provides paddingValues) {
            when (selectedTab) {
                "home" -> HomePage()
                "search" -> SearchPage(searchViewModel)
                "profile" -> AuthPage(accountViewModel)
            }
        }
    }
}