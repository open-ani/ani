package me.him188.ani.app.ui.settings

import androidx.compose.runtime.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import me.him188.ani.app.data.models.preference.DebugSettings
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.settings.framework.SettingsState
import me.him188.ani.utils.platform.currentTimeMillis


class DebugTriggerState(
    private val debugSettingsState: SettingsState<DebugSettings>,
    backgroundScope: CoroutineScope,
) {
    private val debugTriggerRecord = ArrayDeque<Long>()

    private val tasker = MonoTasker(backgroundScope)

    val debugSettings by debugSettingsState

    init {
        debugTriggerRecord.clear()
    }

    private suspend fun clearTriggerRecord() {
        delay(5000L)
        debugTriggerRecord.clear()
    }

    fun triggerDebugMode(): Boolean {
        if (debugSettings.enabled) return false
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
            debugSettingsState.update(debugSettings.copy(enabled = true))
            tasker.cancel()
            return true
        }
        return false
    }
}
