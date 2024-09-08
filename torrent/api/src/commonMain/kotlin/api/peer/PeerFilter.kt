package me.him188.ani.app.torrent.api.peer

/**
 * A filter to reject peer connections of bit torrent.
 */
interface PeerFilter {
    /**
     * determine if this peer should be filtered out.
     * 
     * @return `true` if want to drop connection to the peer.
     */
    fun onFilter(info: PeerInfo): Boolean
}