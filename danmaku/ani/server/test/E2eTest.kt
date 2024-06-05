package me.him188.ani.danmaku.server

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import me.him188.ani.danmaku.protocol.AniUser
import me.him188.ani.danmaku.protocol.BangumiLoginRequest
import me.him188.ani.danmaku.protocol.BangumiLoginResponse
import me.him188.ani.danmaku.protocol.DanmakuGetResponse
import me.him188.ani.danmaku.protocol.DanmakuInfo
import me.him188.ani.danmaku.protocol.DanmakuLocation
import me.him188.ani.danmaku.protocol.DanmakuPostRequest
import me.him188.ani.danmaku.protocol.ReleaseUpdatesDetailedResponse
import me.him188.ani.danmaku.protocol.UpdateInfo
import me.him188.ani.danmaku.server.ktor.getKtorServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.stopKoin
import java.awt.Color
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class E2eTest {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    private val serverEndpoint = "http://localhost:4394/v1"

    @Test
    fun `test login, get username, post danmaku and get danmaku`() = runTest {
        val danmaku = DanmakuInfo(
            playTime = 1000,
            color = Color.BLACK.rgb,
            text = "This is a danmaku",
            location = DanmakuLocation.NORMAL
        )

        val response = client.post("$serverEndpoint/login/bangumi") {
            contentType(ContentType.Application.Json)
            setBody(BangumiLoginRequest(
                bangumiToken = "test_token_1",
                clientVersion = "3.0.0-dev",
                clientOS = "Android",
                clientArch = "aarch64",
            ))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val token = response.body<BangumiLoginResponse>().token

        val response2 = client.post("$serverEndpoint/danmaku/1") {
            contentType(ContentType.Application.Json)
            setBody<DanmakuPostRequest>(
                DanmakuPostRequest(danmaku)
            )
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, response2.status)

        val response5 = client.get("$serverEndpoint/me") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, response5.status)
        val me = response5.body<AniUser>()

        val response3 = client.get("$serverEndpoint/danmaku/1") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, response3.status)
        val danmakuList = response3.body<DanmakuGetResponse>().danmakuList
        assertEquals(1, danmakuList.size)
        assertEquals(danmaku, danmakuList[0].danmakuInfo)
        assertEquals(me.id, danmakuList[0].senderId)

        val response4 = client.get("$serverEndpoint/danmaku/1") {
            parameter("fromTime", 2000)
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, response4.status)
        val danmakuList2 = response4.body<DanmakuGetResponse>().danmakuList
        assertTrue(danmakuList2.isEmpty())
    }
    
    @Test
    fun `test get update info`() = runTest {
        val response1 = client.get("$serverEndpoint/updates/incremental/details")
        assertEquals(HttpStatusCode.BadRequest, response1.status)
        println(response1.body<String>())
        
        val response2 = client.get("$serverEndpoint/updates/incremental/details") {
            parameter("clientVersion", "1.0.0")
            parameter("clientPlatform", "android")
            parameter("clientArch", "aarch64")
            parameter("releaseClass", "stable")
        }
        assertEquals(HttpStatusCode.OK, response2.status)
        val versions = response2.body<ReleaseUpdatesDetailedResponse>()
        assertContentEquals(
            listOf(
                UpdateInfo(
                    version = "1.0.0",
                    downloadUrlAlternatives = listOf("testUrl/v1.0.0"),
                    publishTime = 0,
                    description = "This is version 1.0.0",
                ),
                UpdateInfo(
                    version = "1.0.1",
                    downloadUrlAlternatives = listOf("testUrl/v1.0.1"),
                    publishTime = 0,
                    description = "This is version 1.0.1",
                ),
                UpdateInfo(
                    version = "2.0.0",
                    downloadUrlAlternatives = listOf("testUrl/v2.0.0"),
                    publishTime = 0,
                    description = "This is version 2.0.0",
                )
            ),
            versions.updates
        )
    }

    companion object {
        private val server = getKtorServer {
            port = 4394
            testing = true
            jwt {
                issuer = "test-issuer"
                audience = "test-audience"
            }
        }

        @JvmStatic
        @BeforeAll
        fun setup() {
            server.start(wait = false)
        }

        @JvmStatic
        @AfterAll
        fun teardown() {
            server.stop(1000, 1000)
            stopKoin()
        }
    }
}