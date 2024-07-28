package me.him188.ani.app.torrent.io

import java.io.File
import java.io.IOException

internal object TorrentFileIO {
    @OptIn(ExperimentalStdlibApi::class)
    @kotlin.jvm.Throws(IOException::class)
    fun hashFileMd5(input: File): String {
        val md = java.security.MessageDigest.getInstance("MD5")
        val buffer = ByteArray(81920)
        input.inputStream().use { inputStream ->
            while (true) {
                val read = inputStream.read(buffer)
                if (read == -1) {
                    break
                }
                md.update(buffer, 0, read)
            }
            val bytes = md.digest()
            return bytes.toHexString()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun hashSha1(data: ByteArray): String {
        val md = java.security.MessageDigest.getInstance("SHA-1")
        val bytes = md.digest(data)
        return bytes.toHexString()
    }
}