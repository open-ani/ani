package me.him188.ani.app.ui.profile

import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import me.him188.ani.app.data.models.preference.DebugSettings
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.settings.framework.AbstractSettingsViewModel
import me.him188.ani.utils.platform.currentTimeMillis
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsViewModel(
    private val triggerOnEnableDebugMode: () -> Unit = {}
) : AbstractSettingsViewModel(), KoinComponent {
    private val settingsRepository by inject<SettingsRepository>()
    private val debugTriggerRecord = ArrayDeque<Long>()

    private val tasker = MonoTasker(backgroundScope)

    private val _debugSettings by settings(
        settingsRepository.debugSettings,
        placeholder = DebugSettings(_placeHolder = -1),
    )
    val debugSettings by _debugSettings

    init {
        debugTriggerRecord.clear()
    }

    private suspend fun clearTriggerRecord() {
        delay(5000L)
        debugTriggerRecord.clear()
    }

    fun triggerDebugMode() {
        if (debugSettings.enabled) return
        tasker.launch { clearTriggerRecord() }

        if (debugTriggerRecord.size == 5) {
            debugTriggerRecord.clear()
            tasker.cancel()
        }
        debugTriggerRecord.addFirst(currentTimeMillis())

        if (
            debugTriggerRecord.size == 5 &&
            debugTriggerRecord.zipWithNext().all { (prev, next) -> next - prev < 1000L }
        ) {
            debugTriggerRecord.clear()
            _debugSettings.update(debugSettings.copy(enabled = true))
            triggerOnEnableDebugMode()
            tasker.cancel()
        }
    }
}