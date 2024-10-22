/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.torrent.api.pieces

import kotlin.test.Test
import kotlin.test.assertEquals

internal class PieceTest {
    private val pieces = PieceList.buildPieces(10) { 5 }

    @Test
    fun pieceIndex() {
        val list = pieces
        assertEquals(
            "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]",
            list.asSequence().map { it.pieceIndex }.toList().toString(),
        )
    }

    @Test
    fun startIndex() {
        val list = pieces
        assertEquals(
            listOf(0L, 5, 10, 15, 20, 25, 30, 35, 40, 45),
            with(list) { list.asSequence().map { it.dataStartOffset }.toList() },
        )
    }

    @Test
    fun lastIndex() {
        val list = pieces
        assertEquals(
            listOf(4L, 9, 14, 19, 24, 29, 34, 39, 44, 49),
            with(list) { list.asSequence().map { it.dataLastOffset }.toList() },
        )
    }

    @Test
    fun indexes() {
        val list = pieces
        assertEquals(
            "[0..4, 5..9, 10..14, 15..19, 20..24, 25..29, 30..34, 35..39, 40..44, 45..49]",
            with(list) { list.asSequence().map { it.offsetRange } }.toList().toString(),
        )
    }
}