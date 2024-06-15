package me.him188.ani.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import me.him188.ani.app.ViewModelAuthSupport
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.rememberViewModel
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private class LoginViewModel : AbstractViewModel(), KoinComponent, ViewModelAuthSupport {
    private val sessionManager: SessionManager by inject()

    @Stable
    val isLoggedIn = sessionManager.isSessionValid.filterNotNull()
        .shareInBackground(started = SharingStarted.Eagerly)
}

/**
 * Check if the user is logged in. `null` means not yet known, i.e. waiting for database query on start up, or is refreshing a token.
 */
@Composable
fun isLoggedIn(): State<Boolean?> {
    val vm = rememberViewModel { LoginViewModel() }
    return vm.isLoggedIn.collectAsStateWithLifecycle(null)
}
