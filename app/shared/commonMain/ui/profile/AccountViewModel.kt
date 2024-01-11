package me.him188.ani.app.ui.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.datasources.bangumi.BangumiClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AccountViewModel : AbstractViewModel(), KoinComponent {
    private val sessionManager: SessionManager by inject()
    private val client: BangumiClient by inject()

    val selfInfo = sessionManager.username
        .map {
            withContext(Dispatchers.IO) { client.api.getMyself() }
        }
        .stateInBackground(null)

    val debugInfo = debugInfoFlow().shareInBackground()

    private fun debugInfoFlow() = if (!currentAniBuildConfig.isDebug) {
        emptyFlow()
    } else combine(
        sessionManager.session
    ) { (session) ->

        DebugInfo(properties = buildMap {
            put("accessToken", session?.accessToken)
        })
    }
}

class DebugInfo(
    val properties: Map<String, String?>,
)