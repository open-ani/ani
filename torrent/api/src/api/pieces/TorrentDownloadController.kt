package me.him188.ani.app.torrent.api.pieces

import me.him188.ani.app.torrent.api.files.PieceState

/**
 * Torrent 下载优先级控制器.
 *
 * 下载有两个阶段:
 * 1. Metadata: 下载视频文件首尾的元数据, 使播放器尽快初始化.
 * 2. Sequential: 顺序下载视频文件的中间部分.
 *
 * 通过设置播放需要的 pieces 为最高优先级, 设置其他所有 pieces 为忽略来确保 libtorrent 优先下载所需的 pieces.
 *
 * ## 索引窗口
 *
 * 该控制器会维护一个索引窗口, 请求的所有 pieces 的索引都在这个窗口内. 当窗口内的首部 pieces 下载完成后, 窗口才会向后移动. 若窗口内任意非首部 pieces 下载完成, 则不会移动窗口.
 *
 * 这是为了让 libtorrent 专注于下载最影响当前播放体验的区块, 否则 libtorrent 为了整体下载速度, 可能会导致即将要播放的区块下载缓慢.
 *
 * ### 示例
 *
 * ```
 * // 假设 10 个 pieces, 窗口大小为 3, 当前请求了 3-5:
 *
 * 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
 *       |-----|
 *
 * // 假设 4 下载完成. 因为 4 不是窗口内的第一个 piece, 所以窗口不变, 不会请求更多的 piece.
 * // 假设 3 下载完成, 因为 3 是窗口的第一个 piece, 窗口会前进:
 *
 * 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
 *          |-----|
 *
 * // 由于 4 也已经下载完成了, 窗口继续前进, 直到一个没有下载 piece:
 *
 * 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
 *             |-----|
 *
 * // 现在将会请求下载 pieces 5-7.
 * ```
 *
 *
 * @param windowSize 窗口大小, 即请求的最后一个 piece 的索引与第一个 piece 的索引之差的最大值.
 * @param headerSize 将文件首部多少字节作为 metadata, 在 metadata 阶段请求
 * @param footerSize 将文件尾部多少字节作为 metadata, 在 metadata 阶段请求
 */
class TorrentDownloadController(
    private val pieces: List<Piece>, // sorted
    private val priorities: PiecePriorities,
    private val windowSize: Int = 8,
    private val headerSize: Long = 128 * 1024,
    private val footerSize: Long = 128 * 1024,
) {
    private val totalSize: Long = pieces.sumOf { it.size }

    private val footerPieces = pieces.dropWhile { it.lastIndex < totalSize - footerSize }


    private val lastIndex = pieces.indexOfFirst { it.lastIndex >= totalSize - footerSize } - 1

    private var currentWindowStart = 0

    // inclusive
    private var currentWindowEnd = (currentWindowStart + windowSize - 1).coerceAtMost(lastIndex)

    private var downloadingPieces: MutableList<Int> =
        (currentWindowStart until (currentWindowStart + windowSize).coerceAtMost(lastIndex)).toMutableList()


    @Synchronized
    fun isDownloading(pieceIndex: Int): Boolean {
        return downloadingPieces.contains(pieceIndex)
    }

    @Synchronized
    fun onTorrentResumed() {
        priorities.downloadOnly(downloadingPieces)
    }

    @Synchronized
    fun onSeek(pieceIndex: Int) {
        downloadingPieces.clear()
        currentWindowEnd = pieceIndex - 1
        fillWindow(pieceIndex)
        priorities.downloadOnly(downloadingPieces)
    }

    /**
     * 找接下来最近的还未完成的 piece, 如果没有, 返回 [startIndex]
     */
    private fun findNextDownloadingPiece(startIndex: Int): Int {
        for (index in (startIndex + 1)..lastIndex) {
            if (pieces[index].state.value != PieceState.FINISHED) {
                return index
            }
        }
        return startIndex
    }

    @Synchronized
    fun onPieceDownloaded(pieceIndex: Int) {
        if (!downloadingPieces.remove(pieceIndex)) {
            return
        }

        val newWindowEnd = findNextDownloadingPiece(currentWindowEnd)
        if (newWindowEnd != currentWindowEnd) {
            downloadingPieces.add(newWindowEnd)
            currentWindowEnd = newWindowEnd
        }
        priorities.downloadOnly(downloadingPieces)
    }

    private fun fillWindow(pieceIndex: Int) {
        val nextStartIndex = downloadingPieces.firstOrNull() ?: (currentWindowEnd + 1)
        val nextEndIndex = (nextStartIndex + windowSize - 1).coerceAtMost(lastIndex)
        downloadingPieces = (nextStartIndex..nextEndIndex).toMutableList()
        currentWindowStart = downloadingPieces.firstOrNull() ?: -1
        currentWindowEnd = downloadingPieces.lastOrNull() ?: -1
        if (pieceIndex <= 1) {
            // 正在下载第 0-1 个 piece, 说明我们刚刚开始下载视频, 需要额外请求尾部元数据
            addFooterPieces()
        }
    }

    private fun addFooterPieces() {
        for (footerPiece in footerPieces) {
            if (footerPiece.state.value != PieceState.FINISHED) {
                downloadingPieces.addIfNotExist(footerPiece.pieceIndex)
            }
        }
    }
}

private fun <E> MutableList<E>.addIfNotExist(pieceIndex: E) {
    if (!contains(pieceIndex)) {
        add(pieceIndex)
    }
}


interface PiecePriorities {
    /**
     * 设置仅下载指定的 pieces.
     */
    fun downloadOnly(pieceIndexes: Collection<Int>)
}
