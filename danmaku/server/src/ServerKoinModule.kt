package me.him188.ani.danmaku.server

import kotlinx.coroutines.CoroutineScope
import me.him188.ani.danmaku.server.data.DanmakuRepository
import me.him188.ani.danmaku.server.data.InMemoryDanmakuRepositoryImpl
import me.him188.ani.danmaku.server.data.mongodb.MongoDbDanmakuRepositoryImpl
import me.him188.ani.danmaku.server.service.DanmakuService
import me.him188.ani.danmaku.server.service.DanmakuServiceImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun getServerKoinModule(
    env: EnvironmentVariables,
    topCoroutineScope: CoroutineScope
) = module {
    single(named("env")) { env }
    single(named("topCoroutineScope")) { topCoroutineScope }
    single(named("danmakuGetRequestMaxCountAllowed")) { 8000 }
    
    single<DanmakuService> { DanmakuServiceImpl() }
    
    single<DanmakuRepository> { 
        if (env.testing) {
            InMemoryDanmakuRepositoryImpl()
        } else {
            MongoDbDanmakuRepositoryImpl()
        }
    }
}