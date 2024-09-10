package me.him188.ani.app.ui.settings.tabs.media.torrent.peer

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import me.him188.ani.app.data.models.preference.TorrentPeerConfig

@Immutable
data class PeerFilterItemState(
    val enabled: Boolean,
    val content: List<String>
) {
    companion object {
        @Stable
        val Default = PeerFilterItemState(false, emptyList())
    }
}

@Stable
class PeerFilterSettingsState(
    configState: State<TorrentPeerConfig?>,
    private val onSave: (TorrentPeerConfig) -> Unit,
) {
    private val config by configState
    
    private val ipFilterState by derivedStateOf { 
        config?.let { PeerFilterItemState(it.enableIpFilter, it.ipFilters) } 
    }
    private val idFilterState by derivedStateOf {
        config?.let { PeerFilterItemState(it.enableIdFilter, it.idRegexFilters) }
    }
    private val clientFilterState by derivedStateOf {
        config?.let { PeerFilterItemState(it.enableClientFilter, it.clientRegexFilters) }
    }
    
    var ipFilter: PeerFilterItemState
        get() = ipFilterState ?: PeerFilterItemState.Default
        set(value) {
            val originalConfig = config ?: return
            onSave(originalConfig.copy(enableIpFilter = value.enabled, ipFilters = value.content))
        }

    var idFilter: PeerFilterItemState
        get() = idFilterState ?: PeerFilterItemState.Default
        set(value) {
            val originalConfig = config ?: return
            onSave(originalConfig.copy(enableIdFilter = value.enabled, idRegexFilters = value.content))
        }

    var clientFilter: PeerFilterItemState
        get() = clientFilterState ?: PeerFilterItemState.Default
        set(value) {
            val originalConfig = config ?: return
            onSave(originalConfig.copy(enableClientFilter = value.enabled, clientRegexFilters = value.content))
        }
}