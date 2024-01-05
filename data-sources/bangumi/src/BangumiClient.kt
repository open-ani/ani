/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.animationgarden.datasources.bangumi

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.Closeable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import me.him188.animationgarden.datasources.bangumi.dto.BangumiAccount

class BangumiToken(
    internal val username: String,
    internal val auth: String,
    internal val authEncode: String,
)

interface BangumiClientAccounts {
    sealed interface LoginResponse {
        data class Success(
            val account: BangumiAccount,
            val token: BangumiToken,
        ) : LoginResponse

        data object UsernameOrPasswordMismatch : LoginResponse
        data class UnknownError(
            val trace: String,
        ) : LoginResponse
    }

    suspend fun login(
        email: String,
        password: String,
    ): LoginResponse
}

interface BangumiClientSubjects {
    /**
     * 搜索条目.
     *
     * @param keywords 关键字, 例如番剧名称
     * @param type 条目类型, 默认为 [BangumiSubjectType.ANIME]
     * @param responseGroup 返回数据大小, 默认为 [BangumiResponseGroup.SMALL]
     * @param start 开始条数, 默认为 0
     * @param maxResults 返回条数, 最大为 25
     * @return 搜索结果, null 表示已经到达最后一条
     */
    suspend fun searchSubjectByKeywords(
        keywords: String,
        type: BangumiSubjectType? = null,
        responseGroup: BangumiResponseGroup? = null,
        start: Int? = null,
        maxResults: Int? = null,
    ): List<BangumiSubject>?
}

interface BangumiClient : Closeable {
    // Bangumi open API: https://github.com/bangumi/api/blob/master/open-api/api.yml

    val accounts: BangumiClientAccounts

    val subjects: BangumiClientSubjects

    companion object Factory {
        fun create(): BangumiClient = BangumiClientImpl()
    }
}

@Serializable
data class BangumiSubject(
    // 以下为 small
    val id: Long,
    val url: String,
    val type: BangumiSubjectType,
    val name: String, // 日文
    @SerialName("name_cn") val nameCN: String = name, // 中文
    val images: BangumiSubjectImages,

    // 以下为 medium
    val summary: String = "",
    val eps: Int = 1, // 话数
    @SerialName("eps_count") val epsCount: Int = 0, // 实测跟 eps 一样
    val rank: Int = 0,
    val collection: BangumiCollection? = null,

    // 以下为 large
    @SerialName("air_date") val airDate: String = "", // "2002-04-02"
    @SerialName("air_weekday") val airWeekday: Int = 0,
    val rating: BangumiRating? = null,
)

@Serializable
data class BangumiCollection(
    val wish: Int = 0,
    val collect: Int = 0,
    val doing: Int = 0,
    @SerialName("on_hold") val onHold: Int = 0,
    val dropped: Int = 0,
)

@Serializable
data class BangumiRating(
    /**
     * 总评分人数
     */
    val total: Int = 0,
    val count: Map<Rating, Int> = mapOf(),
    val score: Double = 0.0,
)

@Serializable(with = Rating.AsStringSerializer::class)
enum class Rating(
    val id: String,
) {
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    TEN("10"),
    ;

    internal object AsStringSerializer : KSerializer<Rating> {
        override val descriptor: SerialDescriptor = String.serializer().descriptor

        override fun deserialize(decoder: Decoder): Rating {
            val raw = String.serializer().deserialize(decoder)
            return entries.firstOrNull { it.id == raw }
                ?: throw IllegalStateException("Unknown rating: $raw")
        }

        override fun serialize(encoder: Encoder, value: Rating) {
            return String.serializer().serialize(encoder, value.id)
        }
    }
}

@Serializable
data class BangumiSubjectImages(
    val large: String = "",
    val common: String = "",
    val medium: String = "",
    val small: String = "",
    val grid: String = "",
)

enum class BangumiResponseGroup(val id: String) {
    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large"),
    ;
}

// Legacy_SubjectType
@Serializable(BangumiSubjectType.AsIntSerializer::class)
enum class BangumiSubjectType(val id: Int) {
    BOOK(1),
    ANIME(2),
    MUSIC(3),
    GAME(4),
    REAL(6),
    ;

    internal object AsIntSerializer : KSerializer<BangumiSubjectType> {
        override val descriptor: SerialDescriptor = Int.serializer().descriptor

        override fun deserialize(decoder: Decoder): BangumiSubjectType {
            val raw = Int.serializer().deserialize(decoder)
            return entries.firstOrNull { it.id == raw }
                ?: throw IllegalStateException("Unknown BangumiSubjectType: $raw")
        }

        override fun serialize(encoder: Encoder, value: BangumiSubjectType) {
            return Int.serializer().serialize(encoder, value.id)
        }
    }
}

internal class BangumiClientImpl : BangumiClient {
    private val httpClient = HttpClient(CIO) {
        install(HttpRequestRetry) {
            maxRetries = 3
            delayMillis { 2000 }
        }
        install(HttpTimeout)
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient
            })
        }
    }

    @Serializable
    private class SearchSubjectByKeywordsResponse(
//        val results: Int, // count
        val list: List<BangumiSubject>? = null,
    )

    override val accounts: BangumiClientAccounts = object : BangumiClientAccounts {
        override suspend fun login(
            email: String,
            password: String
        ): BangumiClientAccounts.LoginResponse {
            val resp = httpClient.get("https://api.bgm.tv/auth?source=onAir") {
                basicAuth(email, password)
            }

            when {
                resp.status.isSuccess() -> {
                    val body = resp.body<BangumiAccount>()
                    return BangumiClientAccounts.LoginResponse.Success(
                        account = body,
                        token = BangumiToken(
                            username = email,
                            auth = body.auth,
                            authEncode = body.authEncode,
                        )
                    )
                }

                resp.status == HttpStatusCode.Unauthorized -> {
                    return BangumiClientAccounts.LoginResponse.UsernameOrPasswordMismatch
                }

                else -> return BangumiClientAccounts.LoginResponse.UnknownError(
                    trace = resp.bodyAsText()
                )
            }
        }
    }

    override val subjects: BangumiClientSubjects = object : BangumiClientSubjects {
        override suspend fun searchSubjectByKeywords(
            keywords: String,
            type: BangumiSubjectType?,
            responseGroup: BangumiResponseGroup?,
            start: Int?,
            maxResults: Int?,
        ): List<BangumiSubject>? {
            val resp = httpClient.get("https://api.bgm.tv/search/subject/${keywords}") {
                type?.id?.let { parameter("type", it) }
                responseGroup?.id?.let { parameter("responseGroup", it) }
                start?.let { parameter("start", it) }
                maxResults?.let { parameter("max_results", it) }
            }

            if (!resp.status.isSuccess()) {
                throw IllegalStateException("Failed to search subject by keywords: $resp")
            }

            val body = resp.body<SearchSubjectByKeywordsResponse>()
            return body.list
        }
    }

    override fun close() {
        httpClient.close()
    }
}
