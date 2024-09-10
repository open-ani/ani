package me.him188.ani.app.ui.settings.tabs.media.torrent.peer

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import me.him188.ani.app.data.models.preference.TorrentPeerConfig
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.AbstractViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class PeerFilterSettingsViewModel : AbstractViewModel(), KoinComponent {
    val settingsRepository: SettingsRepository by inject()
    
    private val peerFilterConfig = settingsRepository.torrentPeerConfig.flow.produceState(null)
    private val updateTasker = MonoTasker(backgroundScope)
    
    val state by derivedStateOf { 
        PeerFilterSettingsState(
            peerFilterConfig,
            onSave = { update(it) }
        ) 
    }
    
    private fun update(new: TorrentPeerConfig) {
        updateTasker.launch {
            settingsRepository.torrentPeerConfig.update { new }
        }
    }
}