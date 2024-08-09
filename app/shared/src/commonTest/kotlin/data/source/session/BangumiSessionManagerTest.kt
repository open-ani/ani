package me.him188.ani.app.data.source.session

import kotlinx.coroutines.flow.toList
import me.him188.ani.app.data.models.ApiFailure
import me.him188.ani.app.data.models.ApiResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.fail

class BangumiSessionManagerTest : AbstractBangumiSessionManagerTest() {

    ///////////////////////////////////////////////////////////////////////////
    // Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `preconditions - no sharing and replay in tests`() = runTest {
        val manager = createManager(
            getSelfInfo = { noCall() },
            refreshAccessToken = { noCall() },
        )
        setValidToken("testToken")
        assertEquals(SessionStatus.Verifying("testToken"), manager.awaitState())
        runCoroutines()
        assertEquals(SessionStatus.Verifying("testToken"), manager.awaitState()) // should rerun flow
    }

    ///////////////////////////////////////////////////////////////////////////
    // Token state
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `no token`() = runTest {
        val manager = createManager(
            getSelfInfo = { noCall() },
            refreshAccessToken = { noCall() },
        )
        assertEquals(SessionStatus.NoToken, manager.awaitState())
    }

    @Test
    fun `token expired`() = runTest {
        val manager = createManager(
            getSelfInfo = { noCall() },
            refreshAccessToken = { noCall() },
        )
        setExpiredToken()
        assertEquals(SessionStatus.Expired, manager.awaitState())
    }

    @Test
    fun `valid token needs to be verified`() = runTest {
        val manager = createManager(
            getSelfInfo = { ApiResponse.success(SUCCESS_USER_INFO) },
            refreshAccessToken = { noCall() },
        )
        setValidToken()
        assertEquals(SessionStatus.Verifying("testToken"), manager.awaitState())
        assertEquals(SessionStatus.Verified(ACCESS_TOKEN, SUCCESS_USER_INFO), manager.awaitState(1))
    }

    @Test
    fun `valid token - unauthorized verify - successful refresh - successful verify`() = runTest {
        val manager = createManager(
            getSelfInfo = {
                if (getSelfInfoCalled.value == 0) {
                    ApiResponse.failure(ApiFailure.Unauthorized)
                } else {
                    ApiResponse.success(SUCCESS_USER_INFO)
                }
            },
            refreshAccessToken = {
                ApiResponse.success(NewSession(ACCESS_TOKEN, Long.MAX_VALUE, REFRESH_TOKEN))
            },
        )
        setValidToken()
        refreshToken.value = REFRESH_TOKEN
        val states = manager.statePass.toList()
        assertEquals(SessionStatus.Verifying(ACCESS_TOKEN), states[0])
        assertEquals(SessionStatus.Refreshing, states[1])
        assertEquals(SessionStatus.Verifying(ACCESS_TOKEN), states[2])
        assertEquals(SessionStatus.Verified(ACCESS_TOKEN, SUCCESS_USER_INFO), states[3])
        assertEquals(2, getSelfInfoCalled.value)
        assertEquals(1, refreshAccessTokenCalled.value)
    }

    @Test
    fun `valid token - unauthorized verify - successful refresh - unauthorized verify`() = runTest {
        val manager = createManager(
            getSelfInfo = {
                when (getSelfInfoCalled.value) {
                    0, 1 -> ApiResponse.failure(ApiFailure.Unauthorized)
                    else -> fail()
                }
            },
            refreshAccessToken = {
                ApiResponse.success(NewSession(ACCESS_TOKEN, Long.MAX_VALUE, REFRESH_TOKEN))
            },
        )
        setValidToken()
        refreshToken.value = REFRESH_TOKEN
        val states = manager.statePass.toList()
        assertEquals(SessionStatus.Verifying(ACCESS_TOKEN), states[0])
        assertEquals(SessionStatus.Refreshing, states[1])
        assertEquals(SessionStatus.Verifying(ACCESS_TOKEN), states[2])
        assertEquals(SessionStatus.Expired, states[3])
        assertEquals(2, getSelfInfoCalled.value)
        assertEquals(1, refreshAccessTokenCalled.value)
    }

    @Test
    fun `valid token - unauthorized verify - successful refresh - NetworkError verify`() = runTest {
        val manager = createManager(
            getSelfInfo = {
                when (getSelfInfoCalled.value) {
                    0 -> ApiResponse.failure(ApiFailure.Unauthorized)
                    1 -> ApiResponse.failure(ApiFailure.NetworkError)
                    else -> fail()
                }
            },
            refreshAccessToken = {
                ApiResponse.success(NewSession(ACCESS_TOKEN, Long.MAX_VALUE, REFRESH_TOKEN))
            },
        )
        setValidToken()
        refreshToken.value = REFRESH_TOKEN
        val states = manager.statePass.toList()
        assertEquals(SessionStatus.Verifying(ACCESS_TOKEN), states[0])
        assertEquals(SessionStatus.Refreshing, states[1])
        assertEquals(SessionStatus.Verifying(ACCESS_TOKEN), states[2])
        assertEquals(SessionStatus.NetworkError, states[3])
        assertEquals(2, getSelfInfoCalled.value)
        assertEquals(1, refreshAccessTokenCalled.value)
    }

    @Test
    fun `valid token - unauthorized verify - unauthorized refresh`() = runTest {
        val manager = createManager(
            getSelfInfo = {
                ApiResponse.failure(ApiFailure.Unauthorized)
            },
            refreshAccessToken = {
                ApiResponse.failure(ApiFailure.Unauthorized)
            },
        )
        setValidToken()
        refreshToken.value = REFRESH_TOKEN
        val states = manager.statePass.toList()
        assertEquals(SessionStatus.Verifying(ACCESS_TOKEN), states[0])
        assertEquals(SessionStatus.Refreshing, states[1])
        assertEquals(SessionStatus.Expired, states[2])
        assertEquals(1, getSelfInfoCalled.value)
        assertEquals(1, refreshAccessTokenCalled.value)
    }

    @Test
    fun `valid token - unauthorized verify - NetworkError refresh`() = runTest {
        val manager = createManager(
            getSelfInfo = {
                ApiResponse.failure(ApiFailure.Unauthorized)
            },
            refreshAccessToken = {
                ApiResponse.failure(ApiFailure.NetworkError)
            },
        )
        setValidToken()
        refreshToken.value = REFRESH_TOKEN
        val states = manager.statePass.toList()
        assertEquals(SessionStatus.Verifying(ACCESS_TOKEN), states[0])
        assertEquals(SessionStatus.Refreshing, states[1])
        assertEquals(SessionStatus.NetworkError, states[2])
        assertEquals(1, getSelfInfoCalled.value)
        assertEquals(1, refreshAccessTokenCalled.value)
    }

    @Test
    fun `valid token - unauthorized verify - ServiceUnavailable refresh`() = runTest {
        val manager = createManager(
            getSelfInfo = {
                ApiResponse.failure(ApiFailure.Unauthorized)
            },
            refreshAccessToken = {
                ApiResponse.failure(ApiFailure.ServiceUnavailable)
            },
        )
        setValidToken()
        refreshToken.value = REFRESH_TOKEN
        val states = manager.statePass.toList()
        assertEquals(SessionStatus.Verifying(ACCESS_TOKEN), states[0])
        assertEquals(SessionStatus.Refreshing, states[1])
        assertEquals(SessionStatus.ServiceUnavailable, states[2])
        assertEquals(1, getSelfInfoCalled.value)
        assertEquals(1, refreshAccessTokenCalled.value)
    }

    @Test
    fun `valid token - NetworkError verify - no refresh`() = runTest {
        val manager = createManager(
            getSelfInfo = {
                ApiResponse.failure(ApiFailure.NetworkError)
            },
            refreshAccessToken = {
                ApiResponse.success(NewSession(ACCESS_TOKEN, Long.MAX_VALUE, REFRESH_TOKEN))
            },
        )
        setValidToken()
        assertEquals(SessionStatus.NetworkError, manager.awaitState(1))
        assertEquals(1, getSelfInfoCalled.value)
        assertEquals(0, refreshAccessTokenCalled.value)
    }

    @Test
    fun `valid token - ServiceUnavailable verify - no refresh`() = runTest {
        val manager = createManager(
            getSelfInfo = {
                ApiResponse.failure(ApiFailure.ServiceUnavailable)
            },
            refreshAccessToken = {
                ApiResponse.success(NewSession(ACCESS_TOKEN, Long.MAX_VALUE, REFRESH_TOKEN))
            },
        )
        setValidToken()
        assertEquals(SessionStatus.ServiceUnavailable, manager.awaitState(1))
        assertEquals(1, getSelfInfoCalled.value)
        assertEquals(0, refreshAccessTokenCalled.value)
    }


    ///////////////////////////////////////////////////////////////////////////
    // Wrap exceptions
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `rethrow exception in getSelfInfo`() = runTest {
        val myException = IndexOutOfBoundsException()
        val manager = createManager(
            getSelfInfo = { throw myException },
            refreshAccessToken = { noCall() },
        )
        setValidToken()
        assertFailsWith<IndexOutOfBoundsException> {
            manager.awaitState(1)
        }
    }

    @Test
    fun `rethrow exception in refreshAccessToken`() = runTest {
        val myException = IndexOutOfBoundsException()
        val manager = createManager(
            getSelfInfo = { noCall() },
            refreshAccessToken = { throw myException },
        )
        setExpiredToken()
        refreshToken.value = REFRESH_TOKEN
        assertFailsWith<IndexOutOfBoundsException> { // kotlinx-coroutines-debug 会重新构造 exception 所以不能 assertSame
            manager.awaitState(1)
        }
    }
}
