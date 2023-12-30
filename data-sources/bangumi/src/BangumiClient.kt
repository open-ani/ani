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

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

internal interface BangumiClient : Closeable {
    // Bangumi open API: https://github.com/bangumi/api/blob/master/open-api/api.yml

    suspend fun searchSubjectByKeywords(
        keywords: String,
        type: BangumiSubjectType? = null,
        responseGroup: BangumiResponseGroup? = null, // 返回数据大小, 默认为 small
        start: Int? = null, // 开始条数, 默认为 0
        maxResults: Int? = null, // 返回条数, 最大为 25
    ): List<BangumiSubject>

    companion object Factory {
        fun create(): BangumiClient = BangumiClientImpl()
    }
}

@Serializable
internal data class BangumiSubject(
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
internal data class BangumiCollection(
    val wish: Int = 0,
    val collect: Int = 0,
    val doing: Int = 0,
    @SerialName("on_hold") val onHold: Int = 0,
    val dropped: Int = 0,
)

@Serializable
internal data class BangumiRating(
    /**
     * 总评分人数
     */
    val total: Int = 0,
    val count: Map<Rating, Int> = mapOf(),
    val score: Double = 0.0,
)

@Serializable(with = Rating.AsStringSerializer::class)
internal enum class Rating(
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
internal data class BangumiSubjectImages(
    val large: String = "",
    val common: String = "",
    val medium: String = "",
    val small: String = "",
    val grid: String = "",
)

internal enum class BangumiResponseGroup(val id: String) {
    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large"),
    ;
}

// Legacy_SubjectType
@Serializable(BangumiSubjectType.AsIntSerializer::class)
internal enum class BangumiSubjectType(val id: Int) {
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
        val list: List<BangumiSubject>,
    )

    override suspend fun searchSubjectByKeywords(
        keywords: String,
        type: BangumiSubjectType?,
        responseGroup: BangumiResponseGroup?,
        start: Int?,
        maxResults: Int?,
    ): List<BangumiSubject> {
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

    override fun close() {
        httpClient.close()
    }
}