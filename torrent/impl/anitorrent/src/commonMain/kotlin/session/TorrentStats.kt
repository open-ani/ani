package me.him188.ani.app.torrent.anitorrent.session

// torrent_stats_t
// 
interface TorrentStats {
    /**
     * the total number of bytes to download for this torrent. This
     * may be less than the size of the torrent in case there are
     * pad files. This number only counts bytes that will actually
     * be requested from peers.
     */
    val total: Long

    /**
     * the total number of bytes of the file(s) that we have. All this does
     * not necessarily has to be downloaded during this session (that's
     * ``total_payload_download``).
     */
    val totalDone: Long

//    val totalWanted: Long
//
//    /**
//     * the number of bytes we have downloaded, only counting the pieces that
//     * we actually want to download. i.e. excluding any pieces that we have
//     * but have priority 0 (i.e. not wanted).
//     * Once a torrent becomes seed, any piece- and file priorities are
//     * forgotten and all bytes are considered "wanted".
//     */
//    val totalWantedDone: Long

    /**
     * are accumulated upload and download payload byte counters. They are
     * saved in and restored from resume data to keep totals across sessions.
     */
    val allTimeUpload: Long
    val allTimeDownload: Long

    /**
     * the total transfer rate of payload only, not counting protocol
     * chatter. This might be slightly smaller than the other rates, but if
     * projected over a long time (e.g. when calculating ETA:s) the
     * difference may be noticeable.
     */
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