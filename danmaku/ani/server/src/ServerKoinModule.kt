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
import me.him188.ani.danmaku.server.service.AniClientVersionVerifierImpl
import me.him188.ani.danmaku.server.service.AuthService
import me.him188.ani.danmaku.server.service.AuthServiceImpl
import me.him188.ani.danmaku.server.service.BangumiLoginHelper
import me.him188.ani.danmaku.server.service.BangumiLoginHelperImpl
import me.him188.ani.danmaku.server.service.ClientReleaseInfoManager
import me.him188.ani.danmaku.server.service.ClientReleaseInfoManagerImpl
import me.him188.ani.danmaku.server.service.DanmakuService
import me.him188.ani.danmaku.server.service.DanmakuServiceImpl
import me.him188.ani.danmaku.server.service.ClientVersionVerifier
import me.him188.ani.danmaku.server.service.GithubVersionVerifierImpl
import me.him188.ani.danmaku.server.service.JwtTokenManager
import me.him188.ani.danmaku.server.service.JwtTokenManagerImpl
import me.him188.ani.danmaku.server.service.TestBangumiLoginHelperImpl
import me.him188.ani.danmaku.server.service.TestClientReleaseInfoManager
import me.him188.ani.danmaku.server.service.UserService
import me.him188.ani.danmaku.server.service.UserServiceImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.helpers.NOPLogger
import kotlin.time.Duration.Companion.minutes

fun getServerKoinModule(
    config: ServerConfig,
    topCoroutineScope: CoroutineScope,
    logger: Logger = NOPLogger.NOP_LOGGER,
) = module {
    single(named("topCoroutineScope")) { topCoroutineScope }
    single<Logger> { logger }
    single<ServerConfig> { config }

    single<DanmakuService> { DanmakuServiceImpl() }
    single<AuthService> { AuthServiceImpl() }
    single<UserService> { UserServiceImpl() }
    single<JwtTokenManager> { JwtTokenManagerImpl() }
    single<ClientVersionVerifier> {
        AniClientVersionVerifierImpl(
            versionWhitelistRegex = listOf("[3-9].[0-9]{1,2}.[0-9]{1,2}-dev")
        )
    }

    if (config.testing) {
        single<DanmakuRepository> { InMemoryDanmakuRepositoryImpl() }
        single<UserRepository> { InMemoryUserRepositoryImpl() }
        single<BangumiLoginHelper> { TestBangumiLoginHelperImpl() }
        
        single<ClientReleaseInfoManager> { TestClientReleaseInfoManager() }
    } else {
        single<MongoCollectionProvider> { MongoCollectionProviderImpl() }
        single<DanmakuRepository> { MongoDanmakuRepositoryImpl() }
        single<UserRepository> { MongoUserRepositoryImpl() }
        single<BangumiLoginHelper> { BangumiLoginHelperImpl() }

        single<ClientReleaseInfoManager> { ClientReleaseInfoManagerImpl(
            bufferExpirationTime = 10.minutes.inWholeMilliseconds
        ) }
    }
}