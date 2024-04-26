package me.him188.ani.app.torrent

import kotlinx.coroutines.flow.StateFlow
import java.io.File


/**
 * 表示一整个 BT 资源的下载任务.
 *
 * @See TorrentDownloader
 */
interface TorrentDownloadSession : AutoCloseable {
    val state: StateFlow<TorrentDownloadState>

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

