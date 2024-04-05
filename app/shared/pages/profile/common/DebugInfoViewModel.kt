package me.him188.ani.app.ui.profile

import androidx.compose.runtime.Immutable
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.tools.TimeFormatter
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.profile.update.Release
import me.him188.ani.utils.logging.error
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Instant

class DebugInfoViewModel : AbstractViewModel(), KoinComponent {
    private val sessionManager: SessionManager by inject()
    val browserNavigator: BrowserNavigator by inject()

    val debugInfo = debugInfoFlow().shareInBackground(started = SharingStarted.Eagerly)

    val currentVersion = currentAniBuildConfig.versionName

    private fun debugInfoFlow() = combine(
        sessionManager.session,
        sessionManager.processingRequest.flatMapLatest { it?.state ?: flowOf(null) },
        sessionManager.isSessionValid,
    ) { session, processingRequest, isSessionValid ->
        DebugInfo(properties = buildMap {
            val buildConfig = currentAniBuildConfig
            put("isDebug", buildConfig.isDebug.toString())
            if (buildConfig.isDebug) {
                put("accessToken", session?.accessToken)
            }
            put("processingRequest.state", processingRequest.toString())
            put("sessionManager.isSessionValid", isSessionValid.toString())
        })
    }

    suspend fun getLatestVersionOrNull(): NewVersion? {
        return kotlin.runCatching {
            HttpClient().use { client ->
                val json = Json {
                    ignoreUnknownKeys = true
                }
                val body = client.get("https://api.github.com/repos/him188/Ani/releases/latest").bodyAsText()
                val release = json.decodeFromString(Release.serializer(), body)
                val tag = release.tagName

                NewVersion(
                    name = tag.substringAfter("v"),
                    changelog = release.body.substringBeforeLast("----").trim(),
                    apkUrl = release.assets.firstOrNull { it.name.endsWith(".apk") }?.browserDownloadUrl ?: "",
                    publishedAt = kotlin.runCatching {
                        TimeFormatter().format(
                            Instant.parse(release.publishedAt).toEpochMilli()
                        )
                    }.getOrElse { release.publishedAt }
                )
            }
        }.onFailure {
            logger.error(it) { "Failed to get latest version" }
            return null
        }.getOrNull()
    }
}

@Immutable
class NewVersion(
    val name: String,
    val changelog: String,
    val apkUrl: String,
    val publishedAt: String,
)