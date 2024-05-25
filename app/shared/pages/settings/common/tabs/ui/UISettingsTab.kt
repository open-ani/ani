package me.him188.ani.app.ui.settings.tabs.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.him188.ani.app.data.models.UISettings
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.ui.collection.progress.EpisodeProgressTheme
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.settings.SettingsScope
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.SwitchItem
import me.him188.ani.app.ui.settings.framework.AbstractSettingsViewModel
import org.koin.core.component.inject


@Stable
class UiSettingsViewModel : AbstractSettingsViewModel() {
    private val settingsRepository: SettingsRepository by inject()

    val uiSettings by settings(
        settingsRepository.uiSettings,
        UISettings(_placeholder = -1)
    )
}

@Composable
fun UISettingsTab(
    vm: UiSettingsViewModel = rememberViewModel<UiSettingsViewModel> { UiSettingsViewModel() },
    modifier: Modifier = Modifier
) {
    val uiSettings by vm.uiSettings
    SettingsTab(modifier) {
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
        UISettingsTabPlatform(vm)
    }
}

@Composable
internal expect fun SettingsScope.UISettingsTabPlatform(vm: UiSettingsViewModel)
