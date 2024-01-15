package me.him188.ani.app.videoplayer

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.torrent.TorrentDownloadSession
import java.io.File

/**
 * 视频源
 */
interface Video : AutoCloseable {
    /**
     * 视频文件. 文件大小会预分配.
     */
    @Stable
    val file: File

    /**
     * 文件总大小
     */
    @Stable
    val totalBytes: Flow<Long>

    /**
     * 总体已下载字节数. 为所有不连续区块的大小之和.
     */
    @Stable
    val downloadedBytes: Flow<Long>

    /**
     * 总体下载速度 Bps
     */
    @Stable
    val downloadRate: Flow<Long>

    /**
     * 总体的下载进度, 即 `downloadedBytes / totalBytes`. 这只能说明整个文件的多少比例被下载了, 会是不连续的. 顺序播放视频要使用 [playableByteIndex]
     */
    @Stable
    val overallDownloadProgress: Flow<Float>

    /**
     * 视频长度秒数
     */
    @Stable
    val length: Flow<Int>

    // temporarily for debug
    val torrentSource: TorrentDownloadSession?
//
//    /**
//     * 是否已经获取到视频首尾区块 (即可以让播放器去识别 metadata)
//     */
//    @Stable
//    val headersAvailable: Flow<Boolean>

    /**
     * 停止潜在的下载.
     */
    override fun close()
}