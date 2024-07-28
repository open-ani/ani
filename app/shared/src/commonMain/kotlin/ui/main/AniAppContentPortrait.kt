package me.him188.ani.app.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.window.desktopTitleBarPadding
import me.him188.ani.app.ui.cache.CacheManagementPage
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.profile.BangumiOAuthViewModel
import me.him188.ani.app.ui.profile.auth.BangumiOAuthScene
import me.him188.ani.app.ui.profile.auth.BangumiTokenAuthPage
import me.him188.ani.app.ui.profile.auth.BangumiTokenAuthViewModel
import me.him188.ani.app.ui.profile.auth.WelcomeScene
import me.him188.ani.app.ui.profile.auth.WelcomeViewModel
import me.him188.ani.app.ui.settings.SettingsPage
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.subject.cache.SubjectCacheScene
import me.him188.ani.app.ui.subject.cache.SubjectCacheViewModelImpl
import me.him188.ani.app.ui.subject.details.SubjectDetailsScene
import me.him188.ani.app.ui.subject.details.SubjectDetailsViewModel
import me.him188.ani.app.ui.subject.episode.EpisodeScene
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.path
import moe.tlaster.precompose.navigation.query

@Composable
fun AniAppContentPortrait(
    aniNavigator: AniNavigator,
    modifier: Modifier = Modifier
) {
    val navigator = aniNavigator.navigator
    CompositionLocalProvider(LocalNavigator provides aniNavigator) {
        NavHost(navigator, initialRoute = "/home", modifier = modifier) {
            scene("/welcome") { // 由 SessionManager.requireAuthorize 跳转到
                WelcomeScene(rememberViewModel { WelcomeViewModel() }, Modifier.desktopTitleBarPadding().fillMaxSize())
            }
            scene("/home") {
                HomeScene()
            }
            scene("/bangumi-oauth") {
                BangumiOAuthScene(rememberViewModel { BangumiOAuthViewModel() }, Modifier.desktopTitleBarPadding())
            }
            scene("/bangumi-token-auth") {
                BangumiTokenAuthPage(
                    rememberViewModel { BangumiTokenAuthViewModel() },
                    Modifier.desktopTitleBarPadding().fillMaxSize(),
                )
            }
            scene("/subjects/{subjectId}") { backStackEntry ->
                val subjectId = backStackEntry.path<Int>("subjectId") ?: run {
                    navigator.goBack()
                    return@scene
                }
                val vm = rememberViewModel<SubjectDetailsViewModel> { SubjectDetailsViewModel(subjectId) }
                SideEffect { vm.navigator = aniNavigator }
                SubjectDetailsScene(vm, Modifier.desktopTitleBarPadding())
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
                val initialIsFullscreen = backStackEntry.query<Boolean>("fullscreen") ?: false
                val context = LocalContext.current
                val vm = rememberViewModel<EpisodeViewModel>(
                    listOf(subjectId, episodeId),
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
            scene("/settings") { backStackEntry ->
                val initialTab = backStackEntry.query<Int>("tab")
                    ?.let { SettingsTab.entries.getOrNull(it) }
                    ?: SettingsTab.MEDIA
                SettingsPage(
                    Modifier.desktopTitleBarPadding().fillMaxSize(),
                    initialTab = initialTab,
                    allowBack = backStackEntry.query("back") ?: false,
                )
            }
            scene("/caches") {
                CacheManagementPage(Modifier.desktopTitleBarPadding().fillMaxSize(), showBack = true)
            }
            scene("/subjects/{subjectId}/caches") { backStackEntry ->
                val subjectId = backStackEntry.path<Int>("subjectId") ?: run {
                    navigator.goBack()
                    return@scene
                }
                // Don't use rememberViewModel to save memory
                val vm = remember(subjectId) { SubjectCacheViewModelImpl(subjectId) }
                SubjectCacheScene(vm, Modifier.desktopTitleBarPadding())
            }
        }
    }
}
