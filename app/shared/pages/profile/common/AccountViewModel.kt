package me.him188.ani.app.ui.profile

import androidx.annotation.UiThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.ProfileRepository
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.utils.coroutines.runUntilSuccess
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AccountViewModel : AbstractViewModel(), KoinComponent {
    private val sessionManager: SessionManager by inject()
    private val profileRepository: ProfileRepository by inject()

    val selfInfo = sessionManager.username
        .map {
            if (it == null) null else runUntilSuccess { profileRepository.getSelfOrNull() }
        }
        .stateInBackground(null)

    val isLoggedIn = sessionManager.isSessionValid

    val debugInfo = debugInfoFlow().shareInBackground(started = SharingStarted.Eagerly)

    private fun debugInfoFlow() = if (!currentAniBuildConfig.isDebug) {
        emptyFlow()
    } else combine(
        sessionManager.session,
        sessionManager.processingRequest.flatMapLatest { it?.state ?: flowOf(null) },
    ) { session, processingRequest ->

        DebugInfo(properties = buildMap {
            put("accessToken", session?.accessToken)
            put("processingRequest.state", processingRequest.toString())
        })
    }

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