package me.him188.ani.app.data.models.preference

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.app.torrent.api.peer.PeerInfo

@Serializable
data class TorrentPeerConfig(
    /**
     * ip 过滤.
     */
    val ipFilters: List<String> = emptyList(),
    val enableIpFilter: Boolean = false,

    /**
     * 使用正则表达式过滤 [PeerInfo.client].
     */
    val clientRegexFilters: List<String> = emptyList(),
    val enableClientFilter: Boolean = false,

    /**
     * 使用正则表达式过滤 [PeerInfo.id]
     */
    val idRegexFilters: List<String> = emptyList(),
    val enableIdFilter: Boolean = false,


    @Suppress("PropertyName")
    @Transient val _placeholder: Int = 0,
) {
    companion object {
        val Default = TorrentPeerConfig()
    }
}