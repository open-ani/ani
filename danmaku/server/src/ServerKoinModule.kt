package me.him188.ani.danmaku.server

import kotlinx.coroutines.CoroutineScope
import me.him188.ani.danmaku.server.data.DanmakuRepository
import me.him188.ani.danmaku.server.data.InMemoryDanmakuRepositoryImpl
import me.him188.ani.danmaku.server.data.mongodb.MongoCollectionProvider
import me.him188.ani.danmaku.server.data.mongodb.MongoCollectionProviderImpl
import me.him188.ani.danmaku.server.data.mongodb.MongoDanmakuRepositoryImpl
import me.him188.ani.danmaku.server.service.DanmakuService
import me.him188.ani.danmaku.server.service.DanmakuServiceImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun getServerKoinModule(
    env: EnvironmentVariables,
    topCoroutineScope: CoroutineScope
) = module {
    single(named("topCoroutineScope")) { topCoroutineScope }
    single {
        ServerConfig(
            testing = env.testing,
            mongoDbConnectionString = env.mongoDbConnectionString,
            danmakuGetRequestMaxCountAllowed = 8000,
        )
    }

    single<DanmakuService> { DanmakuServiceImpl() }

    if (!env.testing) {
        single<MongoCollectionProvider> { MongoCollectionProviderImpl() }
    }

    single<DanmakuRepository> {
        if (env.testing) {
            InMemoryDanmakuRepositoryImpl()
        } else {
            MongoDanmakuRepositoryImpl()
        }
    }
}

data class ServerConfig(
    val testing: Boolean,
    val mongoDbConnectionString: String?,
    val danmakuGetRequestMaxCountAllowed: Int,
)