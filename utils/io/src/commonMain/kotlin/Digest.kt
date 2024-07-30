package me.him188.ani.utils.io

import kotlinx.io.Source

enum class DigestAlgorithm {
    MD5, SHA256
}

const val DEFAULT_BUFFER_SIZE: Int = 8 * 1024

expect fun Source.readAndDigest(algorithm: DigestAlgorithm): ByteArray
