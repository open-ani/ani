package me.him188.ani.app.torrent.api.files

import me.him188.ani.app.torrent.api.TorrentSession

/**
 * 表示一个打开的 [TorrentFileEntry].
 *
 * 每个 [TorrentFileEntry] 可以有多个 [TorrentFileHandle], 仅当所有 [TorrentFileHandle] 都被关闭或 [pause] 后, 文件的下载才会被停止.
 */
interface TorrentFileHandle {
    val entry: TorrentFileEntry

    /**
     * 恢复下载并设置优先级
     *
     * 注意, 设置低于 [FilePriority.NORMAL] 可能会导致下载速度缓慢
     *
     * @throws IllegalStateException 当已经 [close] 时抛出
     */
    fun resume(priority: FilePriority = FilePriority.NORMAL)

    /**
     * 暂停下载
     * @throws IllegalStateException 当已经 [close] 时抛出
     */
    fun pause()

    /**
     * 停止下载并关闭此 [TorrentFileHandle]. 后续将不能再 [resume] 或 [pause] 等.
     *
     * 若此 torrent 文件是其背后 [TorrentSession] 最后一个要关闭的 torrent 文件，则该函数会挂起，
     * 直到 [TorrentSession] 完全关闭.
     *
     * 如果 [close] 已经被调用过了, 本函数不会有任何效果.
     */
    suspend fun close()

    /**
     * [close] 并且删除文件. 如果已经 close 了, 本函数仍然会删除相关文件.
     */
    suspend fun closeAndDelete()
}
