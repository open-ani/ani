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

object PeerUnexpectedIdFilter : PeerFilter {
    private const val FINGERPRINT_TRIM = '-'
            
    override fun onFilter(info: PeerInfo): Boolean {
        val idArr = info.id
        // id 开头不是 fingerprint 就拒绝连接 
        if (idArr[0] != FINGERPRINT_TRIM) return true
        if (!idArr.drop(1).contains(FINGERPRINT_TRIM)) return true
        return false
    }
}

class PeerIdFilter(patternRegex: String) : PeerFilter {
    private val regex = Regex(patternRegex)
    override fun onFilter(info: PeerInfo): Boolean {
        val decoded = info.id.contentToString()
        return decoded.contains(regex)
    }
}

class PeerIpBlackListFilter(private val ipList: List<String>) : PeerFilter {
    override fun onFilter(info: PeerInfo): Boolean {
        return ipList.contains(info.ipAddr)
    }
}