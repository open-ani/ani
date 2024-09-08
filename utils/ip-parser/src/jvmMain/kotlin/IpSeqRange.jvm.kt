package me.him188.ani.utils.ipparser

import inet.ipaddr.AddressStringException
import inet.ipaddr.IPAddressString
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn

private val logger = logger("IpSeqRange")

internal actual fun createIpSeqRange(ipSeqPattern: String): IpSeqRange {
    return object : IpSeqRange {
        private val range = try {
            IPAddressString(ipSeqPattern).address
        } catch (ex: AddressStringException) {
            logger.warn(ex) { "failed to parse ip range $ipSeqPattern" }
            null
        }
        
        override fun contains(address: String): Boolean {
            if (range == null) return false
            val ipAddress = try { 
                IPAddressString(address).address 
            } catch (ex: AddressStringException) { 
                return false
            }
            return range.contains(ipAddress)
        }
    }
}