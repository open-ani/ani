package me.him188.ani.app.ui.foundation

import me.him188.ani.app.data.source.session.AuthState
import me.him188.ani.app.data.source.session.SessionManager
import me.him188.ani.app.data.source.session.launchAuthorize
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


fun <T> T.AuthState(): AuthState
        where T : KoinComponent, T : AbstractViewModel {
    val sessionManager: SessionManager by inject()
    return AuthState(
        state = sessionManager.state.produceState(null),
        launchAuthorize = { navigator ->
            launchAuthorize(navigator)
        },
        retry = { sessionManager.retry() },
        backgroundScope,
    )
}
