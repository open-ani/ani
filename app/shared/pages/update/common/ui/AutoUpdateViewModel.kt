package me.him188.ani.app.update.ui

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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.data.update.UpdateManager
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.widgets.RichDialogLayout
import me.him188.ani.app.update.DefaultFileDownloader
import me.him188.ani.app.update.FileDownloaderState
import me.him188.ani.utils.logging.info
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext
import java.io.File

/**
 * UI 的"有新版本"标识的状态
 */
@Stable
sealed class UpdateLogoState {
    /**
     * 没有开启自动检查更新, 需要点击检查
     */
    @Immutable
    data object ClickToCheck : UpdateLogoState()

    /**
     * 已经是最新版本
     */
    @Immutable
    data object UpToDate : UpdateLogoState()

    /**
     * 有新版本, 而且没有开启自动下载, 所以要展示一个 "更新" 图标
     */
    @Immutable
    data class HasUpdate(val version: NewVersion) : UpdateLogoState()

    /**
     * 正在下载更新
     */
    @Stable
    data class Downloading(
        val version: NewVersion,
        private val fileDownloaderState: FileDownloaderPresentation,
    ) : UpdateLogoState() {
        val progress: Float get() = fileDownloaderState.progress
    }

    /**
     * 正在下载更新
     */
    @Stable
    data class DownloadFailed(
        val version: NewVersion,
        private val throwable: Throwable,
    ) : UpdateLogoState()

    /**
     * 已经下载完成, 点击安装
     */
    @Immutable
    data class Downloaded(
        val version: NewVersion,
        val file: File,
    ) : UpdateLogoState()
}

@Stable
class AutoUpdateViewModel : AbstractViewModel(), KoinComponent {
    private val settingsRepository: SettingsRepository by inject()
    private val updateSettings = settingsRepository.updateSettings.flow
    private val updateManager: UpdateManager by inject()

    private val updateChecker: UpdateChecker = UpdateChecker()

    private val fileDownloader = DefaultFileDownloader()

    /**
     * 新版本下载进度
     */
    private val fileDownloaderPresentation = FileDownloaderPresentation(fileDownloader, backgroundScope)

    /**
     * 最新的版本. 当 [checked] 为 `true` 时, `null` 表示没有新版本. 否则表示还没有检查过.
     */
    var latestVersion: NewVersion? by mutableStateOf(null)
    val currentVersion = currentAniBuildConfig.versionName

    val hasUpdate by derivedStateOf {
        this.latestVersion != null && this.latestVersion?.name != this.currentVersion
    }

    private var lastCheckTime: Long by mutableLongStateOf(0L)

    /**
     * 是否检查过更新
     */
    val checked by derivedStateOf {
        lastCheckTime != 0L
    }

    private val autoCheckTasker = MonoTasker(backgroundScope)

    val logoState: UpdateLogoState by derivedStateOf {
        val latestVersion = latestVersion
        val fileDownloaderState = fileDownloaderPresentation.state
        when {
            !checked -> UpdateLogoState.ClickToCheck
            latestVersion == null -> UpdateLogoState.UpToDate
            else -> {
                when (fileDownloaderState) {
                    FileDownloaderState.Idle -> UpdateLogoState.HasUpdate(latestVersion)
                    is FileDownloaderState.Failed ->
                        UpdateLogoState.DownloadFailed(latestVersion, fileDownloaderState.throwable)

                    FileDownloaderState.Downloading ->
                        UpdateLogoState.Downloading(latestVersion, fileDownloaderPresentation)

                    is FileDownloaderState.Succeed ->
                        UpdateLogoState.Downloaded(latestVersion, fileDownloaderState.file)
                }
            }
        }
    }

    fun startAutomaticCheckLatestVersion() {
        if (autoCheckTasker.isRunning) {
            return
        } else {
            if (System.currentTimeMillis() - lastCheckTime < 1000 * 60 * 60 * 1) {
                return // 1 小时内检查过
            }

            startCheckLatestVersion()
        }
    }

    fun startCheckLatestVersion() {
        autoCheckTasker.launch {
            val updateSettings = updateSettings.first()

            val ver = try {
                if (!updateSettings.autoCheckUpdate) {
                    logger.info { "autoCheckUpdate disabled" }
                    return@launch
                }
                logger.info { "Checking latest version, updateSettings=${updateSettings}" }

                updateChecker.checkLatestVersion(updateSettings.releaseClass)
            } finally {
                withContext(Dispatchers.Main) {
                    lastCheckTime = System.currentTimeMillis()
                }
            }
            withContext(Dispatchers.Main) { latestVersion = ver }

            if (ver != null && updateSettings.autoDownloadUpdate) {
                logger.info { "autoDownloadUpdate is true, starting download" }
                startDownload(ver)
            }
        }
    }

    private val autoDownloadTasker = MonoTasker(backgroundScope)
    fun startDownload(ver: NewVersion) {
        autoDownloadTasker.launch {
            fileDownloader.download(
                alternativeUrls = ver.downloadUrlAlternatives,
                filenameProvider = { it.substringAfterLast("/", "") },
                saveDir = updateManager.saveDir.resolve("download").apply {
                    withContext(Dispatchers.IO) { mkdirs() }
                }
            )
        }
    }

    fun restartDownload() {
        latestVersion?.let { startDownload(it) }
    }
}

private const val RELEASES = "https://github.com/open-ani/ani/releases"

@Composable
fun ChangelogDialog(
    latestVersion: NewVersion,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    currentVersion: String = currentAniBuildConfig.versionName,
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
                    latestVersion.downloadUrlAlternatives.firstOrNull()
                        ?.let { GlobalContext.get().get<BrowserNavigator>().openBrowser(context, it) }
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
                    "当前版本为 $currentVersion, 最新版本为 ${latestVersion.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                for (changelog in latestVersion.changelogs) {
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
    /**
     * 所有可行的下载地址. 任意一个都可以用
     */
    val downloadUrlAlternatives: List<String>,
    val publishedAt: String,
)

@Immutable
class Changelog(
    val version: String,
    val publishedAt: String,
    val changes: String
)