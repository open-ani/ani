package me.him188.ani.app.torrent.api.peer

// peer_info_t
interface PeerInfo {
    /**
     * The peer's id as used in the bittorrent protocol. 
     * This id can be used to extract 'fingerprints' from the peer. 
     * Sometimes it can tell you which client the peer is using.
     * 
     * For anitorrent, the value is calculated by
     * [computeTorrentFingerprint][me.him188.ani.app.tools.torrent.engines.computeTorrentFingerprint]
     * and [computeTorrentUserAgent][me.him188.ani.app.tools.torrent.engines.computeTorrentUserAgent].
     */
    val id: String

    /**
     * A human readable string describing the software at the other end of the connection. 
     * In some cases this information is not available, then it will contain a string 
     * that may give away something about which software is running in the other end. 
     * In the case of a web seed, the server type and version will be a part of this string.
     */
    val client: String
    
    val ipAddr: String
    
    val ipPort: Int
}