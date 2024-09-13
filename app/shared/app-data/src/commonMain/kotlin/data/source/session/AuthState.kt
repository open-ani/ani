package me.him188.ani.app.data.source.session

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.tools.MonoTasker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class AuthState(
    state: State<SessionStatus?>,
    val launchAuthorize: (navigator: AniNavigator) -> Unit,
    private val retry: suspend () -> Unit,
    backgroundScope: CoroutineScope,
) {
    val status by state

    val isKnownLoggedIn: Boolean by derivedStateOf { this.status is SessionStatus.Verified }
    val isKnownGuest: Boolean by derivedStateOf { this.status is SessionStatus.Guest }
    val isLoading: Boolean by derivedStateOf { this.status == null || this.status is SessionStatus.Loading }

    /**
     * 任何未登录成功的情况, 如网络错误
     */
    val isKnownLoggedOut: Boolean by derivedStateOf { this.status is SessionStatus.VerificationFailed }

    val isKnownExpired: Boolean by derivedStateOf { this.status is SessionStatus.NoToken || this.status is SessionStatus.Expired }

    private val retryTasker = MonoTasker(backgroundScope)
    fun retry() {
        retryTasker.launch {
            retry.invoke()
        }
    }
}

fun <T> T.launchAuthorize(navigator: AniNavigator)
        where T : KoinComponent {
    val sessionManager: SessionManager by inject()
    sessionManager.requireAuthorizeAsync(
        onLaunch = {
            withContext(Dispatchers.Main) { navigator.navigateBangumiOAuthOrTokenAuth() }
        },
        skipOnGuest = false,
    )  //  use SessionManager's lifecycle
}
