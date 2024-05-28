package me.him188.ani.app.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.pages.cache.manage.CacheManagementPage
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.profile.AuthViewModel
import me.him188.ani.app.ui.profile.auth.AuthRequestScene
import me.him188.ani.app.ui.settings.SettingsPage
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.subject.cache.SubjectCacheScene
import me.him188.ani.app.ui.subject.cache.SubjectCacheViewModel
import me.him188.ani.app.ui.subject.details.SubjectDetailsScene
import me.him188.ani.app.ui.subject.details.SubjectDetailsViewModel
import me.him188.ani.app.ui.subject.episode.EpisodeScene
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.path
import moe.tlaster.precompose.navigation.query
import moe.tlaster.precompose.viewmodel.viewModel

@Composable
fun AniAppContentPortrait(
    aniNavigator: AniNavigator,
    modifier: Modifier = Modifier
) {
    val navigator = aniNavigator.navigator
    CompositionLocalProvider(LocalNavigator provides aniNavigator) {
        NavHost(navigator, initialRoute = "/home", modifier = modifier) {
            scene("/home") {
                HomeScene()
            }
            scene("/auth") { backStackEntry ->
                val allowBack = backStackEntry.query<Boolean?>("allowBack") ?: false
                val authViewModel = rememberViewModel { AuthViewModel() }
                AuthRequestScene(authViewModel, allowBack)
            }
            scene("/subjects/{subjectId}") { backStackEntry ->
                val subjectId = backStackEntry.path<Int>("subjectId") ?: run {
                    navigator.goBack()
                    return@scene
                }
                val vm = viewModel<SubjectDetailsViewModel> { SubjectDetailsViewModel(subjectId) }
                SubjectDetailsScene(vm)
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
                    listOf(subjectId, episodeId)
                ) {
                    EpisodeViewModel(
                        initialSubjectId = subjectId,
                        initialEpisodeId = episodeId,
                        initialIsFullscreen = initialIsFullscreen,
                        context,
                    )
                }
                EpisodeScene(vm)
            }
            scene("/preferences") { backStackEntry ->
                val initialTab = backStackEntry.query<Int>("tab")
                    ?.let { SettingsTab.entries.getOrNull(it) }
                    ?: SettingsTab.MEDIA
                SettingsPage(
                    Modifier.fillMaxSize(),
                    initialTab = initialTab,
                )
            }
            scene("/caches") {
                CacheManagementPage(Modifier.fillMaxSize())
            }
            scene("/subjects/{subjectId}/caches") { backStackEntry ->
                val subjectId = backStackEntry.path<Int>("subjectId") ?: run {
                    navigator.goBack()
                    return@scene
                }
                // Don't use rememberViewModel to save memory
                val vm = remember(subjectId) { SubjectCacheViewModel(subjectId) }
                SubjectCacheScene(
                    vm,
                    onClickGlobalCacheSettings = {
                        aniNavigator.navigatePreferences(SettingsTab.MEDIA)
                    },
                    onClickGlobalCacheManage = {
                        aniNavigator.navigateCaches()
                    }
                )
            }
        }
    }
}
