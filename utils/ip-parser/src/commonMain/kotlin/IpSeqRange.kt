package me.him188.ani.utils.ipparser

interface IpSeqRange {
    fun contains(address: String): Boolean
    
    companion object {
        fun parse(ipSeqPattern: String): IpSeqRange {
            return createIpSeqRange(ipSeqPattern)
        }
    }
}

internal expect fun createIpSeqRange(ipSeqPattern: String): IpSeqRange