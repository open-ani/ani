package me.him188.ani.app.torrent.api.files

interface TorrentInfo {
    val name: String
    val infoHashHex: String

    val fileCount: Int
}

@JvmInline
value class EncodedTorrentInfo(
    /**
     * Note: this data can only be used by the torrent library that created it.
     */
    val data: ByteArray,
)
