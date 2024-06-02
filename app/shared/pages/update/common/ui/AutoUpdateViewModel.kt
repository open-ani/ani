package me.him188.ani.app.update.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.data.update.UpdateManager
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.ContextMP
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.platform.currentPlatform
import me.him188.ani.app.platform.isAndroid
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.update.DefaultFileDownloader
import me.him188.ani.app.update.FileDownloaderState
import me.him188.ani.utils.logging.info
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext

/**
 * 主页使用的自动更新检查
 */
@Stable
class AutoUpdateViewModel : AbstractViewModel(), KoinComponent {
    private val settingsRepository: SettingsRepository by inject()
    private val updateSettings = settingsRepository.updateSettings.flow
    private val updateManager: UpdateManager by inject()

    private val updateChecker: UpdateChecker by lazy { UpdateChecker() }
    private val fileDownloader by lazy { DefaultFileDownloader() }

    /**
     * 新版本下载进度
     */
    private val fileDownloaderPresentation = FileDownloaderPresentation(fileDownloader, backgroundScope)

    /**
     * 最新的版本. 当 [checked] 为 `true` 时, `null` 表示没有新版本. 否则表示还没有检查过.
     */
    var latestVersion: NewVersion? by mutableStateOf(null)
    val currentVersion get() = currentAniBuildConfig.versionName

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

    val hasUpdate by derivedStateOf {
        logoState is UpdateLogoState.HasNewVersion
    }

    // 一小时内只会检查一次
    fun startAutomaticCheckLatestVersion() {
        if (autoCheckTasker.isRunning) {
            return
        } else {
            if (System.currentTimeMillis() - lastCheckTime < 1000 * 60 * 60 * 1) {
                return // 1 小时内检查过
            }

            startCheckLatestVersion(null)
        }
    }

    /**
     * @param context 为 null 则不会自动下载
     */
    fun startCheckLatestVersion(
        context: ContextMP?
    ) {
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

            if (context != null && ver != null && updateSettings.autoDownloadUpdate) {
                logger.info { "autoDownloadUpdate is true, starting download" }
                startDownload(ver, context)
            }
        }
    }

    private val autoDownloadTasker = MonoTasker(backgroundScope)
    fun startDownload(ver: NewVersion, context: ContextMP) {
        if (currentPlatform.isAndroid()) {
            GlobalContext.get().get<BrowserNavigator>().openBrowser(
                context,
                ver.downloadUrlAlternatives.first()
            )
            return
        }

        autoDownloadTasker.launch {
            val dir = updateManager.saveDir.resolve("download")
            if (dir.exists()) {
                // 删除旧的文件
                val allowedFilenames = ver.downloadUrlAlternatives.map {
                    it.substringAfterLast("/", "")
                }
                for (file in dir.listFiles().orEmpty()) {
                    if (allowedFilenames.none { file.name.contains(it) }) {
                        logger.info { "Deleting old installer: $file" }
                        updateManager.deleteInstalled(file, currentVersion)
                    }
                }
            }

            withContext(Dispatchers.IO) { dir.mkdirs() }
            fileDownloader.download(
                alternativeUrls = ver.downloadUrlAlternatives,
                filenameProvider = { it.substringAfterLast("/", "") },
                saveDir = dir
            )
        }
    }

    fun restartDownload(context: ContextMP) {
        latestVersion?.let { startDownload(it, context) }
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