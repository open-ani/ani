package me.him188.ani.danmaku.server.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import me.him188.ani.danmaku.server.util.exception.InvalidClientVersionException
import me.him188.ani.danmaku.server.util.semver.SemVersion
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

interface ClientReleaseInfoManager {
    suspend fun getLatestReleaseInfo(): ReleaseInfo
    suspend fun getNewerReleasesInfo(version: String): List<ReleaseInfo>
}

class ClientReleaseInfoManagerImpl(
    private val bufferExpirationTime: Long = 1.minutes.inWholeMilliseconds,
) : ClientReleaseInfoManager {
    private var buffer: List<ReleaseInfo> = listOf()
    private var lastRecordTime: Long = 0
    private val bufferMutex = Mutex()
    private val bufferExpired get() = System.currentTimeMillis() - lastRecordTime > bufferExpirationTime
    
    override suspend fun getLatestReleaseInfo(): ReleaseInfo {
        return getBuffer().last()
    }
    
    override suspend fun getNewerReleasesInfo(version: String): List<ReleaseInfo> {
        val semVersion = try {
            SemVersion.invoke(version)
        } catch (e: IllegalArgumentException) {
            throw InvalidClientVersionException()
        } catch (e: NumberFormatException) {
            throw InvalidClientVersionException()
        }
        
        return getBuffer().takeLastWhile { it.version > semVersion }
    }
    
    private suspend fun getBuffer(): List<ReleaseInfo> {
        if (bufferExpired) {
            bufferMutex.withLock {
                if (bufferExpired) {
                    buffer = getReleaseInfoFromGithub()
                    lastRecordTime = System.currentTimeMillis()
                }
            }
        }
        return buffer
    }

    private val githubReleasesUrl = "https://api.github.com/repos/open-ani/ani/releases"
    private val httpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                Json {
                    ignoreUnknownKeys = true
                }
            }
        }
    }

    private suspend fun getReleaseInfoFromGithub(): List<ReleaseInfo> {
        val response = tryUntilSuccess(timeLimit = 10.seconds.inWholeMilliseconds) {
            httpClient.get(githubReleasesUrl) {
                contentType(ContentType.Application.Json)
            }.also {
                if (it.status != HttpStatusCode.OK) {
                    throw RuntimeException("Failed to get release info from Github")
                }
            }
        }
        val content = Json.decodeFromString<JsonElement>(response.body())
        return parseGithubReleaseInfo(content)
    }

    private fun parseGithubReleaseInfo(content: JsonElement): List<ReleaseInfo> {
        return content.jsonArray.mapNotNull { release ->
            try {
                ReleaseInfo(
                    version = release.jsonObject["tag_name"]!!.jsonPrimitive.content.let {
                        SemVersion.invoke(it.removePrefix("v"))
                    },
                    htmlUrl = release.jsonObject["html_url"]!!.jsonPrimitive.content,
                    publishTime = release.jsonObject["published_at"]!!.jsonPrimitive.content.let {
                        ZonedDateTime.parse(it).toEpochSecond()
                    },
                    description = release.jsonObject["body"]!!.jsonPrimitive.content,
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.version }
    }
}

@Serializable
data class ReleaseInfo(
    val version: SemVersion,
    val htmlUrl: String,
    val publishTime: Long,
    val description: String,
)

suspend fun <T> tryUntilSuccess(timeLimit: Long, block: suspend () -> T): T {
    val startTime = System.currentTimeMillis()
    while (true) {
        try {
            return block()
        } catch (e: Exception) {
            if (System.currentTimeMillis() - startTime > timeLimit) {
                throw e
            } // else ignore and retry
        }
    }
}
