package me.him188.ani.app.navigation

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import me.him188.ani.app.session.SessionManager
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

interface AniNavigator {
    fun setNavigator(
        navigator: Navigator,
    )

    val navigator: Navigator

    fun navigateSubjectDetails(subjectId: Int) {
        navigator.navigate("/subjects/$subjectId")
    }

    fun navigateEpisodeDetails(subjectId: Int, episodeId: Int) {
        navigator.navigate("/subjects/$subjectId/episodes/$episodeId")
    }

    fun navigateHome() {
        navigator.navigate("/home")
    }

    suspend fun requestBangumiAuthorization(): AuthorizationResult
}

fun AniNavigator(): AniNavigator = AniNavigatorImpl()

class AniNavigatorImpl(
) : AniNavigator, KoinComponent {
    private val _navigator: CompletableDeferred<Navigator> = CompletableDeferred()

    override val navigator: Navigator
        get() = _navigator.getCompleted()

    private val sessionManager: SessionManager by inject()

    override fun setNavigator(navigator: Navigator) {
        this._navigator.complete(navigator)
    }

    override suspend fun requestBangumiAuthorization(): AuthorizationResult {
        if (sessionManager.isSessionValid.value == true) {
            return AuthorizationResult.SUCCESS
        }


        delay(2.seconds)

        return _navigator.await().navigateForResult("/auth", NavOptions(launchSingleTop = true)) as AuthorizationResult
    }
}

val LocalNavigator = compositionLocalOf<AniNavigator> {
    error("Navigator not found")
}
