package me.him188.ani.app.torrent.download

import me.him188.ani.app.torrent.model.Piece
import me.him188.ani.app.torrent.model.lastIndex

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
public class TorrentDownloadController(
    private val pieces: List<Piece>,
    private val priorities: PiecePriorities,
    private val windowSize: Int = 8,
    private val headerSize: Long = 128 * 1024,
    private val footerSize: Long = 128 * 1024,
) {
    private val totalSize: Long = pieces.sumOf { it.size }

    internal var state: State = State.Metadata(
        requestedPieces = (pieces.takeWhile { it.offset < headerSize } + pieces.dropWhile { it.lastIndex < totalSize - footerSize }).map { it.pieceIndex },
    )

    public val downloadingPieces: List<Int> get() = state.downloadingPieces.toList()

    @Synchronized
    public fun onTorrentResumed() {
        priorities.downloadOnly(state.downloadingPieces)
    }

    @Synchronized
    public fun onAllRequestedPiecesDownloaded() {
        for (downloadingPiece in state.downloadingPieces.toList()) { // avoid ConcurrentModificationException
            onPieceDownloaded(downloadingPiece)
        }
    }

    @Synchronized
    public fun onPieceDownloaded(pieceIndex: Int) {
        when (val state = state) {
            is State.Metadata -> {
                state.onPieceDownloaded(pieceIndex)
                if (state.allPiecesDownloaded()) {
                    this.state = State.Sequential(
                        pieces.indexOfFirst { it.offset >= headerSize },
                        pieces.indexOfFirst { it.lastIndex >= totalSize - footerSize } - 1,
                        windowSize = windowSize,
                    ).also {
                        priorities.downloadOnly(it.downloadingPieces)
                    }
                }
            }

            is State.Sequential -> {
                state.onPieceDownloaded(pieceIndex)
                priorities.downloadOnly(state.downloadingPieces)
                if (state.downloadingPieces.isEmpty()) {
                    this.state = State.Finished
                }
            }

            State.Finished -> {
            }
        }
    }

//    private val _debugInfo: MutableStateFlow<DebugInfo> = MutableStateFlow(DebugInfo())
//    public val debugInfo: StateFlow<DebugInfo> get() = _debugInfo

    @Synchronized
    public fun getDebugInfo(): DebugInfo {
        return DebugInfo(
            state = state::class.toString(),
            downloadingPieces = state.downloadingPieces.toList()
        )
    }

    public data class DebugInfo(
        val state: String,
        val downloadingPieces: List<Int>
    )
}


public interface PiecePriorities {
    /**
     * 设置仅下载指定的 pieces.
     */
    public fun downloadOnly(pieceIndexes: Collection<Int>)
}

internal sealed class State {
    abstract val downloadingPieces: List<Int>

    class Metadata(
        val requestedPieces: Collection<Int>,
    ) : State() {
        override val downloadingPieces: MutableList<Int> = requestedPieces.toMutableList()


        fun allPiecesDownloaded(): Boolean {
            return downloadingPieces.isEmpty()
        }

        fun onPieceDownloaded(pieceIndex: Int) {
            downloadingPieces.remove(pieceIndex)
        }

        override fun toString(): String {
            return "Metadata(requestedPieces=$requestedPieces, downloadingPieces=$downloadingPieces)"
        }
    }

    /**
     * 顺序下载状态
     *
     * @param startIndex 下载的起始 piece index, inclusive
     * @param lastIndex 下载的结束 piece index, inclusive
     */
    class Sequential(
        val startIndex: Int,
        val lastIndex: Int,
        val windowSize: Int,
    ) : State() {
        init {
            require(windowSize > 0) { "windowSize must be greater than 0" }
            require(startIndex >= 0) { "startIndex must be greater than or equal to 0" }
            require(lastIndex >= 0) { "lastIndex must be greater than or equal to 0" }
            require(startIndex <= lastIndex) { "startIndex must be less than or equal to lastIndex" }
        }

        internal var currentWindowStart = startIndex

        // inclusive
        internal var currentWindowEnd = (startIndex + windowSize - 1).coerceAtMost(lastIndex)

        override var downloadingPieces: MutableList<Int> =
            ((startIndex) until (startIndex + windowSize).coerceAtMost(lastIndex)).toMutableList()

        fun onPieceDownloaded(pieceIndex: Int) {
            downloadingPieces.remove(pieceIndex)
            if (downloadingPieces.isEmpty()) {
                requestMore()
                return
            }

            if (downloadingPieces.first() > currentWindowStart) {
                // window 首部的 piece 下载完成, 移动 window
                requestMore()
            }
        }

        private fun requestMore() {
            val nextStartIndex = downloadingPieces.firstOrNull() ?: (currentWindowEnd + 1)
            val nextEndIndex = (nextStartIndex + windowSize - 1).coerceAtMost(lastIndex)
            downloadingPieces = (nextStartIndex..nextEndIndex).toMutableList()

            currentWindowStart = downloadingPieces.firstOrNull() ?: -1
            currentWindowEnd = downloadingPieces.lastOrNull() ?: -1
        }

        override fun toString(): String {
            return "Sequential(startIndex=$startIndex, lastIndex=$lastIndex, windowSize=$windowSize, currentWindowStart=$currentWindowStart, currentWindowEnd=$currentWindowEnd, downloadingPieces=$downloadingPieces)"
        }


    }

    data object Finished : State() {
        override val downloadingPieces: List<Int> get() = emptyList()
    }
}