package me.him188.ani.danmaku.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import me.him188.ani.danmaku.server.data.UserRepository
import me.him188.ani.danmaku.server.service.AuthService
import me.him188.ani.danmaku.server.service.ClientVersionVerifier
import me.him188.ani.danmaku.server.util.exception.InvalidClientVersionException
import me.him188.ani.danmaku.server.util.exception.UnauthorizedException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File
import kotlin.test.Test

class AuthServiceTest {
    private val koin = object : KoinComponent {}

    @TempDir
    lateinit var tempDir: File

    private fun runTestWithKoin(extraKoin: Module.() -> Unit = {}, block: suspend () -> Unit) = runTest {
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
            modules(
                getServerKoinModule(config, coroutineScope),
                module { extraKoin() },
            )
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
    fun `test login bangumi with client version`() = runTestWithKoin(
        {
            single<ClientVersionVerifier> {
                object : ClientVersionVerifier {
                    override suspend fun verify(clientVersion: String): Boolean {
                        return clientVersion == "3.0.0-dev" || clientVersion == "3.0.0-beta21"
                    }
                }
            }

        },
    ) {
        val authService = koin.get<AuthService>()

        assertDoesNotThrow { authService.loginBangumi("test_token_1", "3.0.0-dev") }
        assertDoesNotThrow { authService.loginBangumi("test_token_1", "3.0.0-beta21") }
        assertThrows<InvalidClientVersionException> { authService.loginBangumi("test_token_1", "bad_version") }
    }

    @Test
    fun `test login bangumi with client platform`() = runTestWithKoin {
        val authService = koin.get<AuthService>()
        val userRepository = koin.get<UserRepository>()

        val userId = authService.loginBangumi("test_token_1", "3.0.0-dev", "android-aarch64")
        assertEquals(setOf("android-aarch64"), userRepository.getUserById(userId)?.clientPlatforms)

        authService.loginBangumi("test_token_1", "3.0.0-dev", "macos-x86_64")
        assertEquals(setOf("android-aarch64", "macos-x86_64"), userRepository.getUserById(userId)?.clientPlatforms)

        authService.loginBangumi("test_token_1", "3.0.0-dev", "android-aarch64")
        assertEquals(setOf("android-aarch64", "macos-x86_64"), userRepository.getUserById(userId)?.clientPlatforms)
    }
}
