package me.him188.ani.danmaku.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import me.him188.ani.danmaku.server.service.AuthService
import me.him188.ani.danmaku.server.service.AvatarSize
import me.him188.ani.danmaku.server.service.JwtTokenManager
import me.him188.ani.danmaku.server.service.UserService
import org.junit.jupiter.api.AfterEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.Test

class AniUserServiceTest {
    private val koin = object : KoinComponent {}

    private fun runTestWithKoin(block: suspend () -> Unit) = runTest {
        val coroutineScope = CoroutineScope(SupervisorJob())
        startKoin {
            modules(
                getServerKoinModule(
                    EnvironmentVariables(
                        testing = true,
                        jwtIssuer = "test_issuer",
                        jwtAudience = "test_audience",
                    ), coroutineScope
                )
            )
        }
        block()
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `test get bangumi id`() = runTestWithKoin {
        val userService = koin.get<UserService>()
        val authService = koin.get<AuthService>()
        
        val userId = authService.loginBangumi("test_token_1")
        val bangumiId = userService.getBangumiId(userId)
        assert(bangumiId == 1)
        
        val userId2 = authService.loginBangumi("test_token_2")
        val bangumiId2 = userService.getBangumiId(userId2)
        assert(bangumiId2 == 2)
    }
    
    @Test
    fun `test get nickname`() = runTestWithKoin {
        val userService = koin.get<UserService>()
        val authService = koin.get<AuthService>()
        
        val userId = authService.loginBangumi("test_token_2")
        val nickname = userService.getNickname(userId)
        assert(nickname == "test2")
        
        val userId2 = authService.loginBangumi("test_token_3")
        val nickname2 = userService.getNickname(userId2)
        assert(nickname2 == "test3")
    }
    
    @Test
    fun `test get avatar`() = runTestWithKoin {
        val userService = koin.get<UserService>()
        val authService = koin.get<AuthService>()
        
        val userId = authService.loginBangumi("test_token_3")
        val smallAvatar = userService.getAvatar(userId, AvatarSize.SMALL)
        val mediumAvatar = userService.getAvatar(userId, AvatarSize.MEDIUM)
        val largeAvatar = userService.getAvatar(userId, AvatarSize.LARGE)
        assert(smallAvatar == "small3")
        assert(mediumAvatar == "medium3")
        assert(largeAvatar == "large3")
    }
}