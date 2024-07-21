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

package me.him188.ani.app.session

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.repository.ProfileRepository
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.data.repository.TokenRepository
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.utils.coroutines.runUntilSuccess
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.trace
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.days

/**
 * Bangumi 授权状态管理器
 */
interface SessionManager {
    /**
     * 当前有效的会话. 优先使用 [username] 或 [isSessionValid].
     */
    @Stable
    val session: SharedFlow<Session?>

    /**
     * 当前有效会话的用户名. 当不为 `null` 时保证可以使用该用户名执行 API 请求.
     *
     * 若用户登录过, 但会话已经过期, 无论是否正在刷新会话, 此值都会是 `null`, 直到会话刷新成功.
     * 若用户未登录, 此值一直为 `null`.
     *
     * @see isSessionValid
     */
    @Stable
    val username: SharedFlow<String?>

    /**
     * 当前授权是否有效. `null` means not yet known, i.e. waiting for database query on start up.
     */
    @Stable
    val isSessionValid: Flow<Boolean?>

    /**
     * 当前正在进行中的授权请求.
     */
    @Stable
    val processingRequest: StateFlow<ExternalOAuthRequest?>

    /**
     * 请求为线上状态.
     *
     * 1. 若用户已经登录且会话有效 ([isSessionValid] 为 `true`), 则此函数会立即返回.
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
    @Throws(AuthorizationException::class)
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

object TestSessionManagers {
    val Online = object : SessionManager {
        override val session: MutableStateFlow<Session?> = MutableStateFlow(
            Session(
                accessToken = "testToken",
                expiresAt = System.currentTimeMillis() + 1.days.inWholeMilliseconds,
            ),
        )
        override val username: MutableStateFlow<String?> = MutableStateFlow("test")
        override val isSessionValid: Flow<Boolean?> = session.map { it != null }
        override val processingRequest: MutableStateFlow<ExternalOAuthRequest?> = MutableStateFlow(null)

        override suspend fun requireAuthorize(
            navigator: AniNavigator,
            navigateToWelcome: Boolean,
            ignoreGuest: Boolean
        ) {
        }

        override fun requireOnlineAsync(navigator: AniNavigator, navigateToWelcome: Boolean, ignoreGuest: Boolean) {
        }

        override suspend fun setSession(session: Session) {
            this.session.value = session
        }

        override suspend fun logout() {
            username.value = null
            session.value = null
        }
    }
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


internal class SessionManagerImpl : KoinComponent, SessionManager, HasBackgroundScope by BackgroundScope() {
    private val tokenRepository: TokenRepository by inject()
    private val profileRepository: ProfileRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val client: BangumiClient by inject()

    private val profileSettings = settingsRepository.profileSettings

    private val logger = logger(SessionManager::class)

    override val session: SharedFlow<Session?> =
        tokenRepository.session.distinctUntilChanged().shareInBackground(SharingStarted.Eagerly)

    override val username: SharedFlow<String?> =
        session
            .map {
                if (it == null || it.expiresAt <= System.currentTimeMillis()) {
                    null
                } else
                    runUntilSuccess(maxAttempts = Int.MAX_VALUE) { profileRepository.getSelfOrNull() }?.username
            }
            .distinctUntilChanged()
            .shareInBackground(SharingStarted.Eagerly)

    override val isSessionValid: Flow<Boolean?> =
        username.map { it != null }

    private val refreshTokenLoaded = CompletableDeferred<Boolean>()
    private val refreshToken = tokenRepository.refreshToken
        .transformLatest {
            emit(it)
            refreshTokenLoaded.complete(true)
        }
        .shareInBackground(SharingStarted.Eagerly)

    private val singleAuthLock = Mutex()
    override val processingRequest: MutableStateFlow<ExternalOAuthRequest?> = MutableStateFlow(null)

    private suspend fun tryRefreshSessionByRefreshToken(): Boolean {
        logger.trace { "tryRefreshSessionByRefreshToken: start" }
        val session = session.first() ?: return false.also {
            logger.trace { "tryRefreshSessionByRefreshToken: failed because session is empty" }
        }
        if (session.expiresAt > System.currentTimeMillis()) {
            logger.trace { "tryRefreshSessionByRefreshToken: success because session is already valid" }
            // session is valid
            return true
        }

        // session is invalid, refresh it
        val refreshToken = refreshToken.first() ?: return false.also {
            logger.trace { "tryRefreshSessionByRefreshToken: failed because refresh token is null" }
        }
        val newAccessToken = runCatching {
            withContext(Dispatchers.IO) {
                client.refreshAccessToken(
                    refreshToken,
                    BangumiAuthorizationConstants.CALLBACK_URL,
                )
            }
        }.getOrNull()
        if (newAccessToken == null) {
            logger.trace { "tryRefreshSessionByRefreshToken: failed because new token is null, refreshToken=$refreshToken" }
            return false
        }
        // success
        setSession(
//            session.userId,
            newAccessToken.accessToken,
            System.currentTimeMillis() + newAccessToken.expiresIn,
            newAccessToken.refreshToken,
        )
        logger.trace { "tryRefreshSessionByRefreshToken: success" }
        return true
    }

    override suspend fun requireAuthorize(navigator: AniNavigator, navigateToWelcome: Boolean, ignoreGuest: Boolean) {
        logger.trace { "requireOnline" }

        // fast path, already online
        if (isSessionValid.first() == true) return

        singleAuthLock.withLock {
            // not online, try to refresh
            refreshTokenLoaded.await()
            if (isSessionValid.first() == true) return // check again because this might have changed
            if (refreshToken.first() != null) {
                if (tryRefreshSessionByRefreshToken()) {
                    return
                }
            }

            // failed to refresh, possibly refresh token is invalid
            if (!ignoreGuest && profileSettings.flow.first().loginAsGuest) {
                throw AuthorizationCancelledException("以游客身份登录") // 以游客身份登录
            }

            // Launch external oauth (e.g. browser)
            val req = BangumiOAuthRequest(navigator, navigateToWelcome) { session, refreshToken ->
                setSession(session.accessToken, session.expiresAt, refreshToken)
            }
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
                processingRequest.value?.cancel()
                requireAuthorize(navigator, navigateToWelcome = navigateToWelcome, ignoreGuest)
                logger.info { "requireOnline: success" }
            } catch (e: AuthorizationException) {
                logger.error(e) { "Authorization failed" }
            }
        }
    }

    override suspend fun setSession(session: Session) {
        tokenRepository.setSession(session)
        profileSettings.update {
            copy(loginAsGuest = false)
        }
    }

    override suspend fun logout() {
        tokenRepository.clear()
    }

    private suspend fun setSession(accessToken: String, expiresAt: Long, refreshToken: String) {
        logger.info { "Bangumi session refreshed, new expiresAt=$expiresAt" }

        tokenRepository.setRefreshToken(refreshToken)
        tokenRepository.setSession(Session(accessToken, expiresAt))
        // session updates automatically
    }
}
