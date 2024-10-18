/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.navigation.MainScenePage
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.tools.update.InstallationFailureReason
import me.him188.ani.app.ui.adaptive.navigation.AniNavigationSuite
import me.him188.ani.app.ui.adaptive.navigation.AniNavigationSuiteLayout
import me.him188.ani.app.ui.cache.CacheManagementPage
import me.him188.ani.app.ui.cache.CacheManagementViewModel
import me.him188.ani.app.ui.exploration.ExplorationPage
import me.him188.ani.app.ui.exploration.search.SearchPage
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import me.him188.ani.app.ui.foundation.layout.LocalPlatformWindow
import me.him188.ani.app.ui.foundation.layout.desktopTitleBarPadding
import me.him188.ani.app.ui.foundation.layout.setRequestFullScreen
import me.him188.ani.app.ui.foundation.session.SessionTipsIcon
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.app.ui.profile.AccountViewModel
import me.him188.ani.app.ui.settings.SettingsPage
import me.him188.ani.app.ui.settings.SettingsViewModel
import me.him188.ani.app.ui.subject.collection.CollectionPage
import me.him188.ani.app.ui.subject.details.SubjectDetailsScene
import me.him188.ani.app.ui.update.AutoUpdateViewModel
import me.him188.ani.app.ui.update.ChangelogDialog
import me.him188.ani.app.ui.update.FailedToInstallDialog
import me.him188.ani.app.ui.update.UpdateLogoIcon
import me.him188.ani.app.ui.update.UpdateLogoLabel
import me.him188.ani.app.ui.update.handleClickLogo
import me.him188.ani.utils.platform.isAndroid


@Composable
fun MainScene(
    page: MainScenePage,
    modifier: Modifier = Modifier,
    onNavigateToPage: (MainScenePage) -> Unit,
    windowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets, // Compose for Desktop 目前不会考虑这个
    navigationLayoutType: NavigationSuiteType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
        currentWindowAdaptiveInfo(),
    ),
) {
    if (LocalPlatform.current.isAndroid()) {
        val context = LocalContext.current
        val window = LocalPlatformWindow.current
        LaunchedEffect(true) {
            context.setRequestFullScreen(window, false)
        }
    }

    MainSceneContent(page, windowInsets, onNavigateToPage, modifier, navigationLayoutType)
}

@Composable
private fun MainSceneContent(
    page: MainScenePage,
    windowInsets: WindowInsets,
    onNavigateToPage: (MainScenePage) -> Unit,
    modifier: Modifier = Modifier,
    navigationLayoutType: NavigationSuiteType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
        currentWindowAdaptiveInfo(),
    ),
) {
    AniNavigationSuiteLayout(
        navigationSuite = {
            AniNavigationSuite(
                windowInsets,
                navigationRailHeader = {
                    FloatingActionButton(
                        { onNavigateToPage(MainScenePage.Search) },
                        Modifier
                            .desktopTitleBarPadding()
                            .padding(vertical = 48.dp),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                    ) {
                        Icon(Icons.Rounded.Search, "搜索")
                    }
                },
                colors = NavigationSuiteDefaults.colors(
                    navigationDrawerContainerColor = AniThemeDefaults.navigationContainerColor,
                    navigationBarContainerColor = AniThemeDefaults.navigationContainerColor,
                    navigationRailContainerColor = AniThemeDefaults.navigationContainerColor,
                ),
                navigationRailItemSpacing = 8.dp,
            ) {
                @Stable
                fun getIcon(page: MainScenePage): ImageVector = when (page) {
                    MainScenePage.Exploration -> Icons.Rounded.TravelExplore
                    MainScenePage.Collection -> Icons.Rounded.Star
                    MainScenePage.CacheManagement -> Icons.Rounded.DownloadDone
                    MainScenePage.Settings -> Icons.Rounded.Settings
                    MainScenePage.Search -> Icons.Rounded.Search
                }

                @Stable
                fun getText(page: MainScenePage): String = when (page) {
                    MainScenePage.Exploration -> "探索"
                    MainScenePage.Collection -> "追番"
                    MainScenePage.CacheManagement -> "缓存"
                    MainScenePage.Settings -> "设置"
                    MainScenePage.Search -> "搜索"
                }

                for (entry in MainScenePage.visibleEntries) {
                    item(
                        page == entry,
                        onClick = { onNavigateToPage(entry) },
                        icon = { Icon(getIcon(entry), null) },
                        label = { Text(text = getText(entry)) },
                    )
                }
            }
        },
        modifier,
        layoutType = navigationLayoutType,
    ) {
        val navigator by rememberUpdatedState(LocalNavigator.current)
        AnimatedContent(
            page,
            Modifier.fillMaxSize(),
            transitionSpec = {
//                val easing = CubicBezierEasing(0f, 0f, 1f, 1f)
//                val fadeIn = fadeIn(tween(25, easing = easing))
//                val fadeOut = fadeOut(tween(25, easing = easing))
//                fadeIn togetherWith fadeOut
                fadeIn(snap()) togetherWith fadeOut(snap())
            },
        ) { page ->
            TabContent(layoutType = navigationLayoutType) {
                when (page) {
                    MainScenePage.Exploration -> {
                        ExplorationPage(
                            viewModel { ExplorationPageViewModel() }.explorationPageState,
                            onSearch = { onNavigateToPage(MainScenePage.Search) },
                            modifier.fillMaxSize(),
                            contentWindowInsets = windowInsets,
                        )
                    }

                    MainScenePage.Collection -> CollectionPage(
                        windowInsets = windowInsets,
                        onClickSearch = { onNavigateToPage(MainScenePage.Search) },
                        Modifier.fillMaxSize(),
                    )

                    MainScenePage.CacheManagement -> CacheManagementPage(
                        viewModel { CacheManagementViewModel(navigator) },
                        showBack = false,
                        Modifier.fillMaxSize(),
                        windowInsets = windowInsets,
                    )

                    MainScenePage.Settings -> SettingsPage(
                        viewModel {
                            SettingsViewModel()
                        },
                        Modifier.fillMaxSize(),
                        contentWindowInsets = windowInsets,
                    )

                    MainScenePage.Search -> {
                        val vm = viewModel { SearchViewModel() }
                        SearchPage(
                            vm.searchPageState,
                            windowInsets,
                            detailContent = {
                                vm.subjectDetailsViewModelFlow.collectAsStateWithLifecycle(null).value?.let {
                                    SubjectDetailsScene(it)
                                }
                            },
                            Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun TabContent(
    layoutType: NavigationSuiteType,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = when (layoutType) {
        NavigationSuiteType.NavigationBar,
        NavigationSuiteType.None -> RectangleShape

        NavigationSuiteType.NavigationRail,
        NavigationSuiteType.NavigationDrawer -> MaterialTheme.shapes.extraLarge.copy(
            topEnd = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp),
        )

        else -> RectangleShape
    }
    Surface(
        modifier.clip(shape),
        shape = shape,
        color = AniThemeDefaults.pageContentBackgroundColor,
    ) {
        content()
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
    val uriHandler = LocalUriHandler.current
    if (showDialog) {
        vm.latestVersion?.let {
            ChangelogDialog(
                latestVersion = it,
                onDismissRequest = { showDialog = false },
                onStartDownload = { vm.startDownload(it, uriHandler) },
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
                    uriHandler,
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
private fun UserAvatarInNavigation(modifier: Modifier = Modifier) {
    Box(modifier) {
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
    }
}
