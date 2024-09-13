package me.him188.ani.app.ui.settings.tabs.media.torrent.peer

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformLatest
import me.him188.ani.app.data.models.preference.TorrentPeerConfig
import me.him188.ani.app.ui.settings.tabs.media.source.rss.SaveableStorage
import me.him188.ani.app.ui.subject.components.comment.CommentEditorTextState

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
    storage: SaveableStorage<TorrentPeerConfig>
) {
    var searchingBlockedIp by mutableStateOf(false)
        private set
    val searchBlockedIpQuery = MutableStateFlow("")

    private var ipBlackList by storage.prop({ it.ipBlackList }, { copy(ipBlackList = it) }, emptyList())
    var searchedIpBlockList: Flow<List<String>> = searchBlockedIpQuery
        .combine(snapshotFlow { ipBlackList }) { query, list ->
            query to list
        }
        .transformLatest { (query, list) ->
            kotlinx.coroutines.delay(500)
            // 需要去重，避免 lazy column 出现重复的 key
            emit(list.filter { it.contains(query) }.distinct())
        }
    
    val newBlockedIpValue = CommentEditorTextState("")
    
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
    
    fun addBlockedIp(list: List<String>) {
        ipBlackList = ipBlackList.toMutableList().apply { addAll(list) }
    }
    
    fun removeBlockedIp(value: String) {
        val newList = mutableListOf<String>()
        for (ip in ipBlackList) {
            if (ip != value) newList.add(ip)
        }
        ipBlackList = newList
    }
    
    fun setSearchBlockIpQuery(value: String) {
        searchBlockedIpQuery.value = value
    }
    
    fun stopSearchBlockedIp() {
        searchBlockedIpQuery.value = ""
        searchingBlockedIp = false
    }
    
    fun startSearchBlockedIp() {
        searchBlockedIpQuery.value = ""
        searchingBlockedIp = true
    }
}