package me.him188.ani.utils.io

import kotlinx.io.Source
import java.security.MessageDigest

actual fun Source.readAndDigest(algorithm: DigestAlgorithm): ByteArray {
    return when (algorithm) {
        DigestAlgorithm.MD5 -> {
            digest("MD5")
        }

        DigestAlgorithm.SHA256 -> {
            digest("SHA-256")
        }
    }
}

private fun Source.digest(algorithm: String): ByteArray {
    val digest = MessageDigest.getInstance(algorithm)
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var read = 0
    while (read != -1) {
        read = readAtMostTo(buffer)
        if (read != -1) {
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest()
}
