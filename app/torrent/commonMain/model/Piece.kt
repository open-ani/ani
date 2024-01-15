package me.him188.ani.app.torrent.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import me.him188.ani.app.torrent.PieceState

public class Piece(
    public val pieceIndex: Int,
    public val size: Long,
    public val offset: Long,
) {
    public val state: MutableStateFlow<PieceState> = MutableStateFlow(PieceState.READY)

    public companion object {
        public fun buildPieces(numPieces: Int, getPieceSize: (index: Int) -> Long): List<Piece> = buildList(numPieces) {
            var pieceOffset = 0L
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

public val Piece.startIndex: Long get() = offset
public val Piece.lastIndex: Long get() = offset + size - 1
public inline val Piece.indexes: LongRange get() = startIndex..lastIndex

public suspend inline fun Piece.awaitFinished() {
    val piece = this
    if (piece.state.value != PieceState.FINISHED) {
        piece.state.takeWhile { it != PieceState.FINISHED }.collect()
    }
}
