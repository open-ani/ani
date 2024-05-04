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

class Torrent4jTorrentInfo private constructor(
    val info: org.libtorrent4j.TorrentInfo
) : TorrentInfo {
    override val name: String get() = info.name()
    override val infoHashHex: String get() = info.infoHash().toHex()
    override val fileCount: Int get() = info.numFiles()

    companion object {
        fun decodeFrom(encoded: EncodedTorrentInfo): Torrent4jTorrentInfo =
            Torrent4jTorrentInfo(org.libtorrent4j.TorrentInfo(encoded.data))
    }
}