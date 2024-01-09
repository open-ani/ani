package me.him188.ani.app.ui.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.datasources.bangumi.BangumiClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AccountViewModel : AbstractViewModel(), KoinComponent {
    private val sessionManager: SessionManager by inject()
    private val client: BangumiClient by inject()

    val selfInfo = sessionManager.userId
        .map { withContext(Dispatchers.IO) { client.api.getMyself() } }
        .stateInBackground(null)

}