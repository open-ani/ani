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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import me.him188.ani.app.platform.window.desktopTitleBarPadding
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
import me.him188.ani.app.ui.settings.tabs.media.source.EditMediaSourceMode
import me.him188.ani.app.ui.settings.tabs.media.source.EditRssMediaSourcePage
import me.him188.ani.app.ui.settings.tabs.media.source.EditRssMediaSourceViewModel
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
        NavHost(navController, startDestination = "/home", modifier) {
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
                WelcomeScene(viewModel { WelcomeViewModel() }, Modifier.desktopTitleBarPadding().fillMaxSize())
            }
            composable(
                "/home",
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition,
            ) {
                HomeScene()
            }
            composable(
                "/bangumi-oauth",
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition,
            ) {
                BangumiOAuthScene(viewModel { BangumiOAuthViewModel() }, Modifier.desktopTitleBarPadding())
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
                    Modifier.desktopTitleBarPadding().fillMaxSize(),
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
                SubjectDetailsScene(vm, Modifier.desktopTitleBarPadding())
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
                EpisodeScene(vm, Modifier.desktopTitleBarPadding())
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
                    Modifier.desktopTitleBarPadding().fillMaxSize(),
                    initialTab = initialTab,
                    allowBack = backStackEntry.arguments?.getBoolean("back") ?: false,
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
                    Modifier.desktopTitleBarPadding().fillMaxSize(),
                    showBack = true,
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
                    Modifier.desktopTitleBarPadding().fillMaxSize(),
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
                SubjectCacheScene(vm, Modifier.desktopTitleBarPadding())
            }
            composable(
                "/settings/media-source/edit/{factoryId}?existingMediaSourceInstanceId={existingMediaSourceInstanceId}",
                arguments = listOf(
                    navArgument("factoryId") { type = NavType.StringType },
                    navArgument("existingMediaSourceInstanceId") { type = NavType.StringType },
                ),
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition,
            ) { backStackEntry ->
                val factoryId = backStackEntry.arguments?.getString("factoryId") ?: kotlin.run {
                    navController.popBackStack()
                    return@composable
                }
                val mediaSourceInstanceId = backStackEntry.arguments?.getString("existingMediaSourceInstanceId")
                when (FactoryId(factoryId)) {
                    RssMediaSource.FactoryId -> EditRssMediaSourcePage(
                        viewModel<EditRssMediaSourceViewModel>(key = mediaSourceInstanceId) {
                            EditRssMediaSourceViewModel(
                                if (mediaSourceInstanceId == null) EditMediaSourceMode.Add
                                else EditMediaSourceMode.Edit(instanceId = mediaSourceInstanceId),
                            )
                        },
                        Modifier.desktopTitleBarPadding()
                    )

                    else -> error("Unknown factoryId: $factoryId")
                }
            }
        }
    }
}