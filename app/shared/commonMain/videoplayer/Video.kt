package me.him188.ani.app.videoplayer

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.torrent.TorrentDownloadSession
import java.io.File

/**
 * 视频源
 */
interface Video : AutoCloseable {
    @Stable
    val file: File

    /**
     * 文件总大小
     */
    @Stable
    val totalBytes: Flow<Long>

    @Stable
    val downloadedBytes: Flow<Long>

    /**
     * 下载速度 Bps
     */
    @Stable
    val downloadRate: Flow<Long>

    /**
     * 下载进度 `0..1`
     */
    @Stable
    val downloadProgress: Flow<Float>

    /**
     * 视频长度秒数
     */
    @Stable
    val length: Flow<Int>

    // temporarily for debug
    val torrentSource: TorrentDownloadSession?

    override fun close()
}