package me.him188.ani.app.ui.main

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import me.him188.ani.app.data.source.media.source.RssMediaSource
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.window.desktopTitleBar
import me.him188.ani.app.ui.cache.CacheManagementPage
import me.him188.ani.app.ui.cache.CacheManagementViewModel
import me.him188.ani.app.ui.cache.details.MediaCacheDetailsPage
import me.him188.ani.app.ui.cache.details.MediaCacheDetailsPageViewModel
import me.him188.ani.app.ui.foundation.animation.EmphasizedDecelerateEasing
import me.him188.ani.app.ui.profile.BangumiOAuthViewModel
import me.him188.ani.app.ui.profile.SettingsViewModel
import me.him188.ani.app.ui.profile.auth.BangumiOAuthScene
import me.him188.ani.app.ui.profile.auth.BangumiTokenAuthPage
import me.him188.ani.app.ui.profile.auth.BangumiTokenAuthViewModel
import me.him188.ani.app.ui.profile.auth.WelcomeScene
import me.him188.ani.app.ui.profile.auth.WelcomeViewModel
import me.him188.ani.app.ui.settings.SettingsPage
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.tabs.media.source.rss.EditRssMediaSourcePage
import me.him188.ani.app.ui.settings.tabs.media.source.rss.EditRssMediaSourceViewModel
import me.him188.ani.app.ui.subject.cache.SubjectCacheScene
import me.him188.ani.app.ui.subject.cache.SubjectCacheViewModelImpl
import me.him188.ani.app.ui.subject.details.SubjectDetailsScene
import me.him188.ani.app.ui.subject.details.SubjectDetailsViewModel
import me.him188.ani.app.ui.subject.episode.EpisodeScene
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel
import me.him188.ani.datasources.api.source.FactoryId
import kotlin.math.roundToInt

@Composable
fun AniAppContentPortrait(
    aniNavigator: AniNavigator,
    modifier: Modifier = Modifier
) {
    val navController = aniNavigator.navigator
    CompositionLocalProvider(LocalNavigator provides aniNavigator) {

        // 必须传给所有 Scaffold 和 TopAppBar. 注意, 如果你不传, 你的 UI 很可能会在 macOS 不工作.
        val windowInsets = ScaffoldDefaults.contentWindowInsets
            .add(WindowInsets.desktopTitleBar()) // Compose 目前不支持这个所以我们要自己加上

        val density = LocalDensity.current
        NavHost(
            navController, startDestination = "/home",
            modifier.consumeWindowInsets(
                PaddingValues(
                    top = -with(density) {
                        WindowInsets.desktopTitleBar().getTop(density).toDp()
                    }, // add insets
                ),
            ),
        ) {
            // https://m3.material.io/styles/motion/easing-and-duration/applying-easing-and-duration#e5b958f0-435d-4e84-aed4-8d1ea395fa5c
            val enterDuration = 500
            val exitDuration = 200

            // https://m3.material.io/styles/motion/easing-and-duration/applying-easing-and-duration#26a169fb-caf3-445e-8267-4f1254e3e8bb
            // TODO: We should actually use Container transform in CMP 1.7
            // https://developer.android.com/develop/ui/compose/animation/shared-elements
            val enterEasing = EmphasizedDecelerateEasing
            val exitEasing = LinearOutSlowInEasing

            val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
                slideInHorizontally(tween(enterDuration, easing = enterEasing)) { (it * (1f / 5)).roundToInt() }
                    .plus(fadeIn(tween(enterDuration, easing = enterEasing)))
            }

            val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
                fadeOut(tween(exitDuration, easing = exitEasing))
            }

            val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
                fadeIn(tween(enterDuration, easing = enterEasing))
            }

            // 从页面 A 回到上一个页面 B, 切走页面 A 的动画
            val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
                slideOutHorizontally(tween(exitDuration, easing = exitEasing)) { (it * (1f / 7)).roundToInt() }
                    .plus(fadeOut(tween(exitDuration, easing = exitEasing)))
            }

            composable(
                "/welcome",
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition,
            ) { // 由 SessionManager.requireAuthorize 跳转到
                WelcomeScene(viewModel { WelcomeViewModel() }, Modifier.fillMaxSize(), windowInsets)
            }
            composable(
                "/home",
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition,
            ) {
                HomeScene(windowInsets = windowInsets)
            }
            composable(
                "/bangumi-oauth",
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition,
            ) {
                BangumiOAuthScene(viewModel { BangumiOAuthViewModel() }, windowInsets = windowInsets)
            }
            composable(
                "/bangumi-token-auth",
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition,
            ) {
                BangumiTokenAuthPage(
                    viewModel { BangumiTokenAuthViewModel() },
                    Modifier.fillMaxSize(),
                    windowInsets,
                )
            }
            composable(
                "/subjects/{subjectId}",
                arguments = listOf(navArgument("subjectId") { type = NavType.IntType }),
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition,
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: run {
                    navController.popBackStack()
                    return@composable
                }
                val vm = viewModel<SubjectDetailsViewModel> { SubjectDetailsViewModel(subjectId) }
                SideEffect { vm.navigator = aniNavigator }
                SubjectDetailsScene(vm, windowInsets = windowInsets)
            }
            composable(
                "/subjects/{subjectId}/episodes/{episodeId}?fullscreen={fullscreen}",
                arguments = listOf(
                    navArgument("subjectId") { type = NavType.IntType },
                    navArgument("episodeId") { type = NavType.IntType },
                    navArgument("fullscreen") { type = NavType.BoolType },
                ),
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition,
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: run {
                    navController.popBackStack()
                    return@composable
                }

                val episodeId = backStackEntry.arguments?.getInt("episodeId") ?: run {
                    navController.popBackStack()
                    return@composable
                }
                val initialIsFullscreen = backStackEntry.arguments?.getBoolean("fullscreen") ?: false
                val context = LocalContext.current
                val vm = viewModel<EpisodeViewModel>(
                    key = "$subjectId-$episodeId",
                ) {
                    EpisodeViewModel(
                        initialSubjectId = subjectId,
                        initialEpisodeId = episodeId,
                        initialIsFullscreen = initialIsFullscreen,
                        context,
                    )
                }
                EpisodeScene(vm, Modifier.fillMaxSize(), windowInsets)
            }
            composable(
                "/settings?tab={tab}&back={back}",
                arguments = listOf(
                    navArgument("tab") { type = NavType.IntType },
                    navArgument("back") { type = NavType.BoolType },
                ),
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition,
            ) { backStackEntry ->
                val initialTab = backStackEntry.arguments?.getInt("tab")
                    ?.let { SettingsTab.entries.getOrNull(it) }
                    ?: SettingsTab.MEDIA

                SettingsPage(
                    viewModel {
                        SettingsViewModel()
                    },
                    Modifier.fillMaxSize(),
                    initialTab = initialTab,
                    allowBack = backStackEntry.arguments?.getBoolean("back") ?: false,
                    contentWindowInsets = windowInsets,
                )
            }
            composable(
                "/caches",
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition,
            ) {
                CacheManagementPage(
                    viewModel { CacheManagementViewModel(aniNavigator) },
                    Modifier.fillMaxSize(),
                    showBack = true,
                    windowInsets = windowInsets,
                )
            }
            composable(
                "/caches/{cacheId}",
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition,
                arguments = listOf(navArgument("cacheId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val cacheId = backStackEntry.arguments?.getString("cacheId") ?: run {
                    navController.popBackStack()
                    return@composable
                }
                MediaCacheDetailsPage(
                    viewModel(key = cacheId) { MediaCacheDetailsPageViewModel(cacheId) },
                    Modifier.fillMaxSize(),
                    windowInsets = windowInsets,
                )
            }
            composable(
                "/subjects/{subjectId}/caches",
                arguments = listOf(navArgument("subjectId") { type = NavType.IntType }),
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition,
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: run {
                    navController.popBackStack()
                    return@composable
                }
                // Don't use rememberViewModel to save memory
                val vm = remember(subjectId) { SubjectCacheViewModelImpl(subjectId) }
                SubjectCacheScene(vm, Modifier.fillMaxSize(), windowInsets)
            }
            composable(
                "/settings/media-source/edit?factoryId={factoryId}&mediaSourceInstanceId={mediaSourceInstanceId}",
                arguments = listOf(
                    navArgument("factoryId") { type = NavType.StringType },
                    navArgument("mediaSourceInstanceId") { type = NavType.StringType },
                ),
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition,
            ) { backStackEntry ->
                val factoryIdString = backStackEntry.arguments?.getString("factoryId") ?: error("factoryId is required")
                val factoryId = FactoryId(factoryIdString)
                val mediaSourceInstanceId = backStackEntry.arguments?.getString("mediaSourceInstanceId")
                    ?: error("mediaSourceInstanceId is required")
                when (factoryId) {
                    RssMediaSource.FactoryId -> EditRssMediaSourcePage(
                        viewModel<EditRssMediaSourceViewModel>(key = mediaSourceInstanceId) {
                            EditRssMediaSourceViewModel(mediaSourceInstanceId)
                        },
                        Modifier,
                        windowInsets,
                    )

                    else -> error("Unknown factoryId: $factoryId")
                }
            }
        }
    }
}