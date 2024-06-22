package me.him188.ani.app.ui.settings.tabs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.him188.ani.app.data.models.DebugSettings
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.rememberViewModel
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
    val vm = rememberViewModel { DebugViewModel() }
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
    }
}