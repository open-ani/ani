package me.him188.ani.app.torrent.api.files

interface TorrentInfo {
    val name: String
    val infoHashHex: String

    val fileCount: Int
}

@JvmInline
value class EncodedTorrentInfo(
    val data: ByteArray,
)
