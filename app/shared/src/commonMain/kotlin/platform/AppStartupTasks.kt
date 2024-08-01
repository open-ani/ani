package me.him188.ani.app.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.source.session.SessionManager
import me.him188.ani.app.navigation.AniNavigator

object AppStartupTasks {
    suspend fun verifySession(
        sessionManager: SessionManager,
        navigator: AniNavigator,
    ) {
        try {
            sessionManager.requireAuthorize(
                onLaunch = {
                    withContext(Dispatchers.Main) {
                        navigator.navigateWelcome()
                    }
                },
                skipOnGuest = true,
            )
        } catch (e: Throwable) {
            throw IllegalStateException("Failed to automatically log in on startup, see cause", e)
        }
    }
}