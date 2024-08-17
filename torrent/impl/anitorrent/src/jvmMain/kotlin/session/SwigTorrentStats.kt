package me.him188.ani.app.torrent.anitorrent.session

import me.him188.ani.app.torrent.anitorrent.binding.torrent_stats_t

class SwigTorrentStats(
    private val native: torrent_stats_t,
) : TorrentStats {
    override val downloadPayloadRate: Long
        get() = native.download_payload_rate.toUInt().toLong()
    override val uploadPayloadRate: Long
        get() = native.upload_payload_rate.toUInt().toLong()
    override val progress: Float
        get() = native.progress
    override val totalPayloadUpload: Long
        get() = native.total_payload_upload
}
