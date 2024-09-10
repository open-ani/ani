package me.him188.ani.app.ui.settings.tabs.media.torrent.peer

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import me.him188.ani.app.data.models.preference.TorrentPeerConfig
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.settings.tabs.media.source.rss.SaveableStorage

@Immutable
data class PeerFilterItemState(
    val enabled: Boolean,
    val content: String
) {
    companion object {
        @Stable
        val Default = PeerFilterItemState(false, "")
    }
}

@Stable
class PeerFilterSettingsState(
    configState: State<TorrentPeerConfig?>,
    onSave: (TorrentPeerConfig) -> Unit,
    isSavingState: State<Boolean>,
) {
    private val storage = SaveableStorage(
        containerState = configState,
        onSave = onSave,
        isSavingState = stateOf(false)
    )
    
    var editingIpBlockList by mutableStateOf(false)
    
    var ipBlackList by storage.prop({ it.ipBlackList }, { copy(ipBlackList = it) }, emptyList())
    
    var ipFilterEnabled by storage.prop({ it.enableIpFilter }, { copy(enableIpFilter = it) }, false)
    var ipFilters by storage.prop(
        get = { it.ipFilters.joinToString("\n") }, 
        copy = { copy(ipFilters = it.split('\n')) }, 
        default = ""
    )
    
    var idFilterEnabled by storage.prop({ it.enableIdFilter }, { copy(enableIdFilter = it) }, false)
    var idFilters by storage.prop(
        get = { it.idRegexFilters.joinToString("\n") },
        copy = { copy(idRegexFilters = it.split('\n')) },
        default = ""
    )
    
    var clientFilterEnabled by storage.prop({ it.enableClientFilter }, { copy(enableClientFilter = it) }, false)
    var clientFilters by storage.prop(
        get = { it.idRegexFilters.joinToString("\n") },
        copy = { copy(idRegexFilters = it.split('\n')) },
        default = ""
    )
}