package me.him188.ani.app.videoplayer.data

import me.him188.ani.app.torrent.api.TorrentSession
import me.him188.ani.datasources.api.MediaExtraFiles
import me.him188.ani.utils.io.SeekableInput
import kotlin.coroutines.cancellation.CancellationException

/**
 * A source of the video data [S].
 *
 * [VideoSource]s are stateless: They only represent a location of the resource, not holding file descriptors or network connections, etc.
 *
 * ## Obtaining data stream
 *
 * To get the input stream of the video file, two steps are needed:
 * 1. Open a [VideoData] using [open].
 * 2. Use [VideoData.createInput] to get the input stream [SeekableInput].
 *
 * Note that both [VideoData] and [SeekableInput] are [AutoCloseable] and needs to be properly closed.
 *
 * In the BitTorrent scenario, [VideoSource.open] is to resolve magnet links, and to download the torrent metadata file.
 * [VideoData.createInput] is to start downloading the actual video file.
 * Though the actual implementation might start downloading very soon (e.g. when [VideoSource] is just created), so that
 * the video buffers more soon.
 *
 * @param S the type of the stream. For example, a torrent video source would be [TorrentSession].
 */
interface VideoSource<S : VideoData> {
    val uri: String

    val extraFiles: MediaExtraFiles

    /**
     * Opens the underlying video data.
     *
     * Note that [S] should be closed by the caller.
     *
     * Repeat calls to this function may return different instances so it may be desirable to store the result.
     *
     * @throws VideoSourceOpenException 当打开失败时抛出, 包含原因
     */
    @Throws(VideoSourceOpenException::class, CancellationException::class)
    suspend fun open(): S
}
