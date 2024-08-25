package me.him188.ani.app.torrent.anitorrent.session

interface TorrentStats {
    val downloadPayloadRate: Long
    val uploadPayloadRate: Long
    val progress: Float

    val totalPayloadUpload: Long
}