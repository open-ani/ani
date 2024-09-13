package me.him188.ani.app.ui.settings.tabs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.him188.ani.app.data.models.preference.DebugSettings
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.framework.SettingsState
import me.him188.ani.app.ui.settings.framework.components.SwitchItem

@Composable
fun DebugTab(
    debugSettingsState: SettingsState<DebugSettings>,
    modifier: Modifier = Modifier,
    onDisableDebugMode: () -> Unit = {}
) {
    val debugSettings by debugSettingsState

    SettingsTab(modifier) {
        Group(
            title = { Text("调试模式状态") },
            useThinHeader = true,
        ) {
            SwitchItem(
                checked = debugSettings.enabled,
                onCheckedChange = { checked ->
                    if (!checked) onDisableDebugMode()
                    debugSettingsState.update(debugSettings.copy(enabled = checked))
                },
                title = { Text("调试模式") },
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
                    debugSettingsState.update(debugSettings.copy(showAllEpisodes = checked))
                },
                title = { Text("显示所有剧集") },
                description = { Text("显示所有剧集，包括SP、OP、ED等，目前仅部分在线源支持，请谨慎启用") },
            )
        }
    }
}