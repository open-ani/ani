package me.him188.ani.app.torrent.api.files

import me.him188.ani.app.torrent.api.TorrentDownloader

interface TorrentInfo {
    val name: String
    val infoHashHex: String

    val fileCount: Int

    /**
     * [TorrentDownloader.fetchTorrent] 的时候使用的 URI.
     * 对于旧版本数据此项为 `null`.
     */
    val originalUri: String?
}

@JvmInline
value class EncodedTorrentInfo private constructor(
    /**
     * 引擎保存的种子数据. 只能由对应的 [TorrentDownloader] 解析.
     */
    val data: ByteArray,
) {
    companion object {
        fun createRaw(data: ByteArray) = EncodedTorrentInfo(data)
    }
}
