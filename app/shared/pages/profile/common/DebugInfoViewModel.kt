package me.him188.ani.app.ui.profile

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DebugInfoViewModel : AbstractViewModel(), KoinComponent {
    private val sessionManager: SessionManager by inject()
    val browserNavigator: BrowserNavigator by inject()

    val debugInfo = debugInfoFlow().shareInBackground(started = SharingStarted.Eagerly)

    private fun debugInfoFlow() = combine(
        sessionManager.session,
        sessionManager.processingRequest.flatMapLatest { it?.state ?: flowOf(null) },
        sessionManager.isSessionValid,
    ) { session, processingRequest, isSessionValid ->
        DebugInfo(properties = buildMap {
            val buildConfig = currentAniBuildConfig
            put("App Version", buildConfig.versionName)
            put("isDebug", buildConfig.isDebug.toString())
            if (buildConfig.isDebug) {
                put("accessToken", session?.accessToken)
            }
            put("processingRequest.state", processingRequest.toString())
            put("sessionManager.isSessionValid", isSessionValid.toString())
        })
    }

}