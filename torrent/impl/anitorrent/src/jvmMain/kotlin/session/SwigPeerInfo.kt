package me.him188.ani.app.torrent.anitorrent.session

import me.him188.ani.app.torrent.anitorrent.binding.peer_info_t
import me.him188.ani.app.torrent.api.peer.PeerInfo

class SwigPeerInfo(
    private val native: peer_info_t,
) : PeerInfo {
    override val id: String get() = native.peer_id
    override val client: String get() = native.client
    override val ipAddr: String get() = native.ip_addr
    override val ipPort: Int get() = native.ip_port
}