@file:Suppress("MemberVisibilityCanBePrivate")

package me.him188.ani.app.data.source.session

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.setMain
import me.him188.ani.app.data.models.ApiFailure
import me.him188.ani.app.data.models.ApiResponse
import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.app.data.repository.AccessTokenSession
import me.him188.ani.app.data.repository.TokenRepositoryImpl
import me.him188.ani.app.testFramework.mutablePreferencesOf
import me.him188.ani.app.tools.caching.MemoryDataStore
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

sealed class AbstractBangumiSessionManagerTest {
    internal companion object {
        internal const val ACCESS_TOKEN = "testToken"
        internal const val REFRESH_TOKEN = "refreshToken"
        internal val SUCCESS_USER_INFO = UserInfo.EMPTY
    }

    // default state:
    // - no session (access token)
    // - no refresh token

    internal val tokenRepository = TokenRepositoryImpl(MemoryDataStore(mutablePreferencesOf()))
    internal val refreshToken = MutableStateFlow<String?>(null)

    internal val getSelfInfoCalled = atomic(0)
    internal val refreshAccessTokenCalled = atomic(0)

    internal fun TestScope.createManager(
        getSelfInfo: suspend (accessToken: String) -> ApiResponse<UserInfo>,
        refreshAccessToken: suspend (refreshToken: String) -> ApiResponse<NewSession>,
        tokenRepository: TokenRepositoryImpl = this@AbstractBangumiSessionManagerTest.tokenRepository,
        refreshToken: Flow<String?> = this@AbstractBangumiSessionManagerTest.refreshToken,
        parentCoroutineContext: CoroutineContext = testScheduler,
    ) = BangumiSessionManager(
        tokenRepository = tokenRepository,
        refreshToken = refreshToken,
        getSelfInfo = {
            try {
                getSelfInfo(it)
            } finally {
                getSelfInfoCalled.incrementAndGet()
            }
        },
        refreshAccessToken = {
            try {
                refreshAccessToken(it)
            } finally {
                refreshAccessTokenCalled.incrementAndGet()
            }
        },
        parentCoroutineContext = parentCoroutineContext,
        enableSharing = false,
    )

    internal suspend fun BangumiSessionManager.awaitState(drop: Int = 0): SessionStatus {
        return statePass.drop(drop).first()
    }

    internal fun <T> noCall(): ApiResponse<T> {
        // 必须要返回一个, 因为 flow 实际上还会在跑一会
        return ApiResponse.failure(ApiFailure.Unauthorized)
    }

    internal suspend fun setExpiredToken() {
        tokenRepository.setSession(
            AccessTokenSession(
                accessToken = ACCESS_TOKEN,
                expiresAtMillis = 0, // expired
            ),
        )
    }

    internal suspend fun setValidToken(token: String = ACCESS_TOKEN) {
        tokenRepository.setSession(
            AccessTokenSession(
                accessToken = token,
                expiresAtMillis = Long.MAX_VALUE,
            ),
        )
    }

    internal fun TestScope.runCoroutines() {
        testScheduler.runCurrent()
    }

    fun runTest(
        context: CoroutineContext = EmptyCoroutineContext,
        testBody: suspend TestScope.() -> Unit
    ): TestResult {
        return kotlinx.coroutines.test.runTest(context) {
            Dispatchers.setMain(currentCoroutineContext()[CoroutineDispatcher]!!)
            testBody()
        }
    }
}