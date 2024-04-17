package me.him188.ani.danmaku.server

import kotlinx.coroutines.CoroutineScope
import me.him188.ani.danmaku.server.data.DanmakuRepository
import me.him188.ani.danmaku.server.data.InMemoryDanmakuRepositoryImpl
import me.him188.ani.danmaku.server.data.InMemoryUserRepositoryImpl
import me.him188.ani.danmaku.server.data.UserRepository
import me.him188.ani.danmaku.server.data.mongodb.MongoCollectionProvider
import me.him188.ani.danmaku.server.data.mongodb.MongoCollectionProviderImpl
import me.him188.ani.danmaku.server.data.mongodb.MongoDanmakuRepositoryImpl
import me.him188.ani.danmaku.server.data.mongodb.MongoUserRepositoryImpl
import me.him188.ani.danmaku.server.service.AuthService
import me.him188.ani.danmaku.server.service.AuthServiceImpl
import me.him188.ani.danmaku.server.service.BangumiLoginHelper
import me.him188.ani.danmaku.server.service.BangumiLoginHelperImpl
import me.him188.ani.danmaku.server.service.DanmakuService
import me.him188.ani.danmaku.server.service.DanmakuServiceImpl
import me.him188.ani.danmaku.server.service.JwtTokenManager
import me.him188.ani.danmaku.server.service.JwtTokenManagerImpl
import me.him188.ani.danmaku.server.service.TestBangumiLoginHelperImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.security.SecureRandom
import kotlin.time.Duration.Companion.days

fun getServerKoinModule(
    env: EnvironmentVariables,
    topCoroutineScope: CoroutineScope
) = module {
    single(named("topCoroutineScope")) { topCoroutineScope }
    single {
        ServerConfig(
            mongoDbConnectionString = env.mongoDbConnectionString,
            danmakuGetRequestMaxCountAllowed = 8000,
            jwt = JwtConfig(
                secret = env.jwtSecret?.toByteArray() ?: generateSecureRandomBytes(),
                issuer = env.jwtIssuer ?: throw IllegalStateException("JWT issuer is not set"),
                audience = env.jwtAudience ?: throw IllegalStateException("JWT audience is not set"),
                expiration = env.jwtExpiration ?: 7.days.inWholeMilliseconds,
                realm = env.jwtRealm ?: "Ani Danmaku"
            )
        )
    }

    single<DanmakuService> { DanmakuServiceImpl() }
    single<AuthService> { AuthServiceImpl() }
    single<JwtTokenManager> { JwtTokenManagerImpl() }

    if (env.testing) {
        single<DanmakuRepository> { InMemoryDanmakuRepositoryImpl() }
        single<UserRepository> { InMemoryUserRepositoryImpl() }
        single<BangumiLoginHelper> { TestBangumiLoginHelperImpl() }
    } else {
        single<MongoCollectionProvider> { MongoCollectionProviderImpl() }
        single<DanmakuRepository> { MongoDanmakuRepositoryImpl() }
        single<UserRepository> { MongoUserRepositoryImpl() }
        single<BangumiLoginHelper> { BangumiLoginHelperImpl() }
    }
}

/**
 * Function that generates a secure random 32-byte array to be used as a secret for the [JwtConfig].
 */
private fun generateSecureRandomBytes(): ByteArray {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    return bytes
}

class ServerConfig(
    val mongoDbConnectionString: String?,
    val danmakuGetRequestMaxCountAllowed: Int,
    val jwt: JwtConfig,
)

class JwtConfig(
    val secret: ByteArray,
    val issuer: String,
    val audience: String,
    val expiration: Long,
    val realm: String,
)