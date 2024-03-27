package me.him188.ani.app.ui.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.profile.AuthViewModel
import me.him188.ani.app.ui.profile.auth.AuthRequestScene
import me.him188.ani.app.ui.subject.details.SubjectDetailsScene
import me.him188.ani.app.ui.subject.details.SubjectDetailsViewModel
import me.him188.ani.app.ui.subject.episode.EpisodeScene
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel
import moe.tlaster.precompose.navigation.NavHost
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
        NavHost(navigator, initialRoute = "/home") {
            scene("/home") {
                HomeScene()
            }
            scene("/auth") { backStackEntry ->
                val allowBack = backStackEntry.query<Boolean?>("allowBack") ?: false
                val authViewModel = remember { AuthViewModel() }
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
                val initialIsFullscreen = backStackEntry.query<Boolean>("fullscreen") ?: false
                val context = LocalContext.current
                val vm = rememberViewModel<EpisodeViewModel> {
                    EpisodeViewModel(
                        initialSubjectId = subjectId,
                        initialEpisodeId = episodeId,
                        initialIsFullscreen = initialIsFullscreen,
                        context,
                    )
                }
                // TODO: 当切换到全屏时, 会整个 recompose, 导致这里会重新 evaluate, 但是 path 里 fullscreen 参数没有变, 
                //  如果 vm.setFullscreen(initialIsFullscreen), 就会覆盖掉用户的操作, 导致全屏状态时 vm.isFullscreen 为 false
                //            vm.setFullscreen(initialIsFullscreen)
                SideEffect {
                    vm.setSubjectId(subjectId)
                    vm.setEpisodeId(episodeId)
                }
                EpisodeScene(vm)
            }
        }
        SideEffect {
            aniNavigator.setNavigator(navigator)
        }
    }
}
