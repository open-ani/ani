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
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.TokenRepository
import me.him188.ani.app.navigation.AuthorizationNavigator
import me.him188.ani.app.navigation.AuthorizationNavigator.AuthorizationResult
import me.him188.ani.app.platform.Context
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.utils.coroutines.ReentrantMutex
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.infrastructure.ApiClient

/**
 * 授权状态管理器
 */
interface SessionManager {
    /**
     * Currently active session.
     */
    @Stable
    val session: StateFlow<Session?>

    /**
     * Current user name. `null` means user is not logged in.
     */
    @Stable
    val username: StateFlow<String?>

    /**
     * 当前授权是否有效. `null` means not yet known, i.e. waiting for database query on start up.
     */
    @Stable
    val isSessionValid: StateFlow<Boolean?>

    /**
     * 当前授权是否正在进行中
     */
    @Stable
    val processingAuth: StateFlow<Boolean>

    @Throws(IOException::class)
    suspend fun refreshSessionByCode(code: String)

    @Throws(IOException::class)
    suspend fun refreshSessionByRefreshToken()

    /**
     * 请求登录.
     * 若当前未登录 (即 [isSessionValid] 为 `false`), 将会跳转到登录页面, 并等待用户的登录结果. 若当前已经登录, 则此函数会立即返回.
     *
     * 若 [optional] 为 `false`, 则 UI 不会显示返回键, 此函数会一直挂起直到用户登录成功. 当此函数返回时, [isSessionValid] 一定为 `true`.
     *
     * 若 [optional] 为 `true`, 则 UI 会显示返回键, 用户可以选择不登录. 当此函数返回时, [isSessionValid] 仍然可能是 `false`.
     *
     * @throws AuthorizationCanceledException if user cancels the authorization.
     */
    @Throws(AuthorizationCanceledException::class)
    suspend fun requireAuthorization(context: Context, optional: Boolean)
}

class AuthorizationCanceledException : Exception()


internal class SessionManagerImpl(
    coroutineScope: CoroutineScope,
) : KoinComponent, SessionManager {
    private val tokenRepository: TokenRepository by inject()
    private val authorizationNavigator: AuthorizationNavigator by inject()
    private val client: BangumiClient by inject()

    private val logger = logger(SessionManager::class)

    override val session: StateFlow<Session?> =
        tokenRepository.session.distinctUntilChanged().onEach {
            ApiClient.accessToken = it?.accessToken
        }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val username: StateFlow<String?> =
        session.filterNotNull()
            .map {
                runInterruptible(Dispatchers.IO) { client.api.getMyself() }.username
            }
            .distinctUntilChanged()
            .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    override val isSessionValid: StateFlow<Boolean?> = session.map { it != null }
        .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    private val refreshToken = tokenRepository.refreshToken.stateIn(coroutineScope, SharingStarted.Eagerly, null)

    private val singleAuthLock = Mutex()
    private val mutex = ReentrantMutex()
    override val processingAuth: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private inline fun <R> processAuth(block: () -> R) {
        processingAuth.value = true
        try {
            block()
        } finally {
            processingAuth.value = false
        }
    }

    override suspend fun refreshSessionByCode(code: String) {
        val accessToken = client.exchangeTokens(code, callbackUrl = authorizationNavigator.authorizationCallbackUrl)
        setSession(
            accessToken.userId,
            accessToken.accessToken,
            System.currentTimeMillis() + accessToken.expiresIn,
            accessToken.refreshToken
        )
    }

    override suspend fun refreshSessionByRefreshToken() = processAuth {
        val session = session.replayCache.firstOrNull() ?: return@processAuth
        if (session.expiresIn > System.currentTimeMillis()) {
            // session is valid
            return@processAuth
        }

        // session is invalid, refresh it
        val refreshToken = refreshToken.value ?: return@processAuth
        val newAccessToken = runCatching {
            client.refreshAccessToken(refreshToken, authorizationNavigator.authorizationCallbackUrl)
        }.getOrNull()
        if (newAccessToken == null) {
            logger.info { "Bangumi session refresh failed, refreshToken=$refreshToken" }
            return@processAuth
        }
        // success
        setSession(
            session.userId,
            newAccessToken.accessToken,
            System.currentTimeMillis() + newAccessToken.expiresIn,
            refreshToken
        )
    }

    override suspend fun requireAuthorization(context: Context, optional: Boolean) {
        // fast path
        if (isSessionValid.value == true) return
        if (refreshToken.value != null) {
            withContext(Dispatchers.IO) { refreshSessionByRefreshToken() }
            return
        }

        // require user authorization
        singleAuthLock.withLock {
            // check again
            if (isSessionValid.filterNotNull().first()) return // 等待数据库请求完成, 如果已经登录则不再请求授权
            if (refreshToken.value != null) {
                withContext(Dispatchers.IO) { refreshSessionByRefreshToken() }
                return
            }

            when (authorizationNavigator.navigateToAuthorization(context, optional)) {
                AuthorizationResult.SUCCESS -> {
                    if (isSessionValid.value == true) {
                        return
                    }
                }

                AuthorizationResult.CANCELLED -> {
                    throw AuthorizationCanceledException()
                }
            }
        }
    }

    private suspend fun setSession(userId: Long, accessToken: String, expiresAt: Long, refreshToken: String) {
        logger.info { "Bangumi session refreshed, userId=${userId}, new expiresAt=$expiresAt" }

        tokenRepository.setRefreshToken(refreshToken)
        tokenRepository.setSession(Session(userId, accessToken, expiresAt))
        // session updates automatically
    }
}
