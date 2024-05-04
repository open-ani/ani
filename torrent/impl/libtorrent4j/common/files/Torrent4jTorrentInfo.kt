package me.him188.ani.app.torrent.libtorrent4j.files

import me.him188.ani.app.torrent.api.files.EncodedTorrentInfo
import me.him188.ani.app.torrent.api.files.TorrentInfo
import me.him188.ani.app.torrent.api.handle.TorrentThread


class Torrent4jTorrentInfo
@TorrentThread
private constructor(
    val info: org.libtorrent4j.TorrentInfo
) : TorrentInfo {
    override val name: String = info.name()
    override val infoHashHex: String = info.infoHash().toHex()
    override val fileCount: Int = info.numFiles()

    companion object {
        @TorrentThread
        fun decodeFrom(encoded: EncodedTorrentInfo): Torrent4jTorrentInfo =
            Torrent4jTorrentInfo(org.libtorrent4j.TorrentInfo(encoded.data))
    }
}