package me.him188.ani.app.torrent

import me.him188.ani.utils.io.SeekableInput
import java.nio.file.Path

/**
 * 表示 BT 资源中的一个文件.
 *
 * 所有文件默认都没有开始下载, 需调用 [createHandle] 创建一个句柄, 并使用 [TorrentFileHandle.resume] 才会开始下载.
 * 当句柄被关闭后, 该文件的下载也会被停止.
 *
 * @see TorrentDownloadSession
 */
public interface TorrentFileEntry {
    /**
     * 该文件的下载数据
     */
    public val stats: DownloadStats

    /**
     * 文件数据长度. 注意, 这不是文件在硬盘上的大小. 在硬盘上可能会略有差别.
     */
    public val length: Long

    /**
     * 在种子资源中的相对目录. 例如 `01.mp4`, `TV/01.mp4`
     */
    public val filePath: String

    /**
     * 创建一个句柄, 以用于下载文件.
     */
    public suspend fun createHandle(): TorrentFileHandle

    /**
     * Awaits until the hash is available
     */
    public suspend fun getFileHash(): String

    /**
     * Returns the hash if available, otherwise `null`
     */
    public fun getFileHashOrNull(): String?

    /**
     * 绝对路径. 挂起直到文件路径可用 (即有任意一个 piece 下载完成时)
     */
    public suspend fun resolveFile(): Path

    /**
     * Opens the downloaded file as a [SeekableInput].
     */
    public suspend fun createInput(): SeekableInput
}

/**
 * [TorrentFileEntry] 的下载控制器.
 *
 * 每个 [TorrentFileEntry] 可以有多个 [TorrentFileHandle], 仅当所有 [TorrentFileHandle] 都被关闭或 [pause] 后, 文件的下载才会被停止.
 */
public interface TorrentFileHandle : AutoCloseable {
    public val entry: TorrentFileEntry

    /**
     * 恢复下载并设置优先级
     *
     * 注意, 设置低于 [FilePriority.NORMAL] 可能会导致下载速度缓慢
     *
     * @throws IllegalStateException 当已经 [close] 时抛出
     */
    public suspend fun resume(priority: FilePriority = FilePriority.NORMAL)

    /**
     * 暂停下载
     * @throws IllegalStateException 当已经 [close] 时抛出
     */
    public suspend fun pause()

    /**
     * 停止下载并关闭此 [TorrentFileHandle]. 后续将不能再 [resume] 或 [pause] 等.
     */
    public override fun close()
}