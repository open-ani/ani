package me.him188.ani.app.torrent.anitorrent.session

interface TorrentStats {
    val downloadPayloadRate: Long
    val uploadPayloadRate: Long
    val progress: Float

    /**
     * 当任务被暂停时, 此数据将会清零.
     */
    val totalPayloadDownload: Long

    /**
     * 当任务被暂停时, 此数据将会清零.
     */
    val totalPayloadUpload: Long
}