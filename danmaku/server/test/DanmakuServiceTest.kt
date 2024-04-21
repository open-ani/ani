package me.him188.ani.danmaku.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import me.him188.ani.danmaku.protocol.DanmakuInfo
import me.him188.ani.danmaku.protocol.DanmakuLocation
import me.him188.ani.danmaku.server.service.DanmakuService
import me.him188.ani.danmaku.server.util.exception.AcquiringTooMuchDanmakusException
import me.him188.ani.danmaku.server.util.exception.EmptyDanmakuException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.assertThrows
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import java.awt.Color
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class DanmakuServiceTest {
    private val koin = object : KoinComponent {}

    private val testDanmakus = listOf(
        DanmakuInfo(
            playTime = 1.seconds.inWholeMilliseconds,
            color = Color.BLACK.rgb,
            text = "test danmaku 1",
            location = DanmakuLocation.NORMAL
        ), DanmakuInfo(
            playTime = 5.seconds.inWholeMilliseconds,
            color = Color.CYAN.rgb,
            text = "test danmaku 2",
            location = DanmakuLocation.TOP
        ), DanmakuInfo(
            playTime = 10.seconds.inWholeMilliseconds,
            color = Color.RED.rgb,
            text = "test danmaku 3",
            location = DanmakuLocation.BOTTOM
        )
    )

    private fun runTestWithKoin(block: suspend () -> Unit) = runTest {
        val coroutineScope = CoroutineScope(SupervisorJob())
        startKoin {
            val config = ServerConfigBuilder.create {
                testing = true
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
    fun `test post and get danmaku`() = runTestWithKoin {
        val danmakuService = koin.get<DanmakuService>()

        val episodeId = "test_episode_id"
        val userId = "test_user_id"
        testDanmakus.forEach {
            danmakuService.postDanmaku(episodeId, it, userId)
        }
        danmakuService.getDanmaku(episodeId = episodeId).also {
            assert(it.size == 3)
            assert(it[0].danmakuInfo == testDanmakus[0])
            assert(it[1].danmakuInfo == testDanmakus[1])
            assert(it[2].danmakuInfo == testDanmakus[2])
        }
    }

    @Test
    fun `test get danmaku by different episode`() = runTestWithKoin {
        val danmakuService = koin.get<DanmakuService>()

        val episodeId1 = "test_episode_id_1"
        val episodeId2 = "test_episode_id_2"
        val userId = "test_user_id"
        danmakuService.postDanmaku(episodeId1, testDanmakus[0], userId)
        danmakuService.postDanmaku(episodeId1, testDanmakus[1], userId)
        danmakuService.postDanmaku(episodeId2, testDanmakus[2], userId)
        danmakuService.getDanmaku(episodeId1).also {
            assert(it.size == 2)
            assert(it[0].danmakuInfo == testDanmakus[0])
            assert(it[1].danmakuInfo == testDanmakus[1])
        }
        danmakuService.getDanmaku(episodeId2).also {
            assert(it.size == 1)
            assert(it[0].danmakuInfo == testDanmakus[2])
        }
    }

    @Test
    fun `test get danmaku by different time`() = runTestWithKoin {
        val danmakuService = koin.get<DanmakuService>()


        val episodeId = "test_episode_id"
        val userId = "test_user_id"
        danmakuService.postDanmaku(episodeId, testDanmakus[0], userId)
        danmakuService.postDanmaku(episodeId, testDanmakus[1], userId)
        danmakuService.postDanmaku(episodeId, testDanmakus[2], userId)
        danmakuService.getDanmaku(episodeId, fromTime = 0, toTime = 3.seconds.inWholeMilliseconds).also {
            assert(it.size == 1)
            assert(it[0].danmakuInfo == testDanmakus[0])
        }
        danmakuService.getDanmaku(
            episodeId,
            fromTime = 3.seconds.inWholeMilliseconds,
            toTime = 6.seconds.inWholeMilliseconds
        ).also {
            assert(it.size == 1)
            assert(it[0].danmakuInfo == testDanmakus[1])
        }
        danmakuService.getDanmaku(
            episodeId,
            fromTime = 6.seconds.inWholeMilliseconds,
            toTime = 11.seconds.inWholeMilliseconds
        ).also {
            assert(it.size == 1)
            assert(it[0].danmakuInfo == testDanmakus[2])
        }
    }

    @Test
    fun `test get danmaku with max amount`() = runTestWithKoin {
        val danmakuService = koin.get<DanmakuService>()

        val episodeId = "test_episode_id"
        val userId = "test_user_id"
        testDanmakus.forEach {
            danmakuService.postDanmaku(episodeId, it, userId)
        }
        danmakuService.getDanmaku(episodeId, maxCount = 2).also {
            assert(it.size == 2)
            assert(it[0].danmakuInfo == testDanmakus[0])
            assert(it[1].danmakuInfo == testDanmakus[1])
        }
    }

    @Test
    fun `test get danmaku with too much amount`() = runTestWithKoin {
        val danmakuService = koin.get<DanmakuService>()

        val episodeId = "test_episode_id"
        val userId = "test_user_id"
        testDanmakus.forEach {
            danmakuService.postDanmaku(episodeId, it, userId)
        }
        assertThrows<AcquiringTooMuchDanmakusException> {
            danmakuService.getDanmaku(episodeId, maxCount = 8001)
        }
    }

    @Test
    fun `test post empty or blank danmaku`() = runTestWithKoin {
        val danmakuService = koin.get<DanmakuService>()

        val episodeId = "test_episode_id"
        val userId = "test_user_id"
        assertThrows<EmptyDanmakuException> {
            danmakuService.postDanmaku(
                episodeId, DanmakuInfo(
                    playTime = 5.seconds.inWholeMilliseconds,
                    color = Color.BLACK.rgb,
                    text = "",
                    location = DanmakuLocation.NORMAL
                ), userId
            )
        }
        assertThrows<EmptyDanmakuException> {
            danmakuService.postDanmaku(
                episodeId, DanmakuInfo(
                    playTime = 5.seconds.inWholeMilliseconds,
                    color = Color.BLACK.rgb,
                    text = "  \n\t",
                    location = DanmakuLocation.NORMAL
                ), userId
            )
        }
    }
}