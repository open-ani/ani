package me.him188.ani.app.torrent.libtorrent4j.files

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.him188.ani.app.torrent.api.files.EncodedTorrentInfo
import me.him188.ani.app.torrent.api.files.TorrentInfo
import me.him188.ani.app.torrent.api.handle.TorrentThread

class Torrent4jTorrentInfo
@TorrentThread
private constructor(
    override val originalUri: String? = null,
    val info: org.libtorrent4j.TorrentInfo
) : TorrentInfo {
    override val name: String = info.name()
    override val infoHashHex: String = info.infoHash().toHex()
    override val fileCount: Int = info.numFiles()

    companion object {
        private val json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        @OptIn(ExperimentalStdlibApi::class)
        @TorrentThread
        fun decodeFrom(encoded: EncodedTorrentInfo): Torrent4jTorrentInfo {
            kotlin.runCatching {
                json.decodeFromString(Torrent4jEncodedTorrentInfoSave.serializer(), encoded.data.decodeToString())
            }.getOrNull()?.let {
                // 新版本数据
                return Torrent4jTorrentInfo(
                    originalUri = it.originalUri,
                    info = org.libtorrent4j.TorrentInfo(it.torrentInfoData.hexToByteArray()),
                )
            }

            // 旧版本数据
            return Torrent4jTorrentInfo(
                originalUri = null,
                info = org.libtorrent4j.TorrentInfo(encoded.data),
            )
        }

        @OptIn(ExperimentalStdlibApi::class)
        fun encode(
            originalUri: String,
            torrentInfoData: ByteArray,
        ): EncodedTorrentInfo = EncodedTorrentInfo.createRaw(
            data = json.encodeToString(
                Torrent4jEncodedTorrentInfoSave.serializer(),
                Torrent4jEncodedTorrentInfoSave(originalUri, torrentInfoData.toHexString()),
            ).encodeToByteArray(),
        )
    }
}

@Serializable
private class Torrent4jEncodedTorrentInfoSave(
    val originalUri: String? = null,
    val torrentInfoData: String, // 真的 metadata
)
