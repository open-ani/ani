package me.him188.ani.app.session

import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ViewModelAuthSupport


fun <T> T.requireOnline(navigator: AniNavigator)
        where T : KoinComponent,
              T : HasBackgroundScope,
              T : ViewModelAuthSupport {
    val sessionManager: SessionManager by inject()
    sessionManager.requireOnlineAsync(navigator)  //  use SessionManager's lifecycle
}