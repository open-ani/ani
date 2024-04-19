package me.him188.ani.danmaku.server.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

interface BangumiLoginHelper {
    suspend fun login(bangumiToken: String): BangumiUser?
}

class TestBangumiLoginHelperImpl : BangumiLoginHelper {
    override suspend fun login(bangumiToken: String): BangumiUser? {
        return when (bangumiToken) {
            "test_token_1" -> BangumiUser(1, "test", "small", "medium", "large")
            "test_token_2" -> BangumiUser(2, "test2", "small2", "medium2", "large2")
            "test_token_3" -> BangumiUser(3, "test3", "small3", "medium3", "large3")
            else -> null
        }
    }
}

class BangumiLoginHelperImpl : BangumiLoginHelper, KoinComponent {
    private val httpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    private val bangumiLoginUrl = "https://api.bgm.tv/v0/me"
    private val log : Logger by inject()

    override suspend fun login(bangumiToken: String): BangumiUser? {
        try {
            val response = httpClient.get(bangumiLoginUrl) {
                bearerAuth(bangumiToken)
            }
            if (response.status != HttpStatusCode.OK) {
                log.info("Failed to login to Bangumi due to: Bangumi responded with ${response.status}")
                return null
            }
            val user = response.body<User>()
            return BangumiUser(user.id, user.nickname, user.avatar.small, user.avatar.medium, user.avatar.large)
        } catch (e: Exception) {
            log.info("Failed to login to Bangumi due to: ${e.printStackTrace()}")
            return null
        }
    }

    @Serializable
    data class User(
        val avatar: Avatar,
        val sign: String,
        val username: String,
        val nickname: String,
        val id: Int,
        @SerialName("user_group") val userGroup: Int,
    )

    @Serializable
    data class Avatar(
        val large: String,
        val medium: String,
        val small: String,
    )
}

data class BangumiUser(
    val id: Int,
    val nickname: String,
    val smallAvatar: String,
    val mediumAvatar: String,
    val largeAvatar: String,
)

//fun main() {
//    val bangumiLoginHelper = BangumiLoginHelperImpl()
//    val token = "<enter test token here>"
//    runBlocking {
//        val user = bangumiLoginHelper.login(token)
//        println(user)
//    }
//}
