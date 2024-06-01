package me.him188.ani.app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.pages.cache.manage.CacheManagementPage
import me.him188.ani.app.requireOnline
import me.him188.ani.app.ui.collection.CollectionPage
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import me.him188.ani.app.ui.foundation.layout.isShowLandscapeUI
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.home.HomePage
import me.him188.ani.app.ui.home.SearchViewModel
import me.him188.ani.app.ui.isLoggedIn
import me.him188.ani.app.ui.profile.AccountViewModel
import me.him188.ani.app.ui.profile.ProfilePage
import me.him188.ani.app.ui.settings.SettingsPage
import me.him188.ani.app.ui.update.ChangelogDialog
import me.him188.ani.app.ui.update.UpdateCheckerState
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle


@Composable
fun HomeScene(modifier: Modifier = Modifier) {
    if (isShowLandscapeUI()) {
        HomeSceneLandscape(modifier)
    } else {
        HomeScenePortrait(modifier)
    }
}

@Composable
private fun UserAvatar(
    modifier: Modifier = Modifier,
    vm: AccountViewModel = rememberViewModel { AccountViewModel() },
) {
    val user by vm.selfInfo.collectAsStateWithLifecycle()
    val loggedIn by isLoggedIn()
    if (loggedIn == false) {
        val navigator = LocalNavigator.current
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
            IconButton({ vm.requireOnline(navigator = navigator) }, modifier) {
                Icon(Icons.AutoMirrored.Rounded.Login, "登录")
            }
        }
    } else {
        AvatarImage(
            url = user?.avatar?.medium,
            modifier.clip(CircleShape).placeholder(user == null),
        )
    }
}

@Composable
private fun UpdateCheckerItem(
    state: UpdateCheckerState = rememberViewModel { UpdateCheckerState() },
) {
    SideEffect {
        state.startCheckLatestVersion()
    }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    if (showDialog) {
        state.latestVersion?.let {
            ChangelogDialog(
                latestVersion = it,
                { showDialog = false },
                currentVersion = state.currentVersion
            )
        }
    }
    if (state.hasUpdate) {
        NavigationRailItem(
            false,
            onClick = {
                showDialog = true
            },
            icon = { Icon(Icons.Rounded.Update, null, tint = MaterialTheme.colorScheme.error) },
            label = { Text(text = "更新", color = MaterialTheme.colorScheme.error) }
        )
    }
}

@Composable
private fun HomeSceneLandscape(
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(1) { 4 }
    val uiScope = rememberCoroutineScope()

    Row(modifier) {
        Surface {
            Column {
                // NavigationRail 宽度至少为 80.dp
                NavigationRail(
                    Modifier.padding(top = 16.dp).weight(1f),
                    header = {
                        UserAvatar(Modifier.size(48.dp))
                    }
                ) {
                    Column(
                        Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NavigationRailItem(
                            pagerState.currentPage == 0,
                            onClick = {
                                uiScope.launch {
                                    pagerState.scrollToPage(0)
                                }
                            },
                            icon = { Icon(Icons.Rounded.TravelExplore, null) },
                            label = { Text(text = "找番") }
                        )
                        NavigationRailItem(
                            pagerState.currentPage == 1,
                            onClick = {
                                uiScope.launch {
                                    pagerState.scrollToPage(1)
                                }
                            },
                            icon = { Icon(Icons.Rounded.Star, null) },
                            label = { Text(text = "追番") }
                        )
                        NavigationRailItem(
                            pagerState.currentPage == 2,
                            onClick = {
                                uiScope.launch {
                                    pagerState.scrollToPage(2)
                                }
                            },
                            icon = { Icon(Icons.Rounded.DownloadDone, null) },
                            label = { Text(text = "缓存") }
                        )

                        UpdateCheckerItem()

                        Spacer(Modifier.weight(1f))

                        NavigationRailItem(
                            pagerState.currentPage == 3,
                            onClick = {
                                uiScope.launch {
                                    pagerState.scrollToPage(3)
                                }
                            },
                            icon = { Icon(Icons.Rounded.Settings, null) },
                            label = { Text(text = "设置") }
                        )
                    }
                }
            }
        }
        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(Modifier.fillMaxHeight().weight(1f)) {
            val navigator by rememberUpdatedState(LocalNavigator.current)

            val contentPadding = PaddingValues(16.dp)

            CompositionLocalProvider(LocalContentPaddings provides contentPadding) {
                VerticalPager(pagerState, userScrollEnabled = false) {
                    when (it) {
                        0 -> HomePage(contentPadding)
                        1 -> CollectionPage(
                            onClickCaches = {
                                navigator.navigateCaches()
                            },
                            Modifier.fillMaxSize(),
                            contentPadding,
                        )

                        2 -> CacheManagementPage(Modifier.fillMaxSize())
                        3 -> SettingsPage(Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScenePortrait(
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(1) { 3 }
    val uiScope = rememberCoroutineScope()
    Scaffold(
        modifier.fillMaxSize(),
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
                            pagerState.currentPage == 0,
                            onClick = {
                                uiScope.launch {
                                    pagerState.scrollToPage(0)
                                }
                                closeSearch()
                            },
                            icon = { Icon(Icons.Rounded.TravelExplore, null) },
                            label = { Text(text = "找番") }
                        )
                        NavigationBarItem(
                            pagerState.currentPage == 1,
                            onClick = {
                                uiScope.launch {
                                    pagerState.scrollToPage(1)
                                }
                                closeSearch()
                            },
                            icon = { Icon(Icons.Rounded.Star, null) },
                            label = { Text(text = "追番") }
                        )
                        NavigationBarItem(
                            pagerState.currentPage == 2,
                            onClick = {
                                uiScope.launch {
                                    pagerState.scrollToPage(2)
                                }
                                closeSearch()
                            },
                            icon = { Icon(Icons.Rounded.Settings, null) },
                            label = { Text(text = "我的") }
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { contentPadding ->
        val navigator by rememberUpdatedState(LocalNavigator.current)

        CompositionLocalProvider(LocalContentPaddings provides contentPadding) {
            HorizontalPager(pagerState, userScrollEnabled = false) {
                when (it) {
                    0 -> HomePage(contentPadding)
                    1 -> CollectionPage(
                        onClickCaches = {
                            navigator.navigateCaches()
                        },
                        contentPadding = contentPadding,
                    )

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
