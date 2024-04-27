package me.him188.ani.app.torrent.api.pieces

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile
import me.him188.ani.app.torrent.api.PieceState

class Piece(
    /**
     * 是种子信息中的第几个
     */
    val pieceIndex: Int,
    /**
     * 该 piece 的数据长度 bytes
     */
    val size: Long,
    /**
     * 在种子所能下载的所有文件数据中的 offset bytes
     */
    val offset: Long,
) {
    val state: MutableStateFlow<PieceState> = MutableStateFlow(PieceState.READY)
    val downloadedBytes: Flow<Long>
        get() = state.map {
            if (it == PieceState.FINISHED) size else 0L
        }

    companion object {
        fun buildPieces(
            numPieces: Int,
            initial: Long = 0L,
            getPieceSize: (index: Int) -> Long,
        ): List<Piece> = buildList(numPieces) {
            var pieceOffset = initial
            for (i in 0 until numPieces) {
                val pieceSize = getPieceSize(i)
                val piece = Piece(
                    pieceIndex = i,
                    size = pieceSize,
                    offset = pieceOffset,
                )
                add(piece)
                pieceOffset += pieceSize
            }
        }

        fun buildPieces(
            totalSize: Long,
            pieceSize: Long,
            initial: Long = 0L,
        ) = buildPieces((totalSize / pieceSize).toInt(), initial) { pieceSize }
            .let {
                it + Piece(
                    pieceIndex = it.size,
                    size = totalSize % pieceSize,
                    offset = totalSize - (totalSize % pieceSize) + initial
                )
            }
    }

    override fun toString(): String {
        return "Piece($offset..$lastIndex)"
    }
}

val Piece.startIndex: Long get() = offset
val Piece.lastIndex: Long get() = offset + size - 1
inline val Piece.indexes: LongRange get() = startIndex..lastIndex

suspend inline fun Piece.awaitFinished() {
    val piece = this
    if (piece.state.value != PieceState.FINISHED) {
        piece.state.takeWhile { it != PieceState.FINISHED }.collect()
    }
}
