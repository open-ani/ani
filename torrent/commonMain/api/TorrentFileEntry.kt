package me.him188.ani.app.torrent.api

import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.torrent.api.pieces.lastIndex
import me.him188.ani.app.torrent.api.pieces.startIndex
import me.him188.ani.utils.io.SeekableInput
import java.io.File

/**
 * 表示 BT 资源中的一个文件.
 *
 * 所有文件默认都没有开始下载, 需调用 [createHandle] 创建一个句柄, 并使用 [TorrentFileHandle.resume] 才会开始下载.
 * 当句柄被关闭后, 该文件的下载也会被停止.
 *
 * @see TorrentDownloadSession
 */
interface TorrentFileEntry { // 实现提示, 无 test mock
    /**
     * 该文件的下载数据
     */
    val stats: DownloadStats

    /**
     * 文件数据长度. 注意, 这不是文件在硬盘上的大小. 在硬盘上可能会略有差别.
     */
    val length: Long // get must be fast

    /**
     * 在种子资源中的相对目录. 例如 `01.mp4`, `TV/01.mp4`
     */
    val pathInTorrent: String

    /**
     * 获取与这个文件有关的所有 [Piece].
     *
     * 注意, 文件与 piece 的大小并不一定完全匹配. 文件的第一个字节可能不会是返回的第一个 piece 的第一个字节, 尾部同理.
     * 但不会返回一个完全不包含该文件数据的 piece.
     *
     * @throws IllegalStateException 当未匹配到正确大小的 pieces 时抛出
     */
    val pieces: List<Piece>

    /**
     * 创建一个句柄, 以用于下载文件.
     */
    fun createHandle(): TorrentFileHandle

    /**
     * Awaits until the hash is available
     */
    suspend fun computeFileHash(): String

    /**
     * Returns the hash if available, otherwise `null`
     */
    fun computeFileHashOrNull(): String?

    /**
     * 绝对路径. 挂起直到文件路径可用 (即有任意一个 piece 下载完成时)
     */
    suspend fun resolveFile(): File

    fun resolveFileOrNull(): File?

    /**
     * Opens the downloaded file as a [SeekableInput].
     */
    fun createInput(): SeekableInput
}

/**
 * [TorrentFileEntry] 的下载控制器.
 *
 * 每个 [TorrentFileEntry] 可以有多个 [TorrentFileHandle], 仅当所有 [TorrentFileHandle] 都被关闭或 [pause] 后, 文件的下载才会被停止.
 */
interface TorrentFileHandle : AutoCloseable {
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
     */
    override fun close()
}

// TorrentFilePieceMatcherTest
object TorrentFilePieceMatcher {
    /**
     * @param allPieces all pieces in the torrent
     * @param offset of the file to match
     * @param length of the file to match
     * @return minimum number of pieces that cover the file offset and length,
     * guaranteed to be continuous and sorted
     */
    fun matchPiecesForFile(allPieces: List<Piece>, offset: Long, length: Long): List<Piece> {
        return allPieces.filter { piece ->
            piece.offset >= offset && piece.offset < offset + length
                    || (piece.offset < offset && piece.lastIndex >= offset)
        }.sortedBy { it.offset }.also { pieces ->
            // 检验 pieces 的大小等于文件大小
            if (pieces.isEmpty()) {
                if (length == 0L) {
                    return@also
                }
                throw IllegalStateException("No pieces found for file offset $offset and length $length")
            }

            // Check continuous
            pieces.forEachIndexed { index, piece ->
                if (index == 0) {
                    return@forEachIndexed
                }
                if (piece.offset != pieces[index - 1].lastIndex + 1) {
                    throw IllegalStateException("Pieces offset is not continuous: lastOffset ${pieces[index - 1].lastIndex + 1} -> currently visiting ${piece.offset}")
                }
            }

            check(pieces.last().lastIndex - pieces.first().startIndex + 1 >= length) {
                "Pieces size is less than file size: ${pieces.last().lastIndex - pieces.first().startIndex + 1} < $length"
            }
        }
    }
}