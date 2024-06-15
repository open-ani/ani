package me.him188.ani.app.ui.profile

import androidx.annotation.UiThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.map
import me.him188.ani.app.ViewModelAuthSupport
import me.him188.ani.app.data.repositories.ProfileRepository
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.utils.coroutines.runUntilSuccess
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// TODO: review and maybe refactor AccountViewModel
class AccountViewModel : AbstractViewModel(), KoinComponent, ViewModelAuthSupport {
    private val sessionManager: SessionManager by inject()
    private val profileRepository: ProfileRepository by inject()

    val selfInfo = sessionManager.username
        .map {
            if (it == null) null else runUntilSuccess(maxAttempts = Int.MAX_VALUE) { profileRepository.getSelfOrNull() }
        }
        .stateInBackground(null)

    var logoutEnabled by mutableStateOf(true)
        private set

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