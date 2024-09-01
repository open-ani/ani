package me.him188.ani.app.ui.settings.tabs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import me.him188.ani.app.data.models.preference.DebugSettings
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.framework.AbstractSettingsViewModel
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DebugViewModel : AbstractSettingsViewModel(), KoinComponent {
    private val settingsRepository by inject<SettingsRepository>()

    val debugSettings by settings(
        settingsRepository.debugSettings,
        placeholder = DebugSettings(_placeHolder = -1),
    )
}

@Composable
fun DebugTab(
    modifier: Modifier = Modifier,
    onDisableDebugMode: () -> Unit = {}
) {
    val vm = viewModel { DebugViewModel() }
    val debugSettings by vm.debugSettings

    SettingsTab(modifier) {
        Group(
            title = { Text("调试模式状态") },
            useThinHeader = true,
        ) {
            SwitchItem(
                checked = debugSettings.enabled,
                onCheckedChange = { checked ->
                    if (!checked) onDisableDebugMode()
                    vm.debugSettings.update(debugSettings.copy(enabled = checked))
                },
                title = { Text("调试模式") },
                Modifier.placeholder(vm.debugSettings.loading),
                description = { Text("已开启调试模式，点击关闭") },
            )
        }
        Group(
            title = { Text("条目选集") },
            useThinHeader = true,
        ) {
            SwitchItem(
                checked = debugSettings.showAllEpisodes,
                onCheckedChange = { checked ->
                    vm.debugSettings.update(debugSettings.copy(showAllEpisodes = checked))
                },
                title = { Text("显示所有剧集") },
                Modifier.placeholder(vm.debugSettings.loading),
                description = { Text("显示所有剧集，包括SP、OP、ED等，目前仅部分在线源支持，请谨慎启用") },
            )
        }
    }
}