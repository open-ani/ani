package me.him188.ani.danmaku.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import me.him188.ani.danmaku.server.service.AuthService
import me.him188.ani.danmaku.server.util.exception.InvalidClientVersionException
import me.him188.ani.danmaku.server.util.exception.UnauthorizedException
import me.him188.ani.utils.coroutines.runUntilSuccess
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import java.io.File
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.fail

class AuthServiceTest {
    private val koin = object : KoinComponent {}

    @TempDir
    lateinit var tempDir: File

    private fun runTestWithKoin(block: suspend () -> Unit) = runTest {
        val coroutineScope = CoroutineScope(SupervisorJob())
        startKoin {
            val config = ServerConfigBuilder.create {
                testing = true
                rootDir = tempDir
                jwt {
                    issuer = "test_issuer"
                    audience = "test_audience"
                }
            }.build()
            modules(getServerKoinModule(config, coroutineScope))
        }
        block()
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `test login bangumi succeed`() = runTestWithKoin {
        val authService = koin.get<AuthService>()

        assertDoesNotThrow { authService.loginBangumi("test_token_1") }
    }

    @Test
    fun `test login bangumi failed`() = runTestWithKoin {
        val authService = koin.get<AuthService>()

        assertThrows<UnauthorizedException> { authService.loginBangumi("invalid_token") }
    }

    @Test
    fun `test login bangumi with client version`() = runTestWithKoin {
        val authService = koin.get<AuthService>()
        runUntilSuccess(maxAttempts = 5) {
            authService.loginBangumi("test_token_1", "3.0.0-beta21")
        }
        runUntilSuccess(maxAttempts = 5) {
            try {
                authService.loginBangumi("test_token_1", "bad_version")
            } catch (e: InvalidClientVersionException) {
                return@runUntilSuccess
            }
            fail("Should throw InvalidClientVersionException")
        }
    }
}
