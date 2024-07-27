package me.him188.ani.utils.io

import korlibs.crypto.Hasher
import korlibs.crypto.MD5
import korlibs.crypto.SHA256
import kotlinx.io.Source

actual fun Source.readAndDigest(algorithm: DigestAlgorithm): ByteArray {
    return when (algorithm) {
        DigestAlgorithm.MD5 -> {
            digest(MD5())
        }

        DigestAlgorithm.SHA256 -> {
            digest(SHA256())
        }
    }
}

@Suppress("SpellCheckingInspection")
private fun Source.digest(hasher: Hasher): ByteArray {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var read = 0
    while (read != -1) {
        read = readAtMostTo(buffer)
        if (read != -1) {
            hasher.update(buffer, 0, read)
        }
    }
    return hasher.digest().bytes
}
