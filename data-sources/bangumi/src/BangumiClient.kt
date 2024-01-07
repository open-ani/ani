/*
 * Ani
 * Copyright (C) 2022-2024 Him188
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

package me.him188.ani.datasources.bangumi

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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.him188.ani.datasources.api.Paged
import me.him188.ani.datasources.bangumi.client.BangumiClientAccounts
import me.him188.ani.datasources.bangumi.client.BangumiClientEpisodes
import me.him188.ani.datasources.bangumi.client.BangumiClientSubjects
import me.him188.ani.datasources.bangumi.client.BangumiEpType
import me.him188.ani.datasources.bangumi.client.BangumiEpisode
import me.him188.ani.datasources.bangumi.models.BangumiToken
import me.him188.ani.datasources.bangumi.models.search.BangumiSort
import me.him188.ani.datasources.bangumi.models.subjects.BangumiSubject
import me.him188.ani.datasources.bangumi.models.subjects.BangumiSubjectDetails
import me.him188.ani.datasources.bangumi.models.subjects.BangumiSubjectImageSize
import me.him188.ani.datasources.bangumi.models.subjects.BangumiSubjectType
import me.him188.ani.datasources.bangumi.models.users.BangumiAccount
import me.him188.ani.utils.serialization.toJsonArray
import okhttp3.OkHttpClient
import org.openapitools.client.apis.BangumiApi
import org.openapitools.client.models.RelatedPerson

interface BangumiClient : Closeable {
    // Bangumi open API: https://github.com/bangumi/api/blob/master/open-api/api.yml

    val api: BangumiApi

    val accounts: BangumiClientAccounts

    val subjects: BangumiClientSubjects

    val episodes: BangumiClientEpisodes

    companion object Factory {
        fun create(httpClientConfiguration: HttpClientConfig<*>.() -> Unit = {}): BangumiClient =
            BangumiClientImpl(httpClientConfiguration)
    }
}

private const val BANGUMI_API_HOST = "https://api.bgm.tv"

internal class BangumiClientImpl(
    httpClientConfiguration: HttpClientConfig<*>.() -> Unit = {},
) : BangumiClient {
    override val api = BangumiApi(BANGUMI_API_HOST, OkHttpClient.Builder().apply {
        this.followRedirects(true)
        addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder().addHeader(
                    "User-Agent",
                    "him188/ani/3.0.0-beta01 (Android) (https://github.com/Him188/ani)"
                ).build()
            )
        }
    }.build())

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
            val resp = httpClient.get("$BANGUMI_API_HOST/auth?source=onAir") {
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
        ): Paged<BangumiSubject> {
            val resp = httpClient.post("$BANGUMI_API_HOST/v0/search/subjects") {
                parameter("offset", offset)
                parameter("limit", limit)

                contentType(ContentType.Application.Json)
                val req = buildJsonObject {
                    put("keyword", keyword)
                    sort?.let { sort ->
                        put("sort", sort.id)
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

            val body = resp.body<SearchSubjectByKeywordsResponse>()
            return body.run {
                Paged(
                    total,
                    data == null || (offset ?: 0) + data.size < total,
                    data.orEmpty()
                )
            }
        }

        override suspend fun getSubjectById(id: Long): BangumiSubjectDetails? {
            val resp = httpClient.get("$BANGUMI_API_HOST/v0/subjects/${id}")

            if (!resp.status.isSuccess()) {
                throw IllegalStateException("Failed to get subject by id: $resp")
            }

            return resp.body()
        }

        override suspend fun getSubjectImageUrl(id: Long, size: BangumiSubjectImageSize): String {
            return Companion.getSubjectImageUrl(id, size)
        }

        override suspend fun getSubjectPersonsById(id: Long): List<RelatedPerson> {
            return api.getRelatedPersonsBySubjectId(id.toInt())
//            val resp = httpClient.get("$BANGUMI_API_HOST/v0/subjects/${id}/persons")
//
//            if (!resp.status.isSuccess()) {
//                throw IllegalStateException("Failed to get subject persons by id: $resp")
//            }
//            return resp.body()
        }
    }


    @Serializable
    private data class GetEpisodesResp(
        val total: Int,
        val data: List<BangumiEpisode>,
    )

    override val episodes: BangumiClientEpisodes = object : BangumiClientEpisodes {
        override suspend fun getEpisodes(
            subjectId: Long,
            type: BangumiEpType,
            limit: Int?,
            offset: Int?,
        ): Paged<BangumiEpisode> {
            val resp = httpClient.get("$BANGUMI_API_HOST/v0/episodes") {
                parameter("subject_id", subjectId)
                parameter("type", type.id)
                parameter("limit", limit)
                parameter("offset", offset)
            }

            if (!resp.status.isSuccess()) {
                throw IllegalStateException("Failed to get episodes: $resp")
            }

            return resp.body<GetEpisodesResp>().run {
                Paged(total, (offset ?: 0) + data.size < total, data)
            }
        }
    }

    override fun close() {
        httpClient.close()
    }

    companion object {
        fun getSubjectImageUrl(id: Long, size: BangumiSubjectImageSize): String {
            return "$BANGUMI_API_HOST/v0/subjects/${id}/image?type=${size.id.lowercase()}"
        }
    }
}
