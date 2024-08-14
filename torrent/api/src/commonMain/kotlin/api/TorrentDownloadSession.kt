package me.him188.ani.app.torrent.api

import me.him188.ani.app.torrent.api.files.DownloadStats
import me.him188.ani.app.torrent.api.files.TorrentFileEntry
import me.him188.ani.utils.io.SystemPath

/**
 * 表示一整个 BT 资源的下载任务.
 */
interface TorrentDownloadSession {
    val overallStats: DownloadStats

    val saveDirectory: SystemPath

    /**
     * 该下载任务的名称
     */
    suspend fun getName(): String

    /**
     * 获取该种子资源中的所有文件.
     */
    suspend fun getFiles(): List<TorrentFileEntry>

    suspend fun close()

    suspend fun closeIfNotInUse()
}
