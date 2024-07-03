package me.him188.ani.app.torrent.anitorrent

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.him188.ani.app.torrent.anitorrent.binding.anitorrent
import me.him188.ani.app.torrent.anitorrent.binding.torrent_info_t
import me.him188.ani.app.torrent.api.files.EncodedTorrentInfo
import me.him188.ani.app.torrent.api.files.TorrentInfo

class AnitorrentTorrentInfo
private constructor(
    override val originalUri: String? = null,
    val native: torrent_info_t
) : TorrentInfo {
    override val name: String = native.name
    override val infoHashHex: String = native.infohash_hex
    override val fileCount: Int = native.file_count

    companion object {
        private val json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        @OptIn(ExperimentalStdlibApi::class)
        fun decodeFrom(encoded: EncodedTorrentInfo): AnitorrentTorrentInfo? {
            json.decodeFromString(AnitorrentTorrentInfoSave.serializer(), encoded.data.decodeToString()).let { save ->
                // 新版本数据
                val info = torrent_info_t()
                val nativeInfo = anitorrent.torrent_info_parse(info, save.torrentInfoData)
                if (!nativeInfo) {
                    return null
                }
                return AnitorrentTorrentInfo(
                    originalUri = save.originalUri,
                    native = info,
                )
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        fun encode(
            originalUri: String,
            torrentInfoData: ByteArray,
        ): EncodedTorrentInfo = EncodedTorrentInfo.createRaw(
            data = json.encodeToString(
                AnitorrentTorrentInfoSave.serializer(),
                AnitorrentTorrentInfoSave(originalUri, torrentInfoData.toHexString()),
            ).encodeToByteArray(),
        )
    }
}

@Serializable
private class AnitorrentTorrentInfoSave(
    val originalUri: String? = null,
    val torrentInfoData: String, // 真的 metadata
)
