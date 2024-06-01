package me.him188.ani.app.ui.settings.tabs.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.HdrAuto
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.him188.ani.app.data.models.ThemeKind
import me.him188.ani.app.data.models.UISettings
import me.him188.ani.app.data.models.UpdateSettings
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.platform.isDesktop
import me.him188.ani.app.ui.collection.progress.EpisodeProgressTheme
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.framework.AbstractSettingsViewModel
import me.him188.ani.app.ui.settings.framework.Tester
import me.him188.ani.app.ui.settings.framework.components.DropdownItem
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import me.him188.ani.app.ui.settings.framework.components.TextButtonItem
import me.him188.ani.app.ui.settings.framework.components.TextItem
import me.him188.ani.app.ui.update.ChangelogDialog
import me.him188.ani.app.ui.update.NewVersion
import me.him188.ani.app.ui.update.UpdateCheckerState
import me.him188.ani.danmaku.protocol.ReleaseClass
import org.koin.core.component.inject
import java.util.Locale


sealed class CheckVersionResult {
    data class HasNewVersion(
        val newVersion: NewVersion,
    ) : CheckVersionResult()

    object UpToDate : CheckVersionResult()
    data class Failed(
        val throwable: Throwable,
    ) : CheckVersionResult()
}

@Stable
class AppSettingsViewModel : AbstractSettingsViewModel() {
    private val settingsRepository: SettingsRepository by inject()

    val uiSettings by settings(
        settingsRepository.uiSettings,
        UISettings(_placeholder = -1)
    )
    val updateSettings by settings(
        settingsRepository.updateSettings,
        UpdateSettings(_placeholder = -1)
    )

    val updateChecker = SingleTester(
        Tester(
            "new",
            onTest = {
                UpdateCheckerState().use {
                    val v = it.checkLatestVersion()
                    if (v == null) {
                        CheckVersionResult.UpToDate
                    } else {
                        CheckVersionResult.HasNewVersion(v)
                    }
                }
            },
            onError = { CheckVersionResult.Failed(it) }
        ),
        backgroundScope
    )
}

@Composable
fun AppSettingsTab(
    vm: AppSettingsViewModel = rememberViewModel<AppSettingsViewModel> { AppSettingsViewModel() },
    modifier: Modifier = Modifier
) {
    val uiSettings by vm.uiSettings
    SettingsTab(modifier) {
        Group(title = { Text("软件更新") }) {
            val version = currentAniBuildConfig.versionName
            TextItem(
                title = {
                    Text(version)
                },
                description = { Text("当前版本") },
                icon = {
                    val releaseClass = remember {
                        guessReleaseClass(version)
                    }

                    when (releaseClass) {
                        ReleaseClass.ALPHA -> Icon(Icons.Rounded.RocketLaunch, null)
                        ReleaseClass.BETA -> Icon(Icons.Rounded.Science, null)
                        ReleaseClass.RC,
                        ReleaseClass.STABLE -> Icon(Icons.Rounded.Verified, null)
                    }
                }
            )
            HorizontalDividerItem()
            val updateSettings by vm.updateSettings
            SwitchItem(
                updateSettings.autoCheckUpdate,
                onCheckedChange = {
                    vm.updateSettings.update(updateSettings.copy(autoCheckUpdate = !updateSettings.autoCheckUpdate))
                },
                title = { Text("自动检查更新") },
                description = { Text("只会在首页提示有更新，不会自动下载") },
                modifier = Modifier.placeholder(vm.updateSettings.loading)
            )
            HorizontalDividerItem()
            DropdownItem(
                selected = { updateSettings.releaseClass },
                values = { ReleaseClass.enabledEntries },
                itemText = {
                    when (it) {
                        ReleaseClass.ALPHA -> Text("每日构建 (最早体验新功能)")
                        ReleaseClass.BETA -> Text("测试版 (兼顾新功能和一定稳定性)")
                        ReleaseClass.RC, // RC 实际上不会有
                        ReleaseClass.STABLE -> Text("正式版 (最稳定)")
                    }
                },
                exposedItemText = {
                    when (it) {
                        ReleaseClass.ALPHA -> Text("每日构建")
                        ReleaseClass.BETA -> Text("测试版")
                        ReleaseClass.RC, // RC 实际上不会有
                        ReleaseClass.STABLE -> Text("正式版")
                    }
                },
                onSelect = {
                    vm.updateSettings.update(updateSettings.copy(releaseClass = it))
                },
                modifier = Modifier.placeholder(vm.updateSettings.loading),
                itemIcon = {
                    when (it) {
                        ReleaseClass.ALPHA -> Icon(Icons.Rounded.RocketLaunch, null)
                        ReleaseClass.BETA -> Icon(Icons.Rounded.Science, null)
                        ReleaseClass.RC,
                        ReleaseClass.STABLE -> Icon(Icons.Rounded.Verified, null)
                    }
                },
                title = { Text("更新类型") },
            )
            HorizontalDividerItem()
            var showUpdatePopup by remember { mutableStateOf(false) }
            if (showUpdatePopup) {
                (vm.updateChecker.tester.result as? CheckVersionResult.HasNewVersion)?.let {
                    ChangelogDialog(
                        latestVersion = it.newVersion,
                        onDismissRequest = { showUpdatePopup = false },
                    )
                }
            }
            TextButtonItem(
                title = {
                    if (vm.updateChecker.tester.isTesting) {
                        Text("检查中...")
                        return@TextButtonItem
                    }
                    when (val result = vm.updateChecker.tester.result) {
                        is CheckVersionResult.Failed -> Text("检查失败")
                        is CheckVersionResult.UpToDate -> Text("已是最新")
                        is CheckVersionResult.HasNewVersion -> Text(remember(result.newVersion.name) { "有新版本: ${result.newVersion.name}" })
                        null -> Text("检查更新")
                    }
                },
                onClick = {
                    if (vm.updateChecker.tester.isTesting) {
                        vm.updateChecker.cancel()
                        return@TextButtonItem
                    }
                    when (vm.updateChecker.tester.result) {
                        is CheckVersionResult.HasNewVersion -> showUpdatePopup = true
                        is CheckVersionResult.Failed,
                        is CheckVersionResult.UpToDate,
                        null -> vm.updateChecker.testAll()
                    }
                },
                modifier = Modifier.placeholder(vm.updateSettings.loading),
            )
        }

        if (Platform.currentPlatform.isDesktop()) {
            Group(title = { Text("通用") }) {
                val theme by remember { derivedStateOf { uiSettings.theme } }
                DropdownItem(
                    selected = { theme.kind },
                    values = { ThemeKind.entries },
                    itemText = {
                        when (it) {
                            ThemeKind.AUTO -> Text("自动")
                            ThemeKind.LIGHT -> Text("浅色")
                            ThemeKind.DARK -> Text("深色")
                        }
                    },
                    onSelect = {
                        vm.uiSettings.update(
                            uiSettings.copy(
                                theme = uiSettings.theme.copy(kind = it)
                            )
                        )
                    },
                    modifier = Modifier.placeholder(vm.uiSettings.loading),
                    itemIcon = {
                        when (it) {
                            ThemeKind.AUTO -> Icon(Icons.Rounded.HdrAuto, null)
                            ThemeKind.LIGHT -> Icon(Icons.Rounded.LightMode, null)
                            ThemeKind.DARK -> Icon(Icons.Rounded.DarkMode, null)
                        }
                    },
                    description = {
                        if (theme.kind == ThemeKind.AUTO) {
                            Text("根据系统设置自动切换")
                        }
                    },
                    title = { Text("主题") },
                )
            }
        }
        Group(title = { Text("我的追番") }) {
            val myCollections by remember { derivedStateOf { uiSettings.myCollections } }
            SwitchItem(
                checked = myCollections.enableListAnimation,
                onCheckedChange = {
                    vm.uiSettings.update(
                        uiSettings.copy(
                            myCollections = myCollections.copy(
                                enableListAnimation = !myCollections.enableListAnimation
                            )
                        )
                    )
                },
                title = { Text("列表滚动动画") },
                Modifier.placeholder(vm.uiSettings.loading),
                description = { Text("如遇到显示重叠问题，可尝试关闭") },
            )
        }
        Group(title = { Text("选集播放") }) {
            val episode by remember { derivedStateOf { uiSettings.episodeProgress } }
            SwitchItem(
                checked = episode.theme == EpisodeProgressTheme.LIGHT_UP,
                onCheckedChange = {
                    vm.uiSettings.update(
                        uiSettings.copy(
                            episodeProgress = episode.copy(
                                theme = if (it) EpisodeProgressTheme.LIGHT_UP else EpisodeProgressTheme.ACTION
                            )
                        )
                    )
                },
                title = { Text("点亮模式") },
                Modifier.placeholder(vm.uiSettings.loading),
                description = { Text("高亮已经看过的剧集，而不是将要看的剧集") },
            )
        }
        AppSettingsTabPlatform(vm)
    }
}

@Stable
private fun guessReleaseClass(version: String): ReleaseClass {
    val metadata = version
        .substringAfter("-", "")
        .lowercase(Locale.ENGLISH)
    return when {
        metadata.isEmpty() -> ReleaseClass.STABLE
        "alpha" in metadata || "dev" in metadata -> ReleaseClass.ALPHA
        "beta" in metadata -> ReleaseClass.BETA
        "rc" in metadata -> ReleaseClass.RC
        else -> ReleaseClass.STABLE
    }
}

@Composable
internal expect fun SettingsScope.AppSettingsTabPlatform(vm: AppSettingsViewModel)
