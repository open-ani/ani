package me.him188.ani.app.ui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import me.him188.ani.app.data.models.preference.DebugSettings
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.utils.platform.annotations.TestOnly
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class DebugSettingsViewModel : AbstractViewModel(), KoinComponent {
    private val settingsRepository by inject<SettingsRepository>()
    val debugSettings =
        settingsRepository.debugSettings.stateInBackground(placeholder = DebugSettings(_placeHolder = -1))

    @TestOnly
    var isAppInDebugModeOverride by mutableStateOf(false)

    @OptIn(TestOnly::class)
    val isAppInDebugMode: Boolean by derivedStateOf {
        isAppInDebugModeOverride || debugSettings.value.enabled
    }
}

@Composable
fun isInDebugMode(): Boolean {
    val vm = rememberDebugSettingsViewModel()
    return vm.isAppInDebugMode
}

@Composable
fun rememberDebugSettingsViewModel(): DebugSettingsViewModel =
    viewModel<DebugSettingsViewModel> { DebugSettingsViewModel() }