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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.isInLandscapeMode
import me.him188.ani.app.ui.auth.AuthRequestScene
import me.him188.ani.app.ui.collection.CollectionPage
import me.him188.ani.app.ui.collection.MyCollectionsViewModel
import me.him188.ani.app.ui.foundation.TabNavigationItem
import me.him188.ani.app.ui.profile.AuthViewModel
import me.him188.ani.app.ui.profile.ProfilePage
import me.him188.ani.app.ui.subject.details.SubjectDetailsScene
import me.him188.ani.app.ui.subject.details.SubjectDetailsViewModel
import me.him188.ani.app.ui.subject.episode.EpisodeScene
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.path
import moe.tlaster.precompose.navigation.query
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.viewmodel.viewModel

/**
 * 由 bottom bar 等导致的 paddings
 */
val LocalContentPaddings: ProvidableCompositionLocal<PaddingValues> = androidx.compose.runtime.compositionLocalOf {
    PaddingValues(0.dp)
}

@Composable
fun MainScreen(aniNavigator: AniNavigator) {
    val navigator = rememberNavigator()
    CompositionLocalProvider(LocalNavigator provides aniNavigator) {
        // 当前授权状态
        val myCollectionsViewModel = remember { MyCollectionsViewModel() }
        val searchViewModel = remember { SearchViewModel() }

        if (isInLandscapeMode()) {
            MainScreenLandscape(navigator, myCollectionsViewModel, searchViewModel)
        } else {
            MainScreenPortrait(navigator, myCollectionsViewModel, searchViewModel)
        }
        SideEffect {
            aniNavigator.setNavigator(navigator)
        }
    }
}

@Composable
fun MainScreenLandscape(
    navigator: Navigator,
    myCollectionsViewModel: MyCollectionsViewModel,
    searchViewModel: SearchViewModel
) {
    MainScreenPortrait(
        navigator,
        myCollectionsViewModel,
        searchViewModel
    )
}

var currentAuthViewModel: AuthViewModel? = null // TODO: remove this shit

@Composable
fun MainScreenPortrait(
    navigator: Navigator,
    myCollectionsViewModel: MyCollectionsViewModel,
    searchViewModel: SearchViewModel
) {
    val authViewModel = remember { AuthViewModel().also { currentAuthViewModel = it } }
//    val subjectDetailsViewModel = remember { SubjectDetailsViewModel() }
    NavHost(navigator, initialRoute = "/home") {
        scene("/home") {
            HomeScene(searchViewModel, myCollectionsViewModel)
        }
        scene("/auth") { backStackEntry ->
            val allowBack = backStackEntry.query("allowBack") ?: false
            AuthRequestScene(authViewModel, allowBack, navigator)
        }
        scene("/subjects/{subjectId}") { backStackEntry ->
            val subjectId = backStackEntry.path<Int>("subjectId") ?: run {
                navigator.goBack()
                return@scene
            }
            val vm = viewModel<SubjectDetailsViewModel> { SubjectDetailsViewModel(subjectId) }
            SubjectDetailsScene(vm, navigator)
        }
        scene("/subjects/{subjectId}/episodes/{episodeId}") { backStackEntry ->
            val subjectId = backStackEntry.path<Int>("subjectId") ?: run {
                navigator.goBack()
                return@scene
            }
            val episodeId = backStackEntry.path<Int>("episodeId") ?: run {
                navigator.goBack()
                return@scene
            }
            val vm = viewModel<EpisodeViewModel> { EpisodeViewModel(subjectId, episodeId) }
            EpisodeScene(vm)
        }
    }
}

@Composable
private fun HomeScene(
    searchViewModel: SearchViewModel,
    myCollectionsViewModel: MyCollectionsViewModel
) {
    var selectedTab by remember { mutableStateOf("collection") }
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
            when (selectedTab) {
                "home" -> HomePage(searchViewModel, contentPadding)
                "collection" -> CollectionPage(contentPadding, myCollectionsViewModel)
                "profile" -> ProfilePage()
            }
        }
    }
}