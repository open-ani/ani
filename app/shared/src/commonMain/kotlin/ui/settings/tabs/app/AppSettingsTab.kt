package me.him188.ani.app.ui.settings.tabs.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.HdrAuto
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.data.models.danmaku.DanmakuFilterConfig
import me.him188.ani.app.data.models.preference.FullscreenSwitchMode
import me.him188.ani.app.data.models.preference.ThemeKind
import me.him188.ani.app.data.models.preference.UISettings
import me.him188.ani.app.data.models.preference.UpdateSettings
import me.him188.ani.app.data.models.preference.VideoScaffoldConfig
import me.him188.ani.app.data.source.danmaku.protocol.ReleaseClass
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.platform.currentPlatform
import me.him188.ani.app.platform.isAndroid
import me.him188.ani.app.platform.isDesktop
import me.him188.ani.app.platform.isMobile
import me.him188.ani.app.tools.update.supportsInAppUpdate
import me.him188.ani.app.ui.foundation.isInDebugMode
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.framework.SettingsState
import me.him188.ani.app.ui.settings.framework.SingleTester
import me.him188.ani.app.ui.settings.framework.Tester
import me.him188.ani.app.ui.settings.framework.components.DropdownItem
import me.him188.ani.app.ui.settings.framework.components.RowButtonItem
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import me.him188.ani.app.ui.settings.framework.components.TextButtonItem
import me.him188.ani.app.ui.settings.framework.components.TextItem
import me.him188.ani.app.ui.subject.episode.list.EpisodeListProgressTheme
import me.him188.ani.app.ui.subject.episode.video.settings.DanmakuRegexFilterGroup
import me.him188.ani.app.ui.subject.episode.video.settings.DanmakuRegexFilterState
import me.him188.ani.app.ui.update.AutoUpdateViewModel
import me.him188.ani.app.ui.update.ChangelogDialog
import me.him188.ani.app.ui.update.NewVersion
import me.him188.ani.app.ui.update.TextButtonUpdateLogo
import me.him188.ani.app.ui.update.UpdateChecker
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

@Composable
fun AppSettingsTab(
    softwareUpdateGroupState: SoftwareUpdateGroupState,
    uiSettings: SettingsState<UISettings>,
    videoScaffoldConfig: SettingsState<VideoScaffoldConfig>,
    danmakuFilterConfig: SettingsState<DanmakuFilterConfig>,
    danmakuRegexFilterState: DanmakuRegexFilterState,
    modifier: Modifier = Modifier
) {
    SettingsTab(modifier) {
        SoftwareUpdateGroup(softwareUpdateGroupState)
        UISettingsGroup(uiSettings)
        PlayerGroup(
            videoScaffoldConfig,
            danmakuFilterConfig,
            danmakuRegexFilterState,
        )
        AppSettingsTabPlatform()
    }
}

@Stable
private val themesSupportedByPlatform = if (Platform.currentPlatform.isDesktop()) {
    listOf(ThemeKind.AUTO, ThemeKind.LIGHT, ThemeKind.DARK)
} else {
    listOf(ThemeKind.AUTO, ThemeKind.DYNAMIC)
}

@Composable
private fun SettingsScope.UISettingsGroup(
    state: SettingsState<UISettings>,
) {
    val uiSettings by state
    Group(title = { Text("通用") }) {
        if (Platform.currentPlatform.isDesktop() || Platform.currentPlatform.isAndroid()) {
            DropdownItem(
                selected = { uiSettings.theme.kind },
                values = { themesSupportedByPlatform },
                itemText = {
                    when (it) {
                        ThemeKind.AUTO -> Text("自动")
                        ThemeKind.LIGHT -> Text("浅色")
                        ThemeKind.DARK -> Text("深色")
                        ThemeKind.DYNAMIC -> Text("动态")
                    }
                },
                onSelect = {
                    state.update(
                        uiSettings.copy(
                            theme = uiSettings.theme.copy(kind = it),
                        ),
                    )
                },
                itemIcon = {
                    when (it) {
                        ThemeKind.AUTO -> Icon(Icons.Rounded.HdrAuto, null)
                        ThemeKind.LIGHT -> Icon(Icons.Rounded.LightMode, null)
                        ThemeKind.DARK -> Icon(Icons.Rounded.DarkMode, null)
                        ThemeKind.DYNAMIC -> Icon(Icons.Rounded.Palette, null)
                    }
                },
                description = {
                    when (uiSettings.theme.kind) {
                        ThemeKind.AUTO -> {
                            Text("根据系统设置自动切换")
                        }

                        ThemeKind.DYNAMIC -> Text("根据壁纸动态配色")
                        else -> {}
                    }
                },
                title = { Text("主题") },
            )
        }

        Group(title = { Text("搜索") }, useThinHeader = true) {
            SwitchItem(
                checked = uiSettings.searchSettings.enableNewSearchSubjectApi,
                onCheckedChange = {
                    state.update(
                        uiSettings.copy(
                            searchSettings = uiSettings.searchSettings.copy(
                                enableNewSearchSubjectApi = !uiSettings.searchSettings.enableNewSearchSubjectApi,
                            ),
                        ),
                    )
                },
                title = { Text("使用新版条目查询接口") },
                description = { Text("实验性接口，可能会缺失部分条目，谨慎启用") },
            )
        }

        Group(title = { Text("我的追番") }, useThinHeader = true) {
            SwitchItem(
                checked = uiSettings.myCollections.enableListAnimation,
                onCheckedChange = {
                    state.update(
                        uiSettings.copy(
                            myCollections = uiSettings.myCollections.copy(
                                enableListAnimation = !uiSettings.myCollections.enableListAnimation,
                            ),
                        ),
                    )
                },
                title = { Text("列表滚动动画") },
                description = { Text("如遇到显示重叠问题，可尝试关闭") },
            )
        }

        Group(title = { Text("选集播放") }, useThinHeader = true) {
            val episode by remember { derivedStateOf { uiSettings.episodeProgress } }
            SwitchItem(
                checked = episode.theme == EpisodeListProgressTheme.LIGHT_UP,
                onCheckedChange = {
                    state.update(
                        uiSettings.copy(
                            episodeProgress = episode.copy(
                                theme = if (it) EpisodeListProgressTheme.LIGHT_UP else EpisodeListProgressTheme.ACTION,
                            ),
                        ),
                    )
                },
                title = { Text("点亮模式") },
                description = { Text("高亮已经看过的剧集，而不是将要看的剧集") },
            )
        }
    }
}

@Stable
class SoftwareUpdateGroupState(
    val updateSettings: SettingsState<UpdateSettings>,
    backgroundScope: CoroutineScope,
    val currentVersion: String = currentAniBuildConfig.versionName,
    val releaseClass: ReleaseClass = guessReleaseClass(currentVersion),
    private val onTest: suspend () -> CheckVersionResult = {
        UpdateChecker().let { checker ->
            val v = checker.checkLatestVersion(
                updateSettings.value.releaseClass,
                currentVersion,
            )
            if (v == null) {
                CheckVersionResult.UpToDate
            } else {
                CheckVersionResult.HasNewVersion(v)
            }
        }
    },
) {
    val updateCheckerTester = SingleTester(
        Tester(
            "new",
            onTest = { onTest() },
            onError = { CheckVersionResult.Failed(it) },
        ),
        backgroundScope,
    )
}

@Composable
private fun SettingsScope.SoftwareUpdateGroup(
    state: SoftwareUpdateGroupState,
    modifier: Modifier = Modifier,
) {
    Group(title = { Text("软件更新") }, modifier = modifier) {
        TextItem(
            description = { Text("当前版本") },
            icon = { ReleaseClassIcon(state.releaseClass) },
            title = { Text(state.currentVersion) },
        )
        HorizontalDividerItem()
        val context by rememberUpdatedState(LocalContext.current)
        RowButtonItem(
            onClick = {
                KoinPlatform.getKoin().get<BrowserNavigator>().openBrowser(
                    context,
                    "https://github.com/open-ani/ani/releases/tag/v${currentAniBuildConfig.versionName}",
                )
            },
            icon = { Icon(Icons.Rounded.ArrowOutward, null) },
        ) { Text("查看更新日志") }
        HorizontalDividerItem()
        val updateSettings by state.updateSettings
        SwitchItem(
            updateSettings.autoCheckUpdate,
            onCheckedChange = {
                state.updateSettings.update(updateSettings.copy(autoCheckUpdate = !updateSettings.autoCheckUpdate))
            },
            title = { Text("自动检查更新") },
            description = { Text("只会显示一个更新图标，不会自动下载") },
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
                state.updateSettings.update(updateSettings.copy(releaseClass = it))
            },
            itemIcon = {
                ReleaseClassIcon(it)
            },
            title = { Text("更新类型") },
        )
        HorizontalDividerItem()
        SwitchItem(
            updateSettings.inAppDownload,
            { state.updateSettings.update(updateSettings.copy(inAppDownload = it)) },
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
                Column {
                    HorizontalDividerItem()
                    SwitchItem(
                        updateSettings.autoDownloadUpdate,
                        { state.updateSettings.update(updateSettings.copy(autoDownloadUpdate = it)) },
                        title = { Text("自动下载更新") },
                        description = { Text("下载完成后会提示，确认后才会安装") },
                        enabled = updateSettings.autoCheckUpdate,
                    )
                }
            }
        }
        HorizontalDividerItem()
        var showUpdatePopup by remember { mutableStateOf(false) }
        val autoUpdate: AutoUpdateViewModel = viewModel { AutoUpdateViewModel() }
        if (showUpdatePopup) {
            (state.updateCheckerTester.tester.result as? CheckVersionResult.HasNewVersion)?.let {
                ChangelogDialog(
                    latestVersion = it.newVersion,
                    onDismissRequest = { showUpdatePopup = false },
                    onStartDownload = { autoUpdate.startDownload(it.newVersion, context) },
                )
            }
        }
        TextButtonItem(
            onClick = {
                if (state.updateCheckerTester.tester.isTesting) {
                    state.updateCheckerTester.cancel()
                    return@TextButtonItem
                }
                when (state.updateCheckerTester.tester.result) {
                    is CheckVersionResult.HasNewVersion -> showUpdatePopup = true
                    is CheckVersionResult.Failed,
                    is CheckVersionResult.UpToDate,
                    null -> {
                        state.updateCheckerTester.testAll()
                        autoUpdate.startCheckLatestVersion(context)
                    }
                }
            },
            title = {
                if (state.updateCheckerTester.tester.isTesting) {
                    Text("检查中...")
                    return@TextButtonItem
                }
                when (val result = state.updateCheckerTester.tester.result) {
                    is CheckVersionResult.Failed -> Text("检查失败")
                    is CheckVersionResult.UpToDate -> Text("已是最新")
                    is CheckVersionResult.HasNewVersion -> Text(remember(result.newVersion.name) { "有新版本: ${result.newVersion.name}" })
                    null -> Text("检查更新")
                }
            },
        )
        AnimatedVisibility(
            state.updateCheckerTester.tester.result is CheckVersionResult.HasNewVersion // 在设置里检查的
                    || autoUpdate.hasUpdate, // 在主页自动检查的
        ) {
            HorizontalDividerItem()
            ListItem(
                headlineContent = {},
                trailingContent = {
                    TextButtonUpdateLogo(autoUpdate)
                },
            )
        }
    }
}

@Composable
private fun SettingsScope.PlayerGroup(
    videoScaffoldConfig: SettingsState<VideoScaffoldConfig>,
    danmakuFilterConfig: SettingsState<DanmakuFilterConfig>,
    danmakuRegexFilterState: DanmakuRegexFilterState,
) {
    Group(title = { Text("播放器") }) {
        val config by videoScaffoldConfig
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
                videoScaffoldConfig.update(config.copy(fullscreenSwitchMode = it))
            },
            title = { Text("竖屏模式下显示全屏按钮") },
            description = { Text("总是显示播放器右下角的切换全屏按钮，方便切换") },
        )
        HorizontalDividerItem()
        SwitchItem(
            danmakuFilterConfig.value.enableRegexFilter,
            onCheckedChange = {
                danmakuFilterConfig.update(danmakuFilterConfig.value.copy(enableRegexFilter = it))
            },
            title = { Text("启用正则弹幕过滤器") },
        )
        HorizontalDividerItem()
        DanmakuRegexFilterGroup(
            state = danmakuRegexFilterState,
        )
        HorizontalDividerItem()
        SwitchItem(
            checked = config.pauseVideoOnEditDanmaku,
            onCheckedChange = {
                videoScaffoldConfig.update(config.copy(pauseVideoOnEditDanmaku = it))
            },
            title = { Text("发送弹幕时自动暂停播放") },
        )
        HorizontalDividerItem()
        SwitchItem(
            checked = config.autoMarkDone,
            onCheckedChange = {
                videoScaffoldConfig.update(config.copy(autoMarkDone = it))
            },
            title = { Text("观看 90% 后自动标记为看过") },
        )
        HorizontalDividerItem()
        SwitchItem(
            checked = config.hideSelectorOnSelect,
            onCheckedChange = {
                videoScaffoldConfig.update(config.copy(hideSelectorOnSelect = it))
            },
            title = { Text("选择数据源后自动关闭弹窗") },
        )
        if (currentPlatform.isMobile() && isInDebugMode()) {
            HorizontalDividerItem()
            SwitchItem(
                checked = config.autoFullscreenOnLandscapeMode,
                onCheckedChange = {
                    videoScaffoldConfig.update(config.copy(autoFullscreenOnLandscapeMode = it))
                },
                title = { Text("重力感应旋屏") },
            )
        }
        HorizontalDividerItem()
        SwitchItem(
            checked = config.autoPlayNext,
            onCheckedChange = {
                videoScaffoldConfig.update(config.copy(autoPlayNext = it))
            },
            title = { Text("自动连播") },
        )
        if (currentPlatform.isDesktop()) {
            HorizontalDividerItem()
            SwitchItem(
                checked = config.autoSkipOpEd,
                onCheckedChange = {
                    videoScaffoldConfig.update(config.copy(autoSkipOpEd = it))
                },
                title = { Text("自动跳过 OP 和 ED") },
            )
        }
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
internal expect fun SettingsScope.AppSettingsTabPlatform()
