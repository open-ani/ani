package me.him188.ani.app.ui.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.tools.TimeFormatter
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.foundation.widgets.RichDialogLayout
import me.him188.ani.app.ui.profile.update.Release
import me.him188.ani.utils.logging.error
import org.koin.core.context.GlobalContext
import java.time.Instant

@Stable
class UpdateCheckerState : AbstractViewModel() {
//    private val logger = logger(UpdateCheckerState::class)

    var latestVersion: NewVersion? by mutableStateOf(null)
    val currentVersion = currentAniBuildConfig.versionName

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

    suspend fun checkLatestVersion(): NewVersion? {
        return kotlin.runCatching {
            HttpClient().use { client ->
                val json = Json {
                    ignoreUnknownKeys = true
                }
                val body = client.get("https://api.github.com/repos/him188/Ani/releases/latest").bodyAsText()
                val release = json.decodeFromString(Release.serializer(), body)
                val tag = release.tagName

                val distributionSuffix = getDistributionSuffix()
                NewVersion(
                    name = tag.substringAfter("v"),
                    changelog = release.body.substringBeforeLast("### 下载").substringBeforeLast("----").trim(),
                    apkUrl = release.assets.firstOrNull {
                        it.name.endsWith(distributionSuffix)
                    }?.browserDownloadUrl ?: "",
                    publishedAt = kotlin.runCatching {
                        TimeFormatter().format(
                            Instant.parse(release.publishedAt).toEpochMilli()
                        )
                    }.getOrElse { release.publishedAt }
                ).also {
                    lastCheckTime = System.currentTimeMillis()
                }
            }
        }.onFailure {
            logger.error(it) { "Failed to get latest version" }
            return null
        }.getOrNull()
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
fun UpdateCheckerHost(
    state: UpdateCheckerState = rememberViewModel { UpdateCheckerState() }
) {
    SideEffect {
        state.startCheckLatestVersion()
    }

    var showDialog by rememberSaveable { mutableStateOf(false) }

    if (showDialog) {
        BasicAlertDialog({ showDialog = false }) {
            RichDialogLayout(
                title = {
                    Text("有新版本")
                },
                description = {
                    Text("Ani 目前处于测试阶段, 建议更新到最新版本以获得最佳体验")
                },
                buttons = {
                    val context by rememberUpdatedState(LocalContext.current)
                    TextButton(onClick = { showDialog = false }) {
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

                    Text(
                        "发布时间: " + state.latestVersion?.publishedAt.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Text(
                        "更新内容: ",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Text(
                        state.latestVersion?.changelog.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }

    if (state.latestVersion != null && state.latestVersion?.name != state.currentVersion) {
        HasUpdateTag(onClick = { showDialog = true })
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
    val changelog: String,
    val apkUrl: String,
    val publishedAt: String,
)