package me.him188.ani.app.navigation

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.CompletableDeferred
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import org.koin.core.component.KoinComponent

interface AniNavigator {
    fun setNavigator(
        navigator: Navigator,
    )

    val navigator: Navigator

    fun navigateSubjectDetails(subjectId: Int) {
        navigator.navigate("/subjects/$subjectId")
    }

    fun navigateEpisodeDetails(subjectId: Int, episodeId: Int, fullscreen: Boolean = false) {
        navigator.navigate(
            "/subjects/$subjectId/episodes/$episodeId?fullscreen=$fullscreen",
            NavOptions(launchSingleTop = true, includePath = true)
        )
    }

    fun navigateHome() {
        navigator.navigate("/home")
    }

    /**
     * 登录页面
     */
    fun navigateAuth() {
        navigator.navigate("/auth", NavOptions(launchSingleTop = true))
    }
}

fun AniNavigator(): AniNavigator = AniNavigatorImpl()

class AniNavigatorImpl(
) : AniNavigator, KoinComponent {
    private val _navigator: CompletableDeferred<Navigator> = CompletableDeferred()

    override val navigator: Navigator
        get() = _navigator.getCompleted()

    override fun setNavigator(navigator: Navigator) {
        this._navigator.complete(navigator)
    }
}

val LocalNavigator = compositionLocalOf<AniNavigator> {
    error("Navigator not found")
}
