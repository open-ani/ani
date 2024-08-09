package me.him188.ani.app.data.source.session

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.app.data.repository.AccessTokenSession
import me.him188.ani.app.data.repository.Session
import me.him188.ani.utils.platform.currentTimeMillis
import kotlin.time.Duration.Companion.days

object PreviewSessionManager : SessionManager {
    private val savedSession: MutableStateFlow<Session?> = MutableStateFlow(
        AccessTokenSession(
            accessToken = "testToken",
            expiresAtMillis = currentTimeMillis() + 1.days.inWholeMilliseconds,
        ),
    )
    override val state: Flow<SessionStatus> =
        MutableStateFlow(SessionStatus.Verified("testToken", UserInfo.EMPTY))
    override val processingRequest: MutableStateFlow<ExternalOAuthRequest?> =
        MutableStateFlow(null)
    override val events: SharedFlow<SessionEvent> = MutableSharedFlow(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun requireAuthorize(onLaunch: suspend () -> Unit, skipOnGuest: Boolean) {
    }

    override fun requireAuthorizeAsync(onLaunch: suspend () -> Unit, skipOnGuest: Boolean) {
    }

    override suspend fun setSession(session: Session) {
        savedSession.value = session
    }

    override suspend fun retry() {
    }

    override suspend fun clearSession() {
        savedSession.value = null
    }
}