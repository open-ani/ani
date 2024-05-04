package me.him188.ani.app.torrent.api

import kotlinx.coroutines.flow.StateFlow
import me.him188.ani.app.torrent.api.files.DownloadStats
import me.him188.ani.app.torrent.api.files.TorrentFileEntry
import java.io.File

/**
 * 表示一整个 BT 资源的下载任务.
 *
 * @See DefaultTorrentDownloadSession
 */
interface TorrentDownloadSession : AutoCloseable {

    val state: StateFlow<TorrentDownloadState> // not used, not tested

    val overallStats: DownloadStats

    val saveDirectory: File

    /**
     * 获取该种子资源中的所有文件.
     */
    suspend fun getFiles(): List<TorrentFileEntry>

    override fun close()

    fun closeIfNotInUse()
}

sealed class TorrentDownloadState {
    /**
     * The session is ready and awaiting for the torrent to be added.
     */
    data object Starting : TorrentDownloadState()

    /**
     * The torrent is being fetched from the network.
     *
     * Piece information may not be available yet.
     */
    data object FetchingMetadata : TorrentDownloadState()

    /**
     * 当前可以下载文件. 注意, 可能有文件正在下载, 也可能没有.
     */
    data object Downloading : TorrentDownloadState()

    /**
     * All pieces have been downloaded successfully.
     */
    data object Closed : TorrentDownloadState()
}

