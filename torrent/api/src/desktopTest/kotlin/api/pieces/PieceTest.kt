package me.him188.ani.app.torrent.api.pieces

import kotlin.test.Test
import kotlin.test.assertEquals

internal class PieceTest {
    private val pieces = Piece.buildPieces(10) { 5 }

    @Test
    fun pieceIndex() {
        val list = pieces
        assertEquals("[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]", list.map { it.pieceIndex }.toString())
    }

    @Test
    fun startIndex() {
        val list = pieces
        assertEquals(listOf(0L, 5, 10, 15, 20, 25, 30, 35, 40, 45), list.map { it.startIndex })
    }

    @Test
    fun lastIndex() {
        val list = pieces
        assertEquals(listOf(4L, 9, 14, 19, 24, 29, 34, 39, 44, 49), list.map { it.lastIndex })
    }

    @Test
    fun indexes() {
        val list = pieces
        assertEquals(
            "[0..4, 5..9, 10..14, 15..19, 20..24, 25..29, 30..34, 35..39, 40..44, 45..49]",
            list.map { it.indexes }.toString(),
        )
    }
}