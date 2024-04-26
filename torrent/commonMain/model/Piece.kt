package me.him188.ani.app.torrent.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile
import me.him188.ani.app.torrent.PieceState

class Piece(
    val pieceIndex: Int,
    val size: Long,
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
