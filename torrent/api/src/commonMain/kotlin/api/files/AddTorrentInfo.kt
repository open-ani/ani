package me.him188.ani.app.torrent.api.files

import me.him188.ani.app.torrent.api.TorrentDownloader
import kotlin.jvm.JvmInline

interface AddTorrentInfo

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
