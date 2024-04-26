package me.him188.ani.app.torrent

import me.him188.ani.app.torrent.api.pieces.Piece

class PiecesBuilder(
    private val initialOffset: Long = 0
) {
    private val pieces = mutableListOf<Piece>()

    fun piece(size: Long) {
        val piece = Piece(
            pieceIndex = pieces.size,
            size = size,
            offset = pieces.sumOf { it.size } + initialOffset
        )
        pieces.add(piece)
    }

    fun build(): List<Piece> = pieces.toList()
}

inline fun buildPieceList(
    block: PiecesBuilder.() -> Unit
): List<Piece> = buildPieceList(0, block)

inline fun buildPieceList(
    initialOffset: Long = 0,
    block: PiecesBuilder.() -> Unit
): List<Piece> {
    val builder = PiecesBuilder(initialOffset)
    builder.block()
    return builder.build()
}
