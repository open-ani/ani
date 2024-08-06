package me.him188.ani.app.ui.settings.tabs.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.him188.ani.app.data.models.preference.FullscreenSwitchMode
import me.him188.ani.app.data.models.preference.ThemeKind
import me.him188.ani.app.data.models.preference.UISettings
import me.him188.ani.app.data.models.preference.UpdateSettings
import me.him188.ani.app.data.models.preference.VideoScaffoldConfig
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.data.source.danmaku.protocol.ReleaseClass
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.platform.currentPlatform
import me.him188.ani.app.platform.isDesktop
import me.him188.ani.app.platform.isMobile
import me.him188.ani.app.tools.update.supportsInAppUpdate
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.framework.AbstractSettingsViewModel
import me.him188.ani.app.ui.settings.framework.Tester
import me.him188.ani.app.ui.settings.framework.components.DropdownItem
import me.him188.ani.app.ui.settings.framework.components.RowButtonItem
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import me.him188.ani.app.ui.settings.framework.components.TextButtonItem
import me.him188.ani.app.ui.settings.framework.components.TextItem
import me.him188.ani.app.ui.subject.episode.list.EpisodeListProgressTheme
import me.him188.ani.app.ui.update.AutoUpdateViewModel
import me.him188.ani.app.ui.update.ChangelogDialog
import me.him188.ani.app.ui.update.NewVersion
import me.him188.ani.app.ui.update.TextButtonUpdateLogo
import me.him188.ani.app.ui.update.UpdateChecker
import org.koin.core.component.inject
import org.koin.mp.KoinPlatform


sealed class CheckVersionResult {
    data class HasNewVersion(
        val newVersion: NewVersion,
    ) : CheckVersionResult()

    data object UpToDate : CheckVersionResult()
    data class Failed(
        val throwable: Throwable,
    ) : CheckVersionResult()
}

@Stable
class AppSettingsViewModel : AbstractSettingsViewModel() {
    private val settingsRepository: SettingsRepository by inject()

    val uiSettings by settings(
        settingsRepository.uiSettings,
        UISettings(_placeholder = -1),
    )
    val updateSettings by settings(
        settingsRepository.updateSettings,
        UpdateSettings(_placeholder = -1),
    )
    val videoScaffoldConfig by settings(
        settingsRepository.videoScaffoldConfig,
        VideoScaffoldConfig(_placeholder = -1),
    )

    /**
     * 检查更新, 与 [AutoUpdateViewModel] 不同的是可以更好地在设置中展示状态
     */
    val updateCheckerTester = SingleTester(
        Tester(
            "new",
            onTest = {
                UpdateChecker().let { checker ->
                    val v = checker.checkLatestVersion(
                        updateSettings.value.releaseClass,
                        currentAniBuildConfig.versionName,
                    )
                    if (v == null) {
                        CheckVersionResult.UpToDate
                    } else {
                        CheckVersionResult.HasNewVersion(v)
                    }
                }
            },
            onError = { CheckVersionResult.Failed(it) },
        ),
        backgroundScope,
    )
}

@Composable
fun AppSettingsTab(
    vm: AppSettingsViewModel = rememberViewModel<AppSettingsViewModel> { AppSettingsViewModel() },
    modifier: Modifier = Modifier
) {
    SettingsTab(modifier) {
        Group(title = { Text("软件更新") }) {
            val version = currentAniBuildConfig.versionName
            TextItem(
                description = { Text("当前版本") },
                icon = {
                    val releaseClass = remember {
                        guessReleaseClass(version)
                    }

                    ReleaseClassIcon(releaseClass)
                },
                title = {
                    Text(version)
                },
            )
            HorizontalDividerItem()
            val context by rememberUpdatedState(LocalContext.current)
            RowButtonItem(
                onClick = {
                    KoinPlatform.getKoin().get<BrowserNavigator>().openBrowser(
                        context,
                        "https://github.com/open-ani/ani/releases/tag/v${currentAniBuildConfig.versionName}",
                    )
//                    vm.updateCheckerTester.tester.result?.let {
//                        if (it is CheckVersionResult.HasNewVersion) {
//                            ChangelogDialog(
//                                latestVersion = it.newVersion,
//                                onDismissRequest = {},
//                                onStartDownload = {}
//                            )
//                        }
//                    }
                },
                icon = { Icon(Icons.Rounded.ArrowOutward, null) },
            ) { Text("查看更新日志") }
            HorizontalDividerItem()
            val updateSettings by vm.updateSettings
            SwitchItem(
                updateSettings.autoCheckUpdate,
                onCheckedChange = {
                    vm.updateSettings.update(updateSettings.copy(autoCheckUpdate = !updateSettings.autoCheckUpdate))
                },
                title = { Text("自动检查更新") },
                description = { Text("只会显示一个更新图标，不会自动下载") },
                modifier = Modifier.placeholder(vm.updateSettings.loading),
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
                    ReleaseClassIcon(it)
                },
                title = { Text("更新类型") },
            )
            HorizontalDividerItem()
            SwitchItem(
                updateSettings.inAppDownload,
                { vm.updateSettings.update(updateSettings.copy(inAppDownload = it)) },
                title = { Text("应用内下载") },
                description = {
                    if (updateSettings.inAppDownload) {
                        Text("省去跳转浏览器步骤")
                    } else {
                        Text("已关闭，将会跳转到外部浏览器完成下载")
                    }
                },
                enabled = updateSettings.autoCheckUpdate,
            )
            if (currentPlatform.supportsInAppUpdate) {
                AnimatedVisibility(updateSettings.inAppDownload) {
                    HorizontalDividerItem()
                    SwitchItem(
                        updateSettings.autoDownloadUpdate,
                        { vm.updateSettings.update(updateSettings.copy(autoDownloadUpdate = it)) },
                        title = { Text("自动下载更新") },
                        description = { Text("下载完成后会提示，确认后才会安装") },
                        enabled = updateSettings.autoCheckUpdate,
                    )
                }
            }
            HorizontalDividerItem()
            var showUpdatePopup by remember { mutableStateOf(false) }
            val autoUpdate: AutoUpdateViewModel = rememberViewModel { AutoUpdateViewModel() }
            if (showUpdatePopup) {
                (vm.updateCheckerTester.tester.result as? CheckVersionResult.HasNewVersion)?.let {
                    ChangelogDialog(
                        latestVersion = it.newVersion,
                        onDismissRequest = { showUpdatePopup = false },
                        onStartDownload = { autoUpdate.startDownload(it.newVersion, context) },
                    )
                }
            }
            TextButtonItem(
                onClick = {
                    if (vm.updateCheckerTester.tester.isTesting) {
                        vm.updateCheckerTester.cancel()
                        return@TextButtonItem
                    }
                    when (vm.updateCheckerTester.tester.result) {
                        is CheckVersionResult.HasNewVersion -> showUpdatePopup = true
                        is CheckVersionResult.Failed,
                        is CheckVersionResult.UpToDate,
                        null -> {
                            vm.updateCheckerTester.testAll()
                            autoUpdate.startCheckLatestVersion(context)
                        }
                    }
                },
                modifier = Modifier.placeholder(vm.updateSettings.loading),
                title = {
                    if (vm.updateCheckerTester.tester.isTesting) {
                        Text("检查中...")
                        return@TextButtonItem
                    }
                    when (val result = vm.updateCheckerTester.tester.result) {
                        is CheckVersionResult.Failed -> Text("检查失败")
                        is CheckVersionResult.UpToDate -> Text("已是最新")
                        is CheckVersionResult.HasNewVersion -> Text(remember(result.newVersion.name) { "有新版本: ${result.newVersion.name}" })
                        null -> Text("检查更新")
                    }
                },
            )
            AnimatedVisibility(
                vm.updateCheckerTester.tester.result is CheckVersionResult.HasNewVersion // 在设置里检查的
                        || autoUpdate.hasUpdate, // 在主页自动检查的
            ) {
                HorizontalDividerItem()
                Item(
                    action = {
                        TextButtonUpdateLogo(autoUpdate)
                    },
                )
            }
        }
        val uiSettings by vm.uiSettings

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
                                theme = uiSettings.theme.copy(kind = it),
                            ),
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
        Group(title = { Text("搜索") }) {
            val mySearchSettings by remember { derivedStateOf { uiSettings.searchSettings } }
            SwitchItem(
                checked = mySearchSettings.enableNewSearchSubjectApi,
                onCheckedChange = {
                    vm.uiSettings.update(
                        uiSettings.copy(
                            searchSettings = mySearchSettings.copy(
                                enableNewSearchSubjectApi = !mySearchSettings.enableNewSearchSubjectApi,
                            ),
                        ),
                    )
                },
                title = { Text("使用新版条目查询接口") },
                Modifier.placeholder(vm.uiSettings.loading),
                description = { Text("实验性接口，可能会缺失部分条目，谨慎启用") },
            )
        }
        Group(title = { Text("我的追番") }) {
            val myCollections by remember { derivedStateOf { uiSettings.myCollections } }
            SwitchItem(
                checked = myCollections.enableListAnimation,
                onCheckedChange = {
                    vm.uiSettings.update(
                        uiSettings.copy(
                            myCollections = myCollections.copy(
                                enableListAnimation = !myCollections.enableListAnimation,
                            ),
                        ),
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
                checked = episode.theme == EpisodeListProgressTheme.LIGHT_UP,
                onCheckedChange = {
                    vm.uiSettings.update(
                        uiSettings.copy(
                            episodeProgress = episode.copy(
                                theme = if (it) EpisodeListProgressTheme.LIGHT_UP else EpisodeListProgressTheme.ACTION,
                            ),
                        ),
                    )
                },
                title = { Text("点亮模式") },
                Modifier.placeholder(vm.uiSettings.loading),
                description = { Text("高亮已经看过的剧集，而不是将要看的剧集") },
            )
        }
        PlayerGroup(vm)
        AppSettingsTabPlatform(vm)
    }
}

@Composable
private fun SettingsScope.PlayerGroup(
    vm: AppSettingsViewModel
) {
    Group(title = { Text("播放器") }) {
        val config by vm.videoScaffoldConfig
        DropdownItem(
            selected = { config.fullscreenSwitchMode },
            values = { FullscreenSwitchMode.entries },
            itemText = {
                Text(
                    when (it) {
                        FullscreenSwitchMode.ALWAYS_SHOW_FLOATING -> "总是显示"
                        FullscreenSwitchMode.AUTO_HIDE_FLOATING -> "显示五秒后隐藏"
                        FullscreenSwitchMode.ONLY_IN_CONTROLLER -> "不显示"
                    },
                )
            },
            onSelect = {
                vm.videoScaffoldConfig.update(config.copy(fullscreenSwitchMode = it))
            },
            Modifier.placeholder(vm.uiSettings.loading),
            title = { Text("竖屏模式下显示全屏按钮") },
            description = { Text("总是显示播放器右下角的切换全屏按钮，方便切换") },
        )
        HorizontalDividerItem()
        SwitchItem(
            checked = config.pauseVideoOnEditDanmaku,
            onCheckedChange = {
                vm.videoScaffoldConfig.update(config.copy(pauseVideoOnEditDanmaku = it))
            },
            title = { Text("发送弹幕时自动暂停播放") },
            Modifier.placeholder(vm.uiSettings.loading),
        )
        HorizontalDividerItem()
        SwitchItem(
            checked = config.autoMarkDone,
            onCheckedChange = {
                vm.videoScaffoldConfig.update(config.copy(autoMarkDone = it))
            },
            title = { Text("观看 90% 后自动标记为看过") },
            Modifier.placeholder(vm.uiSettings.loading),
        )
        HorizontalDividerItem()
        SwitchItem(
            checked = config.hideSelectorOnSelect,
            onCheckedChange = {
                vm.videoScaffoldConfig.update(config.copy(hideSelectorOnSelect = it))
            },
            title = { Text("选择数据源后自动关闭弹窗") },
            Modifier.placeholder(vm.uiSettings.loading),
        )
        if (currentPlatform.isMobile()) {
            HorizontalDividerItem()
            SwitchItem(
                checked = config.autoFullscreenOnLandscapeMode,
                onCheckedChange = {
                    vm.videoScaffoldConfig.update(config.copy(autoFullscreenOnLandscapeMode = it))
                },
                title = { Text("重力感应旋屏") },
                Modifier.placeholder(vm.uiSettings.loading),
            )
        }
        HorizontalDividerItem()
        SwitchItem(
            checked = config.autoPlayNext,
            onCheckedChange = {
                vm.videoScaffoldConfig.update(config.copy(autoPlayNext = it))
            },
            title = { Text("自动连播") },
            Modifier.placeholder(vm.uiSettings.loading),
        )
    }
}

@Composable
private fun ReleaseClassIcon(releaseClass: ReleaseClass) {
    when (releaseClass) {
        ReleaseClass.ALPHA -> Icon(Icons.Rounded.RocketLaunch, null)
        ReleaseClass.BETA -> Icon(Icons.Rounded.Science, null)
        ReleaseClass.RC,
        ReleaseClass.STABLE -> Icon(Icons.Rounded.Verified, null)
    }
}

@Stable
private fun guessReleaseClass(version: String): ReleaseClass {
    val metadata = version
        .substringAfter("-", "")
        .lowercase()
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
