package me.him188.ani.app.session

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.ui.foundation.AbstractViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class AuthState(
    isLoggedIn: State<Boolean?>,
    val launchAuthorize: (navigator: AniNavigator) -> Unit,
) {
    private val isLoggedIn by isLoggedIn
    val isKnownLoggedIn: Boolean get() = this.isLoggedIn == true
    val isKnownLoggedOut: Boolean get() = this.isLoggedIn == false
}

fun <T> T.AuthState(): AuthState
        where T : KoinComponent, T : AbstractViewModel {
    val sessionManager: SessionManager by inject()
    return AuthState(
        isLoggedIn = sessionManager.isSessionVerified.produceState(null),
        launchAuthorize = { navigator ->
            sessionManager.requireOnlineAsync(
                navigator,
                navigateToWelcome = false,
                ignoreGuest = true,
            )
        },
    )
}

fun <T> T.launchAuthorize(navigator: AniNavigator)
        where T : KoinComponent {
    val sessionManager: SessionManager by inject()
    sessionManager.requireOnlineAsync(
        navigator,
        navigateToWelcome = false,
        ignoreGuest = true,
    )  //  use SessionManager's lifecycle
}
