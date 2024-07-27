package me.him188.ani.app.ui.profile

import androidx.annotation.UiThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import me.him188.ani.app.data.repository.ProfileRepository
import me.him188.ani.app.data.source.session.AuthState
import me.him188.ani.app.data.source.session.SessionManager
import me.him188.ani.app.data.source.session.userInfo
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// TODO: review and maybe refactor AccountViewModel
class AccountViewModel : AbstractViewModel(), KoinComponent {
    private val sessionManager: SessionManager by inject()
    private val profileRepository: ProfileRepository by inject()

    val selfInfo by sessionManager.userInfo.produceState(null)

    var logoutEnabled by mutableStateOf(true)
        private set

    val authState = AuthState()

    @UiThread
    fun logout() {
        logoutEnabled = false
        launchInBackground {
            sessionManager.logout()
        }
        logoutEnabled = true
    }
}

class DebugInfo(
    val properties: Map<String, String?>,
)