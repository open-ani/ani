package me.him188.ani.app.torrent

import me.him188.ani.app.torrent.api.pieces.Piece

class PiecesBuilder {

    private val pieces = mutableListOf<Piece>()

    fun piece(size: Long) {
        val piece = Piece(
            pieceIndex = pieces.size,
            size = size,
            offset = pieces.sumOf { it.size }
        )
        pieces.add(piece)
    }

    fun build(): List<Piece> = pieces.toList()
}

inline fun buildPieceList(block: PiecesBuilder.() -> Unit): List<Piece> {
    val builder = PiecesBuilder()
    builder.block()
    return builder.build()
}
