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
import kotlin.test.assertFailsWith

@OptIn(RawPieceConstructor::class)
internal class PieceListTest {
    @Test
    fun `create single`() {
        val list = PieceList.create(
            1, 10L,
            getPieceSize = {
                100L
            },
        )
        assertEquals(0, list.initialPieceIndex)
        assertEquals(1, list.sizes.size)
        assertEquals(1, list.count)
        assertEquals(100L, list.totalSize)
        assertEquals(false, list.isEmpty())

        assertEquals(true, list.containsAbsolutePieceIndex(0))
        assertEquals(Piece(0), list.getByAbsolutePieceIndex(0))

        assertFailsWith<IndexOutOfBoundsException> {
            list.getByAbsolutePieceIndex(1)
        }

        list.run {
            val piece0 = getByAbsolutePieceIndex(0)
            assertEquals(0, piece0.pieceIndex)
            assertEquals(10, piece0.dataOffset)
            assertEquals(10, piece0.dataStartOffset)
            assertEquals(109, piece0.dataLastOffset)
            assertEquals(100, piece0.size)
        }

//        assertEquals(true, list.containsListIndex(1))
//        assertEquals(Piece(0), list.getByListIndex(0))
//        assertFailsWith<IndexOutOfBoundsException> {
//            list.getByListIndex(1)
//        }
    }

    @Test
    fun `create sequential`() {
        val list = PieceList.create(
            2, 10L,
            getPieceSize = {
                100L
            },
        )
        assertEquals(0, list.initialPieceIndex)
        assertEquals(2, list.sizes.size)
        assertEquals(2, list.count)
        assertEquals(200L, list.totalSize)
        assertEquals(false, list.isEmpty())

        assertEquals(true, list.containsAbsolutePieceIndex(0))
        assertEquals(Piece(0), list.getByAbsolutePieceIndex(0))

        assertEquals(true, list.containsAbsolutePieceIndex(1))
        assertEquals(Piece(1), list.getByAbsolutePieceIndex(1))

        assertFailsWith<IndexOutOfBoundsException> {
            list.getByAbsolutePieceIndex(2)
        }

        list.run {
            val piece = getByAbsolutePieceIndex(0)
            assertEquals(0, piece.pieceIndex)
            assertEquals(10, piece.dataOffset)
            assertEquals(10, piece.dataStartOffset)
            assertEquals(109, piece.dataLastOffset)
            assertEquals(100, piece.size)
        }

        list.run {
            val piece = getByAbsolutePieceIndex(1)
            assertEquals(1, piece.pieceIndex)
            assertEquals(110, piece.dataOffset)
            assertEquals(110, piece.dataStartOffset)
            assertEquals(110 + 100 - 1, piece.dataLastOffset)
            assertEquals(100, piece.size)
        }
    }

    @Test
    fun `getByAbsolutePieceIndex fails when -1`() {
        val list = PieceList.create(2, 10L, getPieceSize = { 100L })
        assertFailsWith<IndexOutOfBoundsException> {
            list.getByAbsolutePieceIndex(-1)
        }
    }

    @Test
    fun `getByAbsolutePieceIndex fails when OOB`() {
        val list = PieceList.create(2, 10L, getPieceSize = { 100L })
        assertFailsWith<IndexOutOfBoundsException> {
            list.getByAbsolutePieceIndex(2)
        }
    }

    @Test
    fun `getByAbsolutePieceIndex fails when OOB with initialPieceIndex`() {
        val list = PieceList.create(2, 10L, 5) { 100L }
        assertFailsWith<IndexOutOfBoundsException> {
            list.getByAbsolutePieceIndex(4)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            list.getByAbsolutePieceIndex(7)
        }
    }
}
