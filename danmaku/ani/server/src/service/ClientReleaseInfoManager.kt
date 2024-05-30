package me.him188.ani.danmaku.server.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.Platform
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.him188.ani.danmaku.protocol.ReleaseClass
import me.him188.ani.danmaku.server.util.exception.InvalidClientVersionException
import me.him188.ani.danmaku.server.util.semver.SemVersion
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

interface ClientReleaseInfoManager {
    suspend fun getLatestRelease(
        clientArch: String,
        releaseClass: ReleaseClass,
    ): ReleaseInfo?

    suspend fun getAllUpdateLogs(
        version: String,
        clientArch: String,
        releaseClass: ReleaseClass,
    ): List<ReleaseInfo>

    fun getCloudflareDownloadUrl(clientVersion: SemVersion, clientArch: String): String
}

@Serializable
data class ReleaseInfo(
    val version: SemVersion,
    val htmlUrl: String,
    val assetNames: Set<String>,
    val publishTime: Long,
    val description: String,
)

class ClientReleaseInfoManagerImpl(
    private val bufferExpirationTime: Long = 1.minutes.inWholeMilliseconds,
) : ClientReleaseInfoManager {
    private var buffer: List<ReleaseInfo> = listOf()
    private var lastRecordTime: Long = 0
    private val bufferMutex = Mutex()
    private val bufferExpired get() = System.currentTimeMillis() - lastRecordTime > bufferExpirationTime

    override suspend fun getLatestRelease(clientArch: String, releaseClass: ReleaseClass): ReleaseInfo? {
        return getLatestReleaseInternal(getBuffer(), clientArch, releaseClass)
    }

    private fun getLatestReleaseInternal(
        releaseInfoList: List<ReleaseInfo>,
        clientArch: String,
        releaseClass: ReleaseClass
    ): ReleaseInfo? {
        return releaseInfoList.lastOrNull { info ->
            /**
             * The class of target release version should be more stable than or equal to the required release class.
             * For example, if client requires the newest [ReleaseClass.BETA] version,
             * the target release version could be either [ReleaseClass.BETA], [ReleaseClass.RC] or [ReleaseClass.STABLE].
             * On the other hand, if client requires the newest [ReleaseClass.STABLE] version,
             * the target release version should only be [ReleaseClass.STABLE].
             */
            info.version.parseClass().moreStableThan(releaseClass) && info.assetNames.any {
                if (clientArch == "android") {
                    it.endsWith(".apk")
                } else {
                    it.contains(clientArch)
                }
            }
        }
    }

    override suspend fun getAllUpdateLogs(
        version: String,
        clientArch: String,
        releaseClass: ReleaseClass
    ): List<ReleaseInfo> {
        val semVersion = try {
            SemVersion.invoke(version)
        } catch (e: IllegalArgumentException) {
            throw InvalidClientVersionException()
        } catch (e: NumberFormatException) {
            throw InvalidClientVersionException()
        }

        val buffer = getBuffer()
        val latestRelease = getLatestReleaseInternal(buffer, clientArch, releaseClass) ?: return listOf()
        return buffer.dropWhile { it.version <= semVersion }.takeWhile { it.version <= latestRelease.version }
    }

    override fun getCloudflareDownloadUrl(clientVersion: SemVersion, clientArch: String): String {
        val distributionSuffix = when {
            clientArch.startsWith("debian") -> "-$clientArch.deb"
            clientArch.startsWith("macos") -> "-$clientArch.dmg"
            clientArch.startsWith("windows") -> "-$clientArch.zip"
            clientArch.startsWith("android") -> ".apk"
            else -> throw IllegalArgumentException("Unknown client arch: $clientArch")
        }
        return "https://d.myani.org/v${clientVersion}/ani-${clientVersion}${distributionSuffix}"
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
                    assetNames = release.jsonObject["assets"]!!.jsonArray.map {
                        it.jsonObject["name"]!!.jsonPrimitive.content
                    }.toSet(),
                    htmlUrl = release.jsonObject["html_url"]!!.jsonPrimitive.content,
                    publishTime = release.jsonObject["published_at"]!!.jsonPrimitive.content.let {
                        ZonedDateTime.parse(it).toEpochSecond()
                    },
                    description = parseDescription(release.jsonObject["body"]!!.jsonPrimitive.content),
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.version }
    }

    private fun parseDescription(description: String): String {
        return description.substringBeforeLast("### 下载").substringBeforeLast("----").trim()
    }
}

private fun SemVersion.parseClass(): ReleaseClass {
    val identifier = this.identifier
    return when {
        identifier == null -> ReleaseClass.STABLE
        identifier.lowercase(Locale.ROOT).startsWith("alpha") -> ReleaseClass.ALPHA
        identifier.lowercase(Locale.ROOT).startsWith("beta") -> ReleaseClass.BETA
        identifier.lowercase(Locale.ROOT).startsWith("rc") -> ReleaseClass.RC
        else -> throw IllegalArgumentException("Unknown release class: $identifier")
    }
}

private suspend fun <T> tryUntilSuccess(timeLimit: Long, block: suspend () -> T): T {
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