package me.him188.ani.app.torrent.api.peer

import me.him188.ani.datasources.api.topic.FileSize

// peer_info_t
interface PeerInfo {
    val handle: Long // corresponding torrent handle
    /**
     * The peer's id as used in the bittorrent protocol. 
     * This id can be used to extract 'fingerprints' from the peer. 
     * Sometimes it can tell you which client the peer is using.
     * 
     * For anitorrent, the value is calculated by
     * [computeTorrentFingerprint][me.him188.ani.app.tools.torrent.engines.computeTorrentFingerprint].
     */
    val id: CharArray

    /**
     * A human readable string describing the software at the other end of the connection. 
     * In some cases this information is not available, then it will contain a string 
     * that may give away something about which software is running in the other end. 
     * In the case of a web seed, the server type and version will be a part of this string.
     * 
     * For anitorrent, the value is calculated by
     * [computeTorrentUserAgent][me.him188.ani.app.tools.torrent.engines.computeTorrentUserAgent].
     */
    val client: String
    
    val ipAddr: String
    
    val ipPort: Int

    /**
     * The progress of the peer in the range [0, 1].
     */
    val progress: Float

    /**
     * The total number of bytes downloaded from and uploaded to this peer.
     */
    val totalDownload: FileSize
    
    val totalUpload: FileSize

    /**
     * Tells you in which state the peer is in. It is set to
     * any combination of the peer_flags_t flags above.
     */
    val flags: Long
}