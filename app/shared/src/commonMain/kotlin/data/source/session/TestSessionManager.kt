package me.him188.ani.app.data.source.session

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.app.data.repository.Session
import me.him188.ani.app.navigation.AniNavigator
import kotlin.time.Duration.Companion.days

object TestSessionManager : SessionManager {
    private val savedSession: MutableStateFlow<Session?> = MutableStateFlow(
        Session(
            accessToken = "testToken",
            expiresAtMillis = System.currentTimeMillis() + 1.days.inWholeMilliseconds,
        ),
    )
    override val state: Flow<SessionState> =
        MutableStateFlow(SessionState.Verified("testToken", UserInfo.EMPTY))
    override val processingRequest: MutableStateFlow<ExternalOAuthRequest?> =
        MutableStateFlow(null)
    override val events: SharedFlow<SessionEvent> = MutableSharedFlow(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun requireAuthorize(
        navigator: AniNavigator,
        navigateToWelcome: Boolean,
        ignoreGuest: Boolean
    ) {
    }

    override fun requireOnlineAsync(navigator: AniNavigator, navigateToWelcome: Boolean, ignoreGuest: Boolean) {
    }

    override suspend fun setSession(session: Session) {
        savedSession.value = session
    }

    override suspend fun logout() {
        savedSession.value = null
    }
}