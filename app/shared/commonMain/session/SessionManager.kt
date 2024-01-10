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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

interface SessionManager {
    @Stable
    val session: StateFlow<Session?>

    @Stable
    val username: StateFlow<String?>

    @Stable
    val isSessionValid: StateFlow<Boolean>

    @Stable
    val processingAuth: StateFlow<Boolean>

    suspend fun refreshSessionByCode(code: String)
    suspend fun refreshSessionByRefreshToken()

    /**
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

    override val isSessionValid: StateFlow<Boolean> = session.map { it != null }
        .stateIn(coroutineScope, SharingStarted.Eagerly, false)

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
        val accessToken = client.exchangeTokens(code)
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
            client.refreshAccessToken(refreshToken)
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
        if (isSessionValid.value) return
        if (refreshToken.value != null) {
            refreshSessionByRefreshToken()
            return
        }

        // require user authorization
        singleAuthLock.withLock {
            // check again
            if (isSessionValid.value) return
            if (refreshToken.value != null) {
                refreshSessionByRefreshToken()
                return
            }

            when (authorizationNavigator.authorize(context, optional)) {
                AuthorizationResult.SUCCESS -> {
                    if (isSessionValid.value) {
                        return
                    }
                    throw AuthorizationCanceledException()
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
