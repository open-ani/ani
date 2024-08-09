package me.him188.ani.app.data.source.session

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import me.him188.ani.app.data.repository.AccessTokenSession
import me.him188.ani.app.data.repository.GuestSession
import kotlin.test.Test
import kotlin.test.assertEquals

class BangumiSessionManagerEventTest : AbstractBangumiSessionManagerTest() {
    /*
     * Note: 这些顺序对 MyCollectionsViewModel 的切换账号时自动清空缓存很重要 (虽然那个还没写 test)
     */

    @Test
    fun `setSession to Guest emits event`() = runTest {
        val manager = createManager(
            getSelfInfo = { noCall() },
            refreshAccessToken = { noCall() },
        )
        runCollectingEvents(manager) {
            setSession(GuestSession)
        }.run {
            assertEquals(1, size)
            assertEquals(SessionEvent.SwitchToGuest, get(0))
        }
    }

    @Test
    fun `setSession to AccessToken emits event`() = runTest {
        val manager = createManager(
            getSelfInfo = { noCall() },
            refreshAccessToken = { noCall() },
        )
        runCollectingEvents(manager) {
            setSession(AccessTokenSession(ACCESS_TOKEN, 1))
        }.run {
            assertEquals(1, size)
            assertEquals(SessionEvent.Login, get(0))
        }
    }

    @Test
    fun `clearSession emits LogOut when session is AccessToken`() = runTest {
        val manager = createManager(
            getSelfInfo = { noCall() },
            refreshAccessToken = { noCall() },
        )
        manager.setSession(AccessTokenSession(ACCESS_TOKEN, 1))
        runCollectingEvents(manager) {
            clearSession()
        }.run {
            assertEquals(1, size)
            assertEquals(SessionEvent.Logout, get(0))
        }
    }

    @Test
    fun `clearSession does not emit when session is Guest`() = runTest {
        val manager = createManager(
            getSelfInfo = { noCall() },
            refreshAccessToken = { noCall() },
        )
        manager.setSession(GuestSession)
        runCollectingEvents(manager) {
            clearSession()
        }.run {
            assertEquals(0, size)
        }
    }

    private suspend fun TestScope.runCollectingEvents(
        sessionManager: SessionManager,
        block: suspend SessionManager. () -> Unit
    ): List<SessionEvent> =
        coroutineScope {
            val res = mutableListOf<SessionEvent>()
            val job = launch(start = CoroutineStart.UNDISPATCHED) {
                sessionManager.events.collect {
                    res.add(it)
                }
            }
            block(sessionManager)
            runCoroutines()
            job.cancelAndJoin()
            return@coroutineScope res.toImmutableList()
        }
}