package me.him188.ani.app.tools.torrent

import me.him188.ani.app.torrent.api.peer.PeerFilter
import me.him188.ani.app.torrent.api.peer.PeerInfo
import me.him188.ani.utils.ipparser.IpSeqRange

class PeerIpFilter(pattern: String) : PeerFilter {
    private val parser = IpSeqRange.parse(pattern)
    
    override fun onFilter(info: PeerInfo): Boolean {
        return parser.contains(info.ipAddr)
    }
}

class PeerClientFilter(patternRegex: String) : PeerFilter {
    private val regex = Regex(patternRegex)
    override fun onFilter(info: PeerInfo): Boolean {
        return info.client.contains(regex)
    }
}

class PeerIdFilter(patternRegex: String) : PeerFilter {
    private val regex = Regex(patternRegex)
    override fun onFilter(info: PeerInfo): Boolean {
        return info.id.contains(regex)
    }
}