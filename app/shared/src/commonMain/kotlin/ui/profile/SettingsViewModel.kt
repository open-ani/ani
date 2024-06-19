package me.him188.ani.app.ui.profile

import androidx.compose.runtime.getValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.DebugSettings
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.ui.settings.framework.AbstractSettingsViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.LinkedList

class SettingsViewModel : AbstractSettingsViewModel(), KoinComponent {
    private val settingsRepository by inject<SettingsRepository>()
    private val debugTriggerRecord = LinkedList<Long>()

    private var clearJob: Job? = null

    private val _debugSettings by settings(
        settingsRepository.debugSettings,
        placeholder = DebugSettings(_placeHolder = -1)
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
        cancelClearJob()
        clearJob = viewModelScope.launch { clearTriggerRecord() }

        if (debugTriggerRecord.size == 5) {
            debugTriggerRecord.clear()
            cancelClearJob()
        }
        debugTriggerRecord.push(System.currentTimeMillis())

        if (
            debugTriggerRecord.size == 5 &&
            debugTriggerRecord.zipWithNext().all { (prev, next) -> next - prev < 1000L }
        ) {
            debugTriggerRecord.clear()
            _debugSettings.update(debugSettings.copy(enabled = true))
            cancelClearJob()
        }
    }

    private fun cancelClearJob() {
        clearJob?.cancel()
        clearJob = null
    }
}