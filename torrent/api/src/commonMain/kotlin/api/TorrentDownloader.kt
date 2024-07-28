package me.him188.ani.app.torrent.api

import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.torrent.api.files.EncodedTorrentInfo
import me.him188.ani.utils.io.SystemPath
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


/**
 * 下载管理器, 支持根据磁力链解析[种子信息][EncodedTorrentInfo], 然后根据种子信息创建下载会话 [TorrentDownloadSession].
 *
 * Must be closed when it is no longer needed.
 */
interface TorrentDownloader : AutoCloseable {
    /**
     * Total amount of bytes uploaded
     */
    val totalUploaded: Flow<Long>
    val totalDownloaded: Flow<Long>

    val totalUploadRate: Flow<Long>
    val totalDownloadRate: Flow<Long>

    /**
     * Details about the underlying torrent library.
     */
    val vendor: TorrentLibInfo

    /**
     * Fetches a magnet link.
     *
     * @param uri supports magnet link or http link for the torrent file
     *
     * @throws FetchTorrentTimeoutException if timeout has been reached.
     */
    suspend fun fetchTorrent(uri: String, timeoutSeconds: Int = 60): EncodedTorrentInfo

    /**
     * Starts download of a torrent using the torrent data.
     *
     * This function may involve I/O operation e.g. to compare with local caches.
     *
     * @param overrideSaveDir 覆盖的保存目录. 注意, 若这是季度全集资源, 他可能会在 qBit 平台上出问题.
     * 因为 qBit 依赖保存目录获取已有资源.
     * 于是这个参数目前一直没使用.
     */
    suspend fun startDownload(
        data: EncodedTorrentInfo,
        parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
        overrideSaveDir: SystemPath? = null,
    ): TorrentDownloadSession

    fun getSaveDirForTorrent(
        data: EncodedTorrentInfo,
    ): SystemPath

    /**
     * 获取所有的种子保存目录列表
     */
    fun listSaves(): List<SystemPath>

    override fun close()
}

class FetchTorrentTimeoutException(
    override val message: String? = "Magnet fetch timeout",
    override val cause: Throwable? = null
) : Exception()

/**
 * 用于下载 `https://xxx.torrent`
 */
interface HttpFileDownloader : AutoCloseable {
    suspend fun download(url: String): ByteArray
}

class TorrentDownloaderConfig(
    val peerFingerprint: String = "-aniLT3000-",
    val userAgent: String = "ani_libtorrent/3.0.0", // "libtorrent/2.1.0.0", "ani_libtorrent/3.0.0"
    val handshakeClientVersion: String? = "3.0.0",
    @Deprecated("to be removed by 3.5.0")
    val isDebug: Boolean = false,
) {
    companion object {
        val Default: TorrentDownloaderConfig = TorrentDownloaderConfig()
    }
}
