package me.him188.ani.app.ui.settings.tabs.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.effects.OnLifecycleEvent
import me.him188.ani.app.ui.settings.SettingsScope
import me.him188.ani.app.ui.settings.SwitchItem
import moe.tlaster.precompose.lifecycle.Lifecycle


@Preview
@Composable
private fun PreviewUISettingsTab() {
    ProvideCompositionLocalsForPreview {
        UISettingsTab()
    }
}


@SuppressLint("BatteryLife")
@Composable
internal actual fun SettingsScope.UISettingsTabPlatform(vm: UiSettingsViewModel) {
    Group(
        title = { Text("后台运行") },
        description = { Text(text = "缓存功能需要应用保持在后台运行才能下载视频") }
    ) {
        val context by rememberUpdatedState(newValue = LocalContext.current)
        val powerManager by remember {
            derivedStateOf {
                kotlin.runCatching { context.getSystemService(Context.POWER_SERVICE) as PowerManager? }.getOrNull()
            }
        }
        // 禁用电池优化
        if (powerManager != null) {
            var isIgnoring by remember {
                mutableStateOf(powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true)
            }
            OnLifecycleEvent {
                if (it == Lifecycle.State.Active) {
                    isIgnoring = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
                }
            }
            SwitchItem(
                checked = isIgnoring,
                onCheckedChange = {
                    if (!isIgnoring) {
                        kotlin.runCatching {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                    Uri.parse("package:${context.packageName}")
                                )
                            )
                        }
                    } else {
                        kotlin.runCatching {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                                )
                            )
                        }
                    }
                },
                title = { Text("禁用电池优化") },
                description = { Text("可以帮助保持在后台运行。可能增加耗电") },
            )
        }
    }
}
