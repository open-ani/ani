package me.him188.ani.app.torrent

import kotlinx.coroutines.flow.StateFlow


/**
 * 表示一整个 BT 资源的下载任务.
 *
 * @See TorrentDownloader
 */
public interface TorrentDownloadSession : AutoCloseable {
    public val state: StateFlow<TorrentDownloadState>

    public val overallStats: DownloadStats

    /**
     * 获取该种子资源中的所有文件.
     */
    public suspend fun getFiles(): List<TorrentFileEntry>

    public override fun close()

    public fun closeIfNotInUse()
}

public sealed class TorrentDownloadState {
    /**
     * The session is ready and awaiting for the torrent to be added.
     */
    public data object Starting : TorrentDownloadState()

    /**
     * The torrent is being fetched from the network.
     *
     * Piece information may not be available yet.
     */
    public data object FetchingMetadata : TorrentDownloadState()

    /**
     * 当前可以下载文件. 注意, 可能有文件正在下载, 也可能没有.
     */
    public data object Downloading : TorrentDownloadState()

    /**
     * All pieces have been downloaded successfully.
     */
    public data object Closed : TorrentDownloadState()
}

