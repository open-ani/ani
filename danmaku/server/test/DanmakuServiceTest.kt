package me.him188.ani.danmaku.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import me.him188.ani.danmaku.protocol.DanmakuInfo
import me.him188.ani.danmaku.protocol.DanmakuLocation
import me.him188.ani.danmaku.server.service.DanmakuService
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import java.awt.Color
import kotlin.test.Test

class DanmakuServiceTest {
    private val koin = object : KoinComponent {}
    
    private fun runTestWithKoin(block: suspend () -> Unit) = runTest {
        val coroutineScope = CoroutineScope(SupervisorJob())
        startKoin {
            modules(
                getServerKoinModule(EnvironmentVariables(testing = true), coroutineScope)
            )
        }
        block()
        stopKoin()
    }
    
    @Test
    fun `test post and get danmaku`() = runTestWithKoin {
        val danmakuService = koin.get<DanmakuService>()
        val testDanmakus = listOf(
            DanmakuInfo(
                playTime = 1.0,
                color = Color.BLACK.rgb,
                text = "test danmaku 1",
                location = DanmakuLocation.NORMAL
            ), DanmakuInfo(
                playTime = 2.0,
                color = Color.CYAN.rgb,
                text = "test danmaku 2",
                location = DanmakuLocation.TOP
            ), DanmakuInfo(
                playTime = 3.0,
                color = Color.RED.rgb,
                text = "test danmaku 3",
                location = DanmakuLocation.BOTTOM
            )
        )

        val episodeId = "test_episode_id"
        val userId = "test_user_id"
        testDanmakus.forEach {
            danmakuService.postDanmaku(episodeId, it, userId)
        }
        danmakuService.getDanmaku(episodeId, 8000, 0.0, -1.0).also {
            assert(it.size == 3)
            assert(it[0].danmakuInfo == testDanmakus[0])
            assert(it[1].danmakuInfo == testDanmakus[1])
            assert(it[2].danmakuInfo == testDanmakus[2])
        }
    }
    
    @Test
    fun `test get danmaku by different episode`() = runTestWithKoin {
        val danmakuService = koin.get<DanmakuService>()
        val testDanmakus = listOf(
            DanmakuInfo(
                playTime = 1.0,
                color = Color.BLACK.rgb,
                text = "test danmaku 1",
                location = DanmakuLocation.NORMAL
            ), DanmakuInfo(
                playTime = 2.0,
                color = Color.CYAN.rgb,
                text = "test danmaku 2",
                location = DanmakuLocation.TOP
            ), DanmakuInfo(
                playTime = 3.0,
                color = Color.RED.rgb,
                text = "test danmaku 3",
                location = DanmakuLocation.BOTTOM
            )
        )

        val episodeId1 = "test_episode_id_1"
        val episodeId2 = "test_episode_id_2"
        val userId = "test_user_id"
        danmakuService.postDanmaku(episodeId1, testDanmakus[0], userId)
        danmakuService.postDanmaku(episodeId1, testDanmakus[1], userId)
        danmakuService.postDanmaku(episodeId2, testDanmakus[2], userId)
        danmakuService.getDanmaku(episodeId1, 8000, 0.0, -1.0).also {
            assert(it.size == 2)
            assert(it[0].danmakuInfo == testDanmakus[0])
            assert(it[1].danmakuInfo == testDanmakus[1])
        }
        danmakuService.getDanmaku(episodeId2, 8000, 0.0, -1.0).also {
            assert(it.size == 1)
            assert(it[0].danmakuInfo == testDanmakus[2])
        }
    }
    
    @Test
    fun `test get danmaku by different time`() = runTestWithKoin {
        val danmakuService = koin.get<DanmakuService>()
        val testDanmakus = listOf(
            DanmakuInfo(
                playTime = 1.0,
                color = Color.BLACK.rgb,
                text = "test danmaku 1",
                location = DanmakuLocation.NORMAL
            ), DanmakuInfo(
                playTime = 2.0,
                color = Color.CYAN.rgb,
                text = "test danmaku 2",
                location = DanmakuLocation.TOP
            ), DanmakuInfo(
                playTime = 3.0,
                color = Color.RED.rgb,
                text = "test danmaku 3",
                location = DanmakuLocation.BOTTOM
            )
        )

        val episodeId = "test_episode_id"
        val userId = "test_user_id"
        danmakuService.postDanmaku(episodeId, testDanmakus[0], userId)
        danmakuService.postDanmaku(episodeId, testDanmakus[1], userId)
        danmakuService.postDanmaku(episodeId, testDanmakus[2], userId)
        danmakuService.getDanmaku(episodeId, 8000, 0.0, 1.5).also {
            assert(it.size == 1)
            assert(it[0].danmakuInfo == testDanmakus[0])
        }
        danmakuService.getDanmaku(episodeId, 8000, 1.5, 2.5).also {
            assert(it.size == 1)
            assert(it[0].danmakuInfo == testDanmakus[1])
        }
        danmakuService.getDanmaku(episodeId, 8000, 2.5, -1.0).also {
            assert(it.size == 1)
            assert(it[0].danmakuInfo == testDanmakus[2])
        }
    }
    
    @Test
    fun `test get danmaku with max amount`() = runTestWithKoin {
        val danmakuService = koin.get<DanmakuService>()
        val testDanmakus = listOf(
            DanmakuInfo(
                playTime = 1.0,
                color = Color.BLACK.rgb,
                text = "test danmaku 1",
                location = DanmakuLocation.NORMAL
            ), DanmakuInfo(
                playTime = 2.0,
                color = Color.CYAN.rgb,
                text = "test danmaku 2",
                location = DanmakuLocation.TOP
            ), DanmakuInfo(
                playTime = 3.0,
                color = Color.RED.rgb,
                text = "test danmaku 3",
                location = DanmakuLocation.BOTTOM
            )
        )

        val episodeId = "test_episode_id"
        val userId = "test_user_id"
        testDanmakus.forEach {
            danmakuService.postDanmaku(episodeId, it, userId)
        }
        danmakuService.getDanmaku(episodeId, 2, 0.0, -1.0).also {
            assert(it.size == 2)
            assert(it[0].danmakuInfo == testDanmakus[0])
            assert(it[1].danmakuInfo == testDanmakus[1])
        }
    }
}