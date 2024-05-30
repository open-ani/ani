package me.him188.ani.app.ui.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.appendPathSegments
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.platform.currentPlatform
import me.him188.ani.app.tools.TimeFormatter
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.widgets.RichDialogLayout
import me.him188.ani.app.ui.profile.update.Release
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import org.koin.core.context.GlobalContext
import java.time.Instant

@Stable
class UpdateCheckerState : AbstractViewModel() {
//    private val logger = logger(UpdateCheckerState::class)

    var latestVersion: NewVersion? by mutableStateOf(null)
    val currentVersion = currentAniBuildConfig.versionName

    val hasUpdate by derivedStateOf {
        this.latestVersion != null && this.latestVersion?.name != this.currentVersion
    }

    private var lastCheckTime: Long = 0L

    private var job: Job? = null
    fun startCheckLatestVersion() {
        if (job?.isActive == true) {
            return // 有一个检查正在进行中
        } else {
            if (System.currentTimeMillis() - lastCheckTime < 1000 * 60 * 60 * 1) {
                return // 1 小时内检查过
            }

            job = launchInBackground {
                val ver = checkLatestVersion()
                withContext(Dispatchers.Main) { latestVersion = ver }
            }
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private suspend fun checkLatestVersion(): NewVersion? {
        HttpClient {
            expectSuccess = true
        }.use { client ->
            return kotlin.runCatching {
                client.getVersionFromAniServer("https://danmaku-global.myani.org/").also {
                    logger.info { "Got latest version from global server: ${it?.name}" }
                }
            }.recoverCatching {
                client.getVersionFromAniServer("https://danmaku-cn.myani.org/").also {
                    logger.info { "Got latest version from CN server: ${it?.name}" }
                }
            }.recoverCatching {
                val body = client.get("https://api.github.com/repos/him188/Ani/releases/latest").bodyAsText()
                val release = json.decodeFromString(Release.serializer(), body)
                val tag = release.tagName

                val distributionSuffix = getDistributionSuffix()
                val version = tag.substringAfter("v")
                val publishedAt = kotlin.runCatching {
                    TimeFormatter().format(
                        Instant.parse(release.publishedAt).toEpochMilli(),
                    )
                }.getOrElse { release.publishedAt }
                NewVersion(
                    name = version,
                    changelogs = listOf(
                        Changelog(
                            version,
                            publishedAt,
                            release.body.substringBeforeLast("### 下载").substringBeforeLast("----").trim()
                        )
                    ),
                    apkUrl = release.assets.firstOrNull {
                        it.name.endsWith(distributionSuffix)
                    }?.browserDownloadUrl ?: "",
                    publishedAt = publishedAt
                ).also {
                    lastCheckTime = System.currentTimeMillis()
                }.also {
                    logger.info { "Got latest version from Github: ${it.name}" }
                }
            }.onFailure {
                logger.error(it) { "Failed to get latest version" }
                return null
            }.getOrNull()
        }
    }

    private fun formatTime(
        seconds: Long,
    ): String = kotlin.runCatching { TimeFormatter().format(seconds * 1000) }.getOrElse { seconds.toString() }

    private suspend fun HttpClient.getVersionFromAniServer(baseUrl: String): NewVersion? {
        @Serializable
        class UpdatesIncrementalResponse(
            val versions: List<String>,
        )

        @Serializable
        class Update(
            val version: String,
            val downloadUrl: String,
            @SerialName("publishTime") val publishTimeSeconds: Long,
            val description: String,
        )

        @Serializable
        class UpdatesIncrementalDetailsResponse(
            val updates: List<Update>,
        )

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
            val platform = currentPlatform
            parameter("clientVersion", currentAniBuildConfig.versionName)
            parameter("clientArch", platform.name.lowercase() + "-" + platform.arch.displayName)
            parameter("releaseClass", "rc")
        }.bodyAsChannel().toInputStream().use {
            json.decodeFromStream(UpdatesIncrementalDetailsResponse.serializer(), it)
        }.updates

        if (updates.isEmpty()) {
            return null
        }

        return updates.last().let { latest ->
            NewVersion(
                name = latest.version,
                changelogs = updates.asReversed().asSequence().take(10).filter { it.version != currentVersion }.map {
                    Changelog(it.version, formatTime(it.publishTimeSeconds), it.description)
                }.toList(),
                apkUrl = latest.downloadUrl,
                publishedAt = formatTime(latest.publishTimeSeconds)
            )
        }
    }

    private fun getDistributionSuffix(): String = when (val platform = Platform.currentPlatform) {
        is Platform.Linux -> "debian-${platform.arch.displayName}.deb"
        is Platform.MacOS -> "macos-${platform.arch.displayName}.dmg"
        is Platform.Windows -> "windows-${platform.arch.displayName}.zip"
        Platform.Android -> "apk"
    }
}

private const val RELEASES = "https://github.com/open-ani/ani/releases"

@Composable
fun ChangelogDialog(
    state: UpdateCheckerState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(onDismissRequest, modifier) {
        RichDialogLayout(
            title = {
                Text("有新版本")
            },
            description = {
                Text("Ani 目前处于测试阶段, 建议更新到最新版本以获得最佳体验")
            },
            buttons = {
                val context by rememberUpdatedState(LocalContext.current)
                TextButton(onClick = onDismissRequest) {
                    Text("取消")
                }
                OutlinedButton({ GlobalContext.get().get<BrowserNavigator>().openBrowser(context, RELEASES) }) {
                    Icon(Icons.Rounded.ArrowOutward, null)
                }
                Button({
                    GlobalContext.get().get<BrowserNavigator>().openBrowser(context, state.latestVersion!!.apkUrl)
                }) {
                    Icon(Icons.Rounded.Download, null)
                }
            }
        ) {
            Column(
                Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "当前版本为 ${state.currentVersion}, 最新版本为 ${state.latestVersion?.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                for (changelog in state.latestVersion?.changelogs.orEmpty()) {
                    HorizontalDivider()

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            changelog.version,
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Text(
                            changelog.publishedAt,
                            Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    Text(
                        changelog.changes,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

            }
        }
    }
}

@Composable
fun HasUpdateTag(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.background(MaterialTheme.colorScheme.tertiaryContainer, shape = CircleShape),
    ) {
        Icon(Icons.Rounded.Update, "有新版本", tint = MaterialTheme.colorScheme.onTertiaryContainer)
    }
}

@Immutable
class NewVersion(
    val name: String,
    val changelogs: List<Changelog>,
    val apkUrl: String,
    val publishedAt: String,
)

@Immutable
class Changelog(
    val version: String,
    val publishedAt: String,
    val changes: String
)