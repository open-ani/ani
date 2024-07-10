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
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.plugin
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.bangumi.apis.DefaultApi
import me.him188.ani.datasources.bangumi.client.BangumiClientSubjects
import me.him188.ani.datasources.bangumi.models.BangumiSubject
import me.him188.ani.datasources.bangumi.models.BangumiSubjectType
import me.him188.ani.datasources.bangumi.models.search.BangumiSort
import me.him188.ani.datasources.bangumi.models.subjects.BangumiLegacySubject
import me.him188.ani.datasources.bangumi.models.subjects.BangumiSubjectImageSize
import me.him188.ani.datasources.bangumi.next.apis.SubjectBangumiNextApi
import me.him188.ani.utils.ktor.registerLogging
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.serialization.toJsonArray
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Objects
import kotlin.coroutines.CoroutineContext

interface BangumiClient : Closeable {
    // Bangumi open API: https://github.com/bangumi/api/blob/master/open-api/api.yml

    /*
    {
    "access_token":"YOUR_ACCESS_TOKEN",
    "expires_in":604800,
    "token_type":"Bearer",
    "scope":null,
    "refresh_token":"YOUR_REFRESH_TOKEN"
    "user_id" : USER_ID
    }
     */

    @Serializable
    data class GetAccessTokenResponse(
        @SerialName("expires_in") val expiresIn: Long,
        @SerialName("user_id") val userId: Long,
        @SerialName("access_token") val accessToken: String,
        @SerialName("refresh_token") val refreshToken: String,
    )

    /**
     * 用 OAuth 回调的 code 换 access token 和 refresh token
     */
    suspend fun exchangeTokens(code: String, callbackUrl: String): GetAccessTokenResponse
    suspend fun refreshAccessToken(refreshToken: String, callbackUrl: String): GetAccessTokenResponse

    suspend fun executeGraphQL(query: String): JsonObject

    @Serializable
    data class GetTokenStatusResponse(
        @SerialName("access_token") val accessToken: String,
        @SerialName("client_id") val clientId: String,
        @SerialName("expires") val expires: Long, // timestamp
        @SerialName("user_id") val userId: Int,
    )

    suspend fun getTokenStatus(accessToken: String): GetTokenStatusResponse

    suspend fun deleteSubjectCollection(
        subjectId: Int
    )

    suspend fun testConnection(): ConnectionStatus

    val api: DefaultApi
    val nextApi: SubjectBangumiNextApi

    val subjects: BangumiClientSubjects

    companion object Factory {
        fun create(
            clientId: String,
            clientSecret: String,
            bearerToken: Flow<String?>,
            parentCoroutineContext: CoroutineContext,
            httpClientConfiguration: HttpClientConfig<*>.() -> Unit = {}
        ): BangumiClient =
            BangumiClientImpl(clientId, clientSecret, bearerToken, parentCoroutineContext, httpClientConfiguration)
    }
}

private const val BANGUMI_API_HOST = "https://api.bgm.tv"
private const val BANGUMI_NEXT_API_HOST = "https://next.bgm.tv"
private const val BANGUMI_HOST = "https://bgm.tv"

internal class BangumiClientImpl(
    private val clientId: String,
    private val clientSecret: String,
    private val bearerToken: Flow<String?>,
    parentCoroutineContext: CoroutineContext,
    httpClientConfiguration: HttpClientConfig<*>.() -> Unit = {},
) : BangumiClient {
    private val scope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))

    private val logger = logger(this::class)
    override suspend fun exchangeTokens(code: String, callbackUrl: String): BangumiClient.GetAccessTokenResponse {
        val resp = httpClient.post("$BANGUMI_HOST/oauth/access_token") {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("grant_type", "authorization_code")
                    put("client_id", clientId)
                    put("client_secret", clientSecret)
                    put("code", code)
                    put("redirect_uri", callbackUrl)
                },
            )
        }

        if (!resp.status.isSuccess()) {
            throw IllegalStateException("Failed to get access token: $resp")
        }

        return resp.body<BangumiClient.GetAccessTokenResponse>()
    }

    override suspend fun refreshAccessToken(
        refreshToken: String,
        callbackUrl: String
    ): BangumiClient.GetAccessTokenResponse {
        val resp = httpClient.post("$BANGUMI_HOST/oauth/access_token") {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("grant_type", "refresh_token")
                    put("client_id", clientId)
                    put("client_secret", clientSecret)
                    put("refresh_token", refreshToken)
                    put("redirect_uri", callbackUrl)
                },
            )
        }

        if (!resp.status.isSuccess()) {
            throw IllegalStateException("Failed to get access token: $resp")
        }

        return resp.body<BangumiClient.GetAccessTokenResponse>()
    }

    override suspend fun executeGraphQL(query: String): JsonObject {
        val resp = httpClient.post("$BANGUMI_API_HOST/v0/graphql") {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("query", query)
                },
            )
        }

        if (!resp.status.isSuccess()) {
            throw IllegalStateException("Failed to execute GraphQL query: $resp")
        }

        return resp.body()
    }

    override suspend fun getTokenStatus(accessToken: String): BangumiClient.GetTokenStatusResponse {
        val resp = httpClient.post("$BANGUMI_HOST/oauth/token_status") {
            parameter("access_token", accessToken)
        }

        if (!resp.status.isSuccess()) {
            throw IllegalStateException("Failed to get token status: $resp")
        }

        return resp.body<BangumiClient.GetTokenStatusResponse>()
    }

    override suspend fun deleteSubjectCollection(subjectId: Int) {
        // not implemented
    }

    override suspend fun testConnection(): ConnectionStatus {
        return httpClient.get(BANGUMI_API_HOST).run {
            if (status.isSuccess() || status == HttpStatusCode.NotFound)
                ConnectionStatus.SUCCESS
            else ConnectionStatus.FAILED
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = HttpClient {
        httpClientConfiguration()
        install(HttpRequestRetry) {
            maxRetries = 3
            delayMillis { 2000 }
        }
        install(HttpTimeout)
        install(ContentNegotiation) {
            json(json)
        }
        expectSuccess = true
    }.apply {
        registerLogging(logger)
        plugin(HttpSend).intercept { request ->
            bearerToken.first()?.let {
                request.bearerAuth(it)
            }
            val originalCall = execute(request)
            if (originalCall.response.status.value !in 100..399) {
                execute(request)
            } else {
                originalCall
            }
        }
    }

    override val api = DefaultApi(BANGUMI_API_HOST, httpClient)
    override val nextApi: SubjectBangumiNextApi = SubjectBangumiNextApi(BANGUMI_NEXT_API_HOST, httpClient)

    @Serializable
    private data class SearchSubjectByKeywordsResponse(
        val total: Int,
        val data: List<BangumiSubject>? = null,
    )

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

                    put(
                        "filter",
                        buildJsonObject {
                            types?.let { types -> put("type", types.map { it.value }.toJsonArray()) }
                            ranks?.let { put("rank", it.toJsonArray()) }
                            tags?.let { put("tag", it.toJsonArray()) }
                            airDates?.let { put("air_date", it.toJsonArray()) }
                            ratings?.let { put("rating", it.toJsonArray()) }
                            nsfw?.let { put("nsfw", it) }
                        },
                    )
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
                    data.orEmpty(),
                )
            }
        }

        override suspend fun searchSubjectsByKeywordsWithOldApi(
            keyword: String,
            type: BangumiSubjectType,
            responseGroup: BangumiSubjectImageSize?,
            start: Int?,
            maxResults: Int?
        ): Paged<BangumiLegacySubject> {
            val keywordCoded = withContext(Dispatchers.IO) {
                URLEncoder.encode(keyword, StandardCharsets.UTF_8.name())
            }
            val resp = httpClient.get("$BANGUMI_API_HOST/search/subject".plus("/").plus(keywordCoded)) {
                parameter("type", type.value)
                parameter("responseGroup", responseGroup?.toString())
                if (Objects.nonNull(start)) {
                    parameter("start", start)
                }
                if (Objects.nonNull(maxResults)) parameter("max_results", maxResults)
            }

            if (!resp.status.isSuccess()) {
                throw IllegalStateException("Failed to search subject by keywords with old api: $resp")
            }

            val json = resp.body<JsonObject>()
            return json.run {
                // results: subject total
                val results: Int = json["results"]?.toString()?.toInt() ?: 0
                // code: exception code
                val code: String = json["code"]?.toString() ?: "-1"
                // return empty when code exists and not -1 and is 404
                if ("-1" != code && "404" == code) return Paged.empty()
                val legacySubjectsJson: String = json["list"].toString()
                val legacySubjects: List<BangumiLegacySubject> =
                    this@BangumiClientImpl.json.decodeFromString(legacySubjectsJson)
                Paged(
                    results,
                    results > legacySubjects.size,
                    legacySubjects,
                )
            }
        }

        override fun getSubjectImageUrl(id: Int, size: BangumiSubjectImageSize): String {
            return Companion.getSubjectImageUrl(id, size)
        }
    }

    override fun close() {
        httpClient.close()
        scope.cancel()
    }

    companion object {
        fun getSubjectImageUrl(id: Int, size: BangumiSubjectImageSize): String {
            return "$BANGUMI_API_HOST/v0/subjects/${id}/image?type=${size.id.lowercase()}"
        }
    }
}
