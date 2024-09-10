package me.him188.ani.app.ui.settings.tabs.media.torrent.peer

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.models.preference.TorrentPeerConfig
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class PeerFilterSettingsViewModel : AbstractViewModel(), KoinComponent {
    val settingsRepository: SettingsRepository by inject()
    
    private val peerFilterConfig = settingsRepository.torrentPeerConfig.flow
    private val updateTasker = MonoTasker(backgroundScope)
    private val localConfig: MutableState<TorrentPeerConfig?> = mutableStateOf(null)
    
    val state = PeerFilterSettingsState(
        configState = localConfig,
        onSave = { update(it) },
        isSavingState = derivedStateOf { updateTasker.isRunning }
    )
    
    init {
        launchInBackground { 
            peerFilterConfig.distinctUntilChanged().collectLatest { config ->
                withContext(Dispatchers.Main) {
                    localConfig.value = config
                }
            }
        }
    }
    
    private fun update(new: TorrentPeerConfig) {
        updateTasker.launch {
            localConfig.value = new
            delay(500)
            settingsRepository.torrentPeerConfig.update { new }
        }
    }
}