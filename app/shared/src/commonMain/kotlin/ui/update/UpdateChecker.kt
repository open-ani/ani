package me.him188.ani.app.ui.update

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.appendPathSegments
import io.ktor.utils.io.core.use
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import me.him188.ani.app.data.source.danmaku.protocol.ReleaseClass
import me.him188.ani.app.data.source.danmaku.protocol.ReleaseUpdatesDetailedResponse
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.tools.TimeFormatter
import me.him188.ani.utils.coroutines.withExceptionCollector
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.platform.currentPlatform


class CheckVersionFailedException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception()

class UpdateChecker {
    /**
     * 检查是否有更新的版本. 返回最新版本的信息, 或者 `null` 表示没有新版本.
     */
    @Throws(CheckVersionFailedException::class, kotlin.coroutines.cancellation.CancellationException::class)
    suspend fun checkLatestVersion(
        releaseClass: ReleaseClass,
        currentVersion: String = currentAniBuildConfig.versionName,
    ): NewVersion? {
        HttpClient {
            expectSuccess = true
        }.use { client ->
            withExceptionCollector {
                return kotlin.runCatching {
                    client.getVersionFromAniServer("https://danmaku-global.myani.org/", currentVersion, releaseClass)
                        .also {
                            logger.info { "Got latest version from global server: ${it?.name}" }
                        }
                }.recoverCatching { exception ->
                    collect(exception)
                    client.getVersionFromAniServer("https://danmaku-cn.myani.org/", currentVersion, releaseClass).also {
                        logger.info { "Got latest version from CN server: ${it?.name}" }
                    }
                }.onFailure { exception ->
                    collect(exception)
                    if (exception is CancellationException) throw exception
                    collect(CheckVersionFailedException())
                    val finalException = getLast()!!
                    logger.error(finalException) { "Failed to get latest version" }
                    throw finalException
                }.getOrThrow()// should not throw, because of `onFailure`   
            }
        }
    }

    private suspend fun HttpClient.getVersionFromAniServer(
        baseUrl: String,
        currentVersion: String = currentAniBuildConfig.versionName,
        releaseClass: ReleaseClass,
    ): NewVersion? {
//        val versions = get(baseUrl) {
//            url {
//                appendPathSegments("v1/updates/incremental")
//            }
//            val platform = currentPlatform
//            parameter("clientVersion", currentAniBuildConfig.versionName)
//            parameter("clientArch", platform.arch.displayName)
//            parameter("releaseClass", "beta")
//        }.bodyAsChannel().toInputStream().use {
//            json.decodeFromStream(UpdatesIncrementalResponse.serializer(), it)
//        }.versions
//
//        val newestVersion = versions.lastOrNull() ?: return null
        val updates = get(baseUrl) {
            url {
                appendPathSegments("v1/updates/incremental/details")
            }
            val platform = currentPlatform()
            parameter("clientVersion", currentAniBuildConfig.versionName)
            parameter("clientPlatform", platform.name.lowercase())
            parameter("clientArch", platform.arch.displayName)
            parameter("releaseClass", releaseClass.name)
        }.bodyAsText().let {
            json.decodeFromString(ReleaseUpdatesDetailedResponse.serializer(), it)
        }.updates

        if (updates.isEmpty()) {
            return null
        }

        return updates.last().let { latest ->
            NewVersion(
                name = latest.version,
                changelogs = updates.asReversed().asSequence().take(10).filter { it.version != currentVersion }.map {
                    Changelog(it.version, formatTime(it.publishTime), it.description)
                }.toList(),
                downloadUrlAlternatives = latest.downloadUrlAlternatives,
                publishedAt = formatTime(latest.publishTime),
            )
        }
    }

    private companion object {
        private val logger = logger<UpdateChecker>()
        private val json = Json {
            ignoreUnknownKeys = true
        }

        private fun formatTime(
            seconds: Long,
        ): String = kotlin.runCatching { TimeFormatter().format(seconds * 1000) }.getOrElse { seconds.toString() }
    }
}
