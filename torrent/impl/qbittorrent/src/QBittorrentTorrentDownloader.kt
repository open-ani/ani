package me.him188.ani.app.torrent.qbittorrent

import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.torrent.api.TorrentDownloadSession
import me.him188.ani.app.torrent.api.TorrentDownloader
import me.him188.ani.app.torrent.api.TorrentLibInfo
import me.him188.ani.app.torrent.api.files.EncodedTorrentInfo
import java.io.File
import kotlin.coroutines.CoroutineContext

/**
 * 连接 qBittorrent API 的下载器.
 */
class QBittorrentTorrentDownloader(
    private val url: String,
) : TorrentDownloader {
    override val totalUploaded: Flow<Long>
        get() = TODO("Not yet implemented")
    override val totalDownloaded: Flow<Long>
        get() = TODO("Not yet implemented")
    override val totalUploadRate: Flow<Long>
        get() = TODO("Not yet implemented")
    override val totalDownloadRate: Flow<Long>
        get() = TODO("Not yet implemented")
    override val vendor: TorrentLibInfo
        get() = TODO("Not yet implemented")

    override suspend fun fetchTorrent(uri: String, timeoutSeconds: Int): EncodedTorrentInfo {
        TODO("Not yet implemented")
    }

    override suspend fun startDownload(
        data: EncodedTorrentInfo,
        parentCoroutineContext: CoroutineContext
    ): TorrentDownloadSession {
        TODO("Not yet implemented")
    }

    override fun getSaveDir(data: EncodedTorrentInfo): File {
        TODO("Not yet implemented")
    }

    override fun listSaves(): List<File> {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}