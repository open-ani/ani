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
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.him188.animationgarden.datasources.api.Subject
import me.him188.animationgarden.datasources.api.SubjectCollection
import me.him188.animationgarden.datasources.api.SubjectImages
import me.him188.animationgarden.datasources.bangumi.dto.BangumiAccount
import me.him188.animationgarden.utils.serialization.toJsonArray

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
     * @param keyword 关键字, 例如番剧名称
     * @param types 条目类型, 默认为 [BangumiSubjectType.ANIME]
     * @param responseGroup 返回数据大小, 默认为 [BangumiResponseGroup.SMALL]
     * @param offset 开始条数, 默认为 0
     * @param limit 返回条数, 最大为 25
     * @return 搜索结果, null 表示已经到达最后一条
     */
    suspend fun searchSubjectByKeywords(
        keyword: String,
        offset: Int? = null,
        limit: Int? = null,
        sort: BangumiSort? = null,
        types: List<BangumiSubjectType>? = null,
        tags: List<String>? = null, // "童年", "原创"
        airDates: List<String>? = null, // YYYY-MM-DD
        ratings: List<String>? = null, // ">=6", "<8"
        ranks: List<String>? = null,
        nsfw: Boolean? = null,
    ): List<BangumiSubject>?

    suspend fun getSubjectById(
        id: Long,
    ): BangumiSubjectDetails?

    suspend fun getSubjectImageUrl(
        id: Long,
        size: BangumiSubjectImageSize,
    ): String
}

interface BangumiClient : Closeable {
    // Bangumi open API: https://github.com/bangumi/api/blob/master/open-api/api.yml

    val accounts: BangumiClientAccounts

    val subjects: BangumiClientSubjects

    companion object Factory {
        fun create(httpClientConfiguration: HttpClientConfig<*>.() -> Unit = {}): BangumiClient =
            BangumiClientImpl(httpClientConfiguration)
    }
}

@Serializable
data class BangumiSubjectTag(
    val name: String,
    val count: Int,
)

@Serializable
data class BangumiSubject(
    val id: Long,
    val type: BangumiSubjectType,
    @SerialName("date") val airDate: String, // "2002-04-02"
    @SerialName("image") val image: String, // cover
    val summary: String,
    val name: String, // 日文
    @SerialName("name_cn") val nameCN: String, // 中文
    val tags: List<BangumiSubjectTag>,
    val score: Double,
    val rank: Int,
)

fun BangumiSubject.toSubject(): Subject {
    val subject = this
    return Subject(
        id = subject.id.toString(),
        originalName = subject.name,
        chineseName = subject.nameCN,
        images = SubjectImages(
            landscapeCommon = "https://api.bgm.tv/v0/subjects/${subject.id}/image?type=common",
            largePoster = "https://api.bgm.tv/v0/subjects/${subject.id}/image?type=large",
        ),
        score = subject.score,
        rank = subject.rank,
        sourceUrl = "https://bangumi.tv/subject/${subject.id}",
        tags = subject.tags.map { it.name to it.count },
        summary = subject.summary,
    )
}

//fun BangumiSubjectDetails.toSubjectDetails(): SubjectDetails {
//    val subject = this
//    return SubjectDetails(
//        id = subject.id.toString(),
//        originalName = subject.name,
//        chineseName = subject.nameCN,
//        images = SubjectImages(
//            landscapeCommon = subject.images.large,
//            largePoster = subject.images.large,
//        ),
//        score = subject.score,
//        rank = subject.rank,
//        sourceUrl = "https://bangumi.tv/subject/${subject.id}",
//        tags = subject.tags.map { it.name to it.count },
//        summary = subject.summary,
//        nsfw = subject.nsfw,
//        locked = subject.locked,
//        platform = subject.platform,
//        infobox = subject.infobox,
//        volumes = subject.volumes,
//        eps = subject.eps,
//        totalEpisodes = subject.totalEpisodes,
////        collection = subject.collection.toSubjectCollection(),
////        rating = subject.rating.toSubjectRating(),
//    )
//}

private fun BangumiCollection.toSubjectCollection(): SubjectCollection? {
    return SubjectCollection(
        wish = this.wish,
        collect = this.collect,
        doing = this.doing,
        onHold = this.onHold,
        dropped = this.dropped,
    )
}

@Serializable
data class BangumiSubjectDetails(
    val id: Long,
    val type: BangumiSubjectType,
    @SerialName("date") val airDate: String, // "2002-04-02"
    @SerialName("images") val images: BangumiSubjectImages,
    val summary: String, // can be very long
    @SerialName("name") val originalName: String, // 日文
    @SerialName("name_cn") val chineseName: String, // 中文
    val tags: List<BangumiSubjectTag>,

    val nsfw: Boolean = false,
    val locked: Boolean,
    val platform: String = "",
    val infobox: List<BangumiSubjectInfo>,
    val volumes: Int = 0,
    val eps: Int = 1, // 话数
    @SerialName("total_episodes") val totalEpisodes: Int,
    val rating: BangumiRating,
    val collection: BangumiCollection,
)

@Serializable
data class BangumiSubjectInfo(
    val key: String,
    val value: JsonElement,
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
    val rank: Int,
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
enum class BangumiSubjectImageSize {
    @SerialName("small")
    SMALL,

    @SerialName("medium")
    MEDIUM,

    @SerialName("large")
    LARGE,

    @SerialName("grid")
    GRID,

    @SerialName("common")
    COMMON,
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

@Serializable
enum class BangumiSort {
    // don't change names, used as .lowercase()

    /**
     * 按照匹配程度
     */
    @SerialName("match")
    MATCH,

    /**
     * 收藏人数
     */
    @SerialName("heat")
    HEAT,

    @SerialName("rank")
    RANK,

    @SerialName("score")
    SCORE,
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

internal class BangumiClientImpl(
    httpClientConfiguration: HttpClientConfig<*>.() -> Unit = {},
) : BangumiClient {
    private val httpClient = HttpClient(CIO) {
        httpClientConfiguration()
        install(HttpRequestRetry) {
            maxRetries = 3
            delayMillis { 2000 }
        }
        install(HttpTimeout)
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    @Serializable
    private data class SearchSubjectByKeywordsResponse(
        val total: Int,
        val data: List<BangumiSubject>? = null,
    )

    override val accounts: BangumiClientAccounts = object : BangumiClientAccounts {
        override suspend fun login(
            email: String,
            password: String,
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
            keyword: String,
            offset: Int?,
            limit: Int?,
            sort: BangumiSort?,
            types: List<BangumiSubjectType>?,
            tags: List<String>?,
            airDates: List<String>?,
            ratings: List<String>?,
            ranks: List<String>?,
            nsfw: Boolean?,
        ): List<BangumiSubject>? {
            val resp = httpClient.post("https://api.bgm.tv/v0/search/subjects") {
                offset?.let { parameter("offset", it) }
                limit?.let { parameter("limit", it) }

                contentType(ContentType.Application.Json)
                val req = buildJsonObject {
                    put("keyword", keyword)
                    sort?.let { sort ->
                        put("sort", sort.name.lowercase())
                    }

                    put("filter", buildJsonObject {
                        types?.let { types -> put("type", types.map { it.id }.toJsonArray()) }
                        ranks?.let { put("rank", it.toJsonArray()) }
                        tags?.let { put("tag", it.toJsonArray()) }
                        airDates?.let { put("air_date", it.toJsonArray()) }
                        ratings?.let { put("rating", it.toJsonArray()) }
                        nsfw?.let { put("nsfw", it) }
                    })
                }
                setBody(req)
            }

            if (!resp.status.isSuccess()) {
                throw IllegalStateException("Failed to search subject by keywords: $resp")
            }

            return resp.body<SearchSubjectByKeywordsResponse>().data
        }

        override suspend fun getSubjectById(id: Long): BangumiSubjectDetails? {
            val resp = httpClient.get("https://api.bgm.tv/v0/subjects/${id}")

            if (!resp.status.isSuccess()) {
                throw IllegalStateException("Failed to get subject by id: $resp")
            }

            return resp.body()
        }

        override suspend fun getSubjectImageUrl(id: Long, size: BangumiSubjectImageSize): String {
            return "https://api.bgm.tv/v0/subject/${id}/image?type=${size.name.lowercase()}"
//            val resp = httpClient.get("https://api.bgm.tv/v0/subject/${id}/image")
//
//            if (!resp.status.isSuccess()) {
//                throw IllegalStateException("Failed to get subject images by id: $resp")
//            }
//
//            return resp.body()
        }
    }

    override fun close() {
        httpClient.close()
    }
}
