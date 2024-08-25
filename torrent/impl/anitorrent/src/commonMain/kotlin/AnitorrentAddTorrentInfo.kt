package me.him188.ani.app.torrent.anitorrent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.him188.ani.app.torrent.api.files.AddTorrentInfo
import me.him188.ani.app.torrent.api.files.EncodedTorrentInfo

@Serializable
sealed class AnitorrentTorrentData {
    @Serializable
    @SerialName("MagnetUri")
    class MagnetUri(val uri: String) : AnitorrentTorrentData()

    @Serializable
    @SerialName("TorrentFile")
    class TorrentFile(val data: ByteArray) : AnitorrentTorrentData()
}

@Serializable
class AnitorrentAddTorrentInfo(
    val data: AnitorrentTorrentData,
    val httpTorrentFilePath: String? = null,
) : AddTorrentInfo {
    companion object {
        private val json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        fun decodeFrom(encoded: EncodedTorrentInfo): AnitorrentAddTorrentInfo {
            return json.decodeFromString(serializer(), encoded.data.decodeToString())
        }

        fun encode(
            info: AnitorrentAddTorrentInfo
        ): EncodedTorrentInfo = EncodedTorrentInfo.createRaw(
            data = json.encodeToString(serializer(), info).encodeToByteArray(),
        )
    }
}