package me.him188.ani.app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.navigation.OverrideNavigation
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.currentPlatform
import me.him188.ani.app.platform.isAndroid
import me.him188.ani.app.platform.setRequestFullScreen
import me.him188.ani.app.platform.window.LocalPlatformWindow
import me.him188.ani.app.platform.window.desktopTitleBar
import me.him188.ani.app.platform.window.desktopTitleBarPadding
import me.him188.ani.app.platform.window.plus
import me.him188.ani.app.tools.update.InstallationFailureReason
import me.him188.ani.app.ui.cache.CacheManagementPage
import me.him188.ani.app.ui.cache.CacheManagementViewModel
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import me.him188.ani.app.ui.foundation.layout.isShowLandscapeUI
import me.him188.ani.app.ui.home.HomePage
import me.him188.ani.app.ui.home.search.SearchViewModel
import me.him188.ani.app.ui.profile.AccountViewModel
import me.him188.ani.app.ui.profile.ProfilePage
import me.him188.ani.app.ui.settings.SettingsPage
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.subject.collection.CollectionPage
import me.him188.ani.app.ui.subject.collection.components.SessionTipsIcon
import me.him188.ani.app.ui.update.AutoUpdateViewModel
import me.him188.ani.app.ui.update.ChangelogDialog
import me.him188.ani.app.ui.update.FailedToInstallDialog
import me.him188.ani.app.ui.update.UpdateLogoIcon
import me.him188.ani.app.ui.update.UpdateLogoLabel
import me.him188.ani.app.ui.update.handleClickLogo


@Composable
fun HomeScene(modifier: Modifier = Modifier) {
    if (currentPlatform.isAndroid()) {
        val context = LocalContext.current
        val window = LocalPlatformWindow.current
        LaunchedEffect(true) {
            context.setRequestFullScreen(window, false)
        }
    }

    if (isShowLandscapeUI()) {
        HomeSceneLandscape(modifier)
    } else {
        HomeScenePortrait(modifier)
    }
}

@Composable
private fun UpdateCheckerItem(
    vm: AutoUpdateViewModel = viewModel { AutoUpdateViewModel() },
) {
    SideEffect {
        vm.startAutomaticCheckLatestVersion()
    }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val context by rememberUpdatedState(LocalContext.current)
    if (showDialog) {
        vm.latestVersion?.let {
            ChangelogDialog(
                latestVersion = it,
                onDismissRequest = { showDialog = false },
                onStartDownload = { vm.startDownload(it, context) },
                currentVersion = vm.currentVersion,
            )
        }
    }
    if (vm.hasUpdate) {
        var installationError by remember { mutableStateOf<InstallationFailureReason?>(null) }
        if (installationError != null) {
            FailedToInstallDialog({ installationError = null }, { vm.logoState })
        }

        NavigationRailItem(
            false,
            onClick = {
                vm.handleClickLogo(
                    context,
                    onInstallationError = { installationError = it },
                    showChangelogDialog = { showDialog = true },
                )
            },
            icon = { UpdateLogoIcon(vm.logoState) },
            label = { UpdateLogoLabel(vm.logoState) },
        )
    }
}

@Composable
private fun HomeSceneLandscape(
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(1) { 4 }
    val uiScope = rememberCoroutineScope()
    val searchBarFocusRequester = remember { FocusRequester() }

    OverrideNavigation(
        { old ->
            object : AniNavigator by old {
                override fun navigateSearch(requestFocus: Boolean) {
                    uiScope.launch {
                        pagerState.scrollToPage(0)
                        if (requestFocus) {
                            searchBarFocusRequester.requestFocus()
                        }
                    }
                }

                override fun navigateSettings(tab: SettingsTab) {
                    uiScope.launch {
                        pagerState.scrollToPage(3)
                    }
                }
            }
        },
    ) {
        Row(modifier) {
            Surface {
                Column {
                    // NavigationRail 宽度至少为 80.dp
                    NavigationRail(
                        Modifier.desktopTitleBarPadding().padding(top = 16.dp).weight(1f),
                        header = {
                            val vm = viewModel { AccountViewModel() }
                            if (vm.authState.isLoading || vm.authState.isKnownLoggedIn) {
                                // 加载中时展示 placeholder
                                AvatarImage(
                                    url = vm.selfInfo?.avatarUrl,
                                    Modifier.size(48.dp).clip(CircleShape).placeholder(vm.selfInfo == null),
                                )
                            } else {
                                if (vm.authState.isKnownGuest) {
                                    val navigator = LocalNavigator.current
                                    TextButton({ vm.authState.launchAuthorize(navigator) }) {
                                        Text("登录")
                                    }
                                } else {
                                    SessionTipsIcon(vm.authState, showLabel = false)
                                }
                            }
                        },
                    ) {
                        Column(
                            Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            NavigationRailItem(
                                pagerState.currentPage == 0,
                                onClick = {
                                    uiScope.launch {
                                        pagerState.scrollToPage(0)
                                    }
                                },
                                icon = { Icon(Icons.Rounded.TravelExplore, null) },
                                label = { Text(text = "找番") },
                            )
                            NavigationRailItem(
                                pagerState.currentPage == 1,
                                onClick = {
                                    uiScope.launch {
                                        pagerState.scrollToPage(1)
                                    }
                                },
                                icon = { Icon(Icons.Rounded.Star, null) },
                                label = { Text(text = "追番") },
                            )
                            NavigationRailItem(
                                pagerState.currentPage == 2,
                                onClick = {
                                    uiScope.launch {
                                        pagerState.scrollToPage(2)
                                    }
                                },
                                icon = { Icon(Icons.Rounded.DownloadDone, null) },
                                label = { Text(text = "缓存") },
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
                                label = { Text(text = "设置") },
                            )
                        }
                    }
                }
            }

            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant) // 画到 title bar 里面

            Column(Modifier.fillMaxHeight().weight(1f)) {
                val navigator by rememberUpdatedState(LocalNavigator.current)
                val insets =
                    ScaffoldDefaults.contentWindowInsets
                        .plus(WindowInsets.desktopTitleBar())

                Surface(Modifier.height(16.dp).fillMaxWidth()) {}

                VerticalPager(pagerState, userScrollEnabled = false) {
                    when (it) {
                        0 -> {
                            HomePage(
                                Modifier.fillMaxSize(),
                                searchBarFocusRequester = searchBarFocusRequester,
                                contentWindowInsets = insets,
                            )
                        }

                        1 -> CollectionPage(
                            onClickCaches = {
                                navigator.navigateCaches()
                            },
                            Modifier.fillMaxSize(),
                            contentWindowInsets = insets,
                        )

                        2 -> CacheManagementPage(
                            viewModel { CacheManagementViewModel(navigator) },
                            Modifier.fillMaxSize(),
                            contentWindowInsets = insets,
                        )

                        3 -> SettingsPage(
                            Modifier.fillMaxSize(),
                            contentWindowInsets = insets,
                        )
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
    val searchBarFocusRequester = remember { FocusRequester() }
    Scaffold(
        modifier.fillMaxSize(),
        bottomBar = {
            val searchViewModel = viewModel { SearchViewModel() }
            fun closeSearch() {
                searchViewModel.searchActive = false
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
                            label = { Text(text = "找番") },
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
                            label = { Text(text = "追番") },
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
                            label = { Text(text = "我的") },
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.desktopTitleBar(), // no systemBars
    ) { contentPadding -> // only contains padding of bottom bottom appbar
        val navigator by rememberUpdatedState(LocalNavigator.current)

        OverrideNavigation(
            { old ->
                object : AniNavigator by old {
                    override fun navigateSearch(requestFocus: Boolean) {
                        uiScope.launch {
                            pagerState.scrollToPage(0)
                            searchBarFocusRequester.requestFocus()
                        }
                    }
                }
            },
        ) {
            HorizontalPager(pagerState, userScrollEnabled = false) {
                when (it) {
                    0 -> HomePage(
                        contentPadding = contentPadding,
                        searchBarFocusRequester = searchBarFocusRequester,
                    )

                    1 -> CollectionPage(
                        onClickCaches = {
                            navigator.navigateCaches()
                        },
                        contentPadding = contentPadding,
                    )

                    2 -> {
                        ProfilePage(
                            contentPadding = contentPadding,
                            onClickSettings = {
                                navigator.navigateSettings()
                            },
                        )
                    }
                }
            }
        }
    }

}
