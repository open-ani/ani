/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.app.data.source.session

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.models.ApiFailure
import me.him188.ani.app.data.models.ApiResponse
import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.app.data.models.fold
import me.him188.ani.app.data.models.map
import me.him188.ani.app.data.models.preference.ProfileSettings
import me.him188.ani.app.data.models.runApiRequest
import me.him188.ani.app.data.repository.ProfileRepository
import me.him188.ani.app.data.repository.Session
import me.him188.ani.app.data.repository.Settings
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.data.repository.TokenRepository
import me.him188.ani.app.data.repository.isExpired
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.trace
import me.him188.ani.utils.platform.currentTimeMillis
import org.koin.core.Koin
import kotlin.coroutines.CoroutineContext

/**
 * Bangumi 授权状态管理器
 */
interface SessionManager {
    val state: Flow<SessionState>

    /**
     * 当前正在进行中的授权请求.
     */
    val processingRequest: StateFlow<ExternalOAuthRequest?>

    /**
     * 登录/退出登录事件流. 只有当用户主动操作 (例如点击按钮) 时才会广播事件. 刚启动 app 时的自动登录不会触发事件.
     */
    val events: SharedFlow<SessionEvent>

    /**
     * 请求为线上状态.
     *
     * 1. 若用户已经登录且会话有效 ([isSessionVerified] 为 `true`), 则此函数会立即返回.
     * 2. 若用户登录过, 但是当前会话已经过期, 则此函数会尝试使用 refresh token 刷新会话.
     *    - 若刷新成功, 此函数会返回, 不会弹出任何 UI 提示.
     *    - 若刷新失败, 则进行下一步.
     * 3. 若用户不是第一次启动, 而且他曾经选择了以游客身份登录, 则抛出异常 [AuthorizationCancelledException].
     * 4. 取决于 [navigateToWelcome], 通过 [AniNavigator] 跳转到欢迎页或者登录页面, 并等待用户的登录结果.
     *    - 若用户取消登录 (选择游客), 此函数会抛出 [AuthorizationCancelledException].
     *    - 若用户成功登录, 此函数正常返回
     *
     * ## Cancellation Support
     *
     * 此函数支持 coroutine cancellation. 当 coroutine 被取消时, 此函数会中断授权请求并抛出 [CancellationException].
     */
    @Throws(AuthorizationException::class, kotlin.coroutines.cancellation.CancellationException::class)
    suspend fun requireAuthorize(
        navigator: AniNavigator,
        navigateToWelcome: Boolean,
        ignoreGuest: Boolean = false,
    )

    fun requireOnlineAsync(
        navigator: AniNavigator,
        navigateToWelcome: Boolean,
        ignoreGuest: Boolean = false,
    )

    suspend fun setSession(session: Session)

    suspend fun logout()
}

/**
 * `false` 并不一定代表未登录, 也可能是网络错误
 */
val SessionManager.isSessionVerified get() = state.map { it is SessionState.Verified }
val SessionManager.unverifiedAccessToken get() = state.map { it.unverifiedAccessToken }
val SessionManager.userInfo get() = state.map { it.userInfo }
val SessionManager.username get() = state.map { it.username }

val SessionManager.verifiedAccessToken: Flow<String?>
    get() = state.map {
        (it as? SessionState.Verified)?.accessToken
    }

/**
 * 当用户希望以游客身份登录时抛出的异常.
 */
class AuthorizationCancelledException(
    override val message: String?,
    override val cause: Throwable? = null
) : AuthorizationException()

class AuthorizationFailedException(
    override val cause: Throwable? = null
) : AuthorizationException()

sealed class AuthorizationException : Exception()


fun SessionManager(
    koin: Koin,
    parentCoroutineContext: CoroutineContext,
): SessionManager {
    val tokenRepository: TokenRepository by koin.inject()
    val profileRepository: ProfileRepository by koin.inject()
    val settingsRepository: SettingsRepository by koin.inject()
    val client: BangumiClient by koin.inject()

    return BangumiSessionManager(
        tokenRepository,
        refreshToken = tokenRepository.refreshToken,
        profileSettings = settingsRepository.profileSettings,
        getSelfInfo = { accessToken ->
            profileRepository.getSelfUserInfo(accessToken)
        },
        refreshAccessToken = { refreshToken ->
            runApiRequest {
                client.refreshAccessToken(refreshToken).let {
                    NewSession(it.accessToken, it.expiresIn * 1000L + currentTimeMillis(), it.refreshToken)
                }
            }
        },
        parentCoroutineContext,
        SharingStarted.WhileSubscribed(5000),
    )
}

internal class NewSession(
    val accessToken: String,
    val expiresAtMillis: Long,
    val refreshToken: String,
)

internal class BangumiSessionManager(
    private val tokenRepository: TokenRepository,
    private val refreshToken: Flow<String?>,
    private val profileSettings: Settings<ProfileSettings>,
    private val getSelfInfo: suspend (accessToken: String) -> ApiResponse<UserInfo>,
    private val refreshAccessToken: suspend (refreshToken: String) -> ApiResponse<NewSession>,
    parentCoroutineContext: CoroutineContext,
    sharingStarted: SharingStarted,
) : SessionManager, HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    private val logger = logger(SessionManager::class)

    private val refreshCounter = MutableStateFlow(0)
    override val state: Flow<SessionState> = refreshCounter.flatMapLatest { _ ->
        tokenRepository.session.transformLatest { session ->
            if (session == null) {
                emit(SessionState.NoToken)
                return@transformLatest
            }
            if (session.isExpired()) {
                emit(SessionState.Expired)
                return@transformLatest
            }

            try {
                emit(
                    doAuth(session.accessToken).let { state ->
                        if (state == SessionState.Expired) {
                            tryRefreshSessionByRefreshToken().fold(
                                onSuccess = { accessToken ->
                                    if (accessToken == null) { // no refresh token
                                        SessionState.Expired
                                    } else {
                                        doAuth(accessToken)
                                    }
                                },
                                onKnownFailure = { failure ->
                                    failure.toSessionState()
                                },
                            )
                        } else {
                            state
                        }
                    },
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                emit(SessionState.Exception(e))
            }
        }
    }.shareInBackground(sharingStarted)

    private suspend fun doAuth(accessToken: String): SessionState {
        return getSelfInfo(accessToken).fold(
            onSuccess = { value ->
                SessionState.Verified(accessToken, value)
            },
            onKnownFailure = { failure ->
                failure.toSessionState()
            },
        )
    }

    private fun ApiFailure.toSessionState() = when (this) {
        ApiFailure.NetworkError -> SessionState.NetworkError
        ApiFailure.ServiceUnavailable -> SessionState.NetworkError
        ApiFailure.Unauthorized -> SessionState.Expired
    }

    private val singleAuthLock = Mutex()
    override val processingRequest: MutableStateFlow<ExternalOAuthRequest?> = MutableStateFlow(null)
    override val events: MutableSharedFlow<SessionEvent> = MutableSharedFlow(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private suspend fun tryRefreshSessionByRefreshToken(): ApiResponse<String?> {
        logger.trace { "tryRefreshSessionByRefreshToken: start" }
        // session is invalid, refresh it
        val refreshToken = refreshToken.first() ?: return ApiResponse.success(null).also {
            logger.trace { "tryRefreshSessionByRefreshToken: failed because refresh token is null" }
        }
        val newAccessToken = refreshAccessToken(refreshToken)
        return newAccessToken.map { session ->
            setSessionAndRefreshToken(
//            session.userId,
                session,
                isNewLogin = false,
            )

            session.accessToken
        }
    }

    override suspend fun requireAuthorize(navigator: AniNavigator, navigateToWelcome: Boolean, ignoreGuest: Boolean) {
        logger.trace { "requireOnline" }

        // fast path, already online
        if (isSessionVerified.first()) return

        singleAuthLock.withLock {
            // not online, try to refresh
            if (isSessionVerified.first()) return // check again because this might have changed

            // failed to refresh, possibly refresh token is invalid
            if (!ignoreGuest && profileSettings.flow.first().loginAsGuest) {
                throw AuthorizationCancelledException("Login as guest") // 以游客身份登录
            }

            // Launch external oauth (e.g. browser)
            val req = BangumiOAuthRequest(
                onLaunch = {
                    withContext(Dispatchers.Main) {
                        if (navigateToWelcome) {
                            navigator.navigateWelcome()
                        } else {
                            navigator.navigateBangumiOAuthOrTokenAuth()
                        }
                    }
                },
                onSuccess = { session ->
                    setSessionAndRefreshToken(session, isNewLogin = true)
                },
            )
            processingRequest.value = req
            try {
                req.invoke()
            } finally {
                processingRequest.value = null
            }

            // Throw exceptions according to state
            val state = req.state.value
            check(state is ExternalOAuthRequest.State.Result)
            when (state) {
                is ExternalOAuthRequest.State.Cancelled -> {
                    profileSettings.update {
                        copy(loginAsGuest = true)
                    }
                    throw AuthorizationCancelledException(null, state.cause)
                }

                is ExternalOAuthRequest.State.Failed -> {
                    throw AuthorizationFailedException(state.throwable)
                }

                ExternalOAuthRequest.State.Success -> {
                    // nop
                }
            }
        }
    }

    override fun requireOnlineAsync(navigator: AniNavigator, navigateToWelcome: Boolean, ignoreGuest: Boolean) {
        launchInBackground {
            try {
                requireAuthorize(navigator, navigateToWelcome = navigateToWelcome, ignoreGuest)
                logger.info { "requireOnline: success" }
            } catch (e: AuthorizationException) {
                logger.error(e) { "Authorization failed" }
            }
        }
    }

    private suspend fun setSessionAndRefreshToken(
        newSession: NewSession,
        isNewLogin: Boolean
    ) {
        logger.info { "Bangumi session refreshed, new expiresAtMillis=${newSession.expiresAtMillis}" }

        tokenRepository.setRefreshToken(newSession.refreshToken)
        setSessionImpl(Session(newSession.accessToken, newSession.expiresAtMillis))
        if (isNewLogin) {
            events.tryEmit(SessionEvent.Login)
        } else {
            events.tryEmit(SessionEvent.TokenRefreshed)
        }
    }

    override suspend fun setSession(session: Session) {
        setSessionImpl(session)
        events.tryEmit(SessionEvent.Login)
    }

    private suspend fun setSessionImpl(session: Session) {
        tokenRepository.setSession(session)
        profileSettings.update {
            copy(loginAsGuest = false)
        }
    }

    override suspend fun logout() {
        tokenRepository.clear()
        events.tryEmit(SessionEvent.Logout)
    }
}
