package me.him188.ani.app.torrent.anitorrent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.him188.ani.app.torrent.api.files.EncodedTorrentInfo
import me.him188.ani.app.torrent.api.files.TorrentInfo

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
class AnitorrentTorrentInfo(
    val data: AnitorrentTorrentData,
    val httpTorrentFilePath: String? = null,
) : TorrentInfo {
    @Deprecated("Use data instead", ReplaceWith("data"))
    override val originalUri: Nothing? get() = null

    companion object {
        private val json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        fun decodeFrom(encoded: EncodedTorrentInfo): AnitorrentTorrentInfo {
            return json.decodeFromString(serializer(), encoded.data.decodeToString())
        }

        fun encode(
            info: AnitorrentTorrentInfo
        ): EncodedTorrentInfo = EncodedTorrentInfo.createRaw(
            data = json.encodeToString(serializer(), info).encodeToByteArray(),
        )
    }
}