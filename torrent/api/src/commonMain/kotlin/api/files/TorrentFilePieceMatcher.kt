/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.torrent.api.files

import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.torrent.api.pieces.PieceList
import me.him188.ani.app.torrent.api.pieces.first
import me.him188.ani.app.torrent.api.pieces.forEachIndexed
import me.him188.ani.app.torrent.api.pieces.indexOfFirst
import me.him188.ani.app.torrent.api.pieces.indexOfLast
import me.him188.ani.app.torrent.api.pieces.last
import me.him188.ani.app.torrent.api.pieces.slice

// TorrentFilePieceMatcherTest
object TorrentFilePieceMatcher {
    /**
     * @param allPieces all pieces in the torrent
     * @param offset of the file to match
     * @param length of the file to match
     * @return minimum number of pieces that cover the file offset and length,
     * guaranteed to be continuous and sorted
     */
    fun matchPiecesForFile(allPieces: PieceList, offset: Long, length: Long): PieceList = with(allPieces) {
//        .filter { piece ->
////                piece.dataOffset >= offset && piece.dataOffset < offset + length
////                        || (piece.dataOffset < offset && piece.dataLastOffset >= offset)
////            }
        val predicate: PieceList.(Piece) -> Boolean = { piece ->
            (piece.dataStartOffset >= offset && piece.dataStartOffset < offset + length)
                    || (piece.dataStartOffset < offset && piece.dataLastOffset >= offset)
        }
        val startIndex = allPieces.indexOfFirst(predicate)
        val endIndex = allPieces.indexOfLast(predicate)
        if (startIndex == -1 || endIndex == -1) {
            if (length == 0L) {
                return PieceList.Empty
            }
            throw IllegalStateException("No pieces found for file offset $offset and length $length")
        }
        allPieces.slice(
            startIndex = startIndex,
            endIndex = endIndex + 1,
        ).also { pieces ->
            // Check continuous
            pieces.forEachIndexed { index, piece ->
                if (index == 0) {
                    return@forEachIndexed
                }
                if (piece.dataStartOffset != pieces.getByPieceIndex(piece.pieceIndex - 1).dataLastOffset + 1) {
                    throw IllegalStateException(
                        "Pieces offset is not continuous: lastOffset " +
                                "${pieces.getByPieceIndex(index - 1).dataLastOffset + 1}" +
                                " -> currently visiting ${piece.dataOffsetRange}",
                    )
                }
            }

            check(pieces.last().dataLastOffset - pieces.first().dataStartOffset + 1 >= length) {
                "Pieces size is less than file size: ${pieces.last().dataLastOffset - pieces.first().dataStartOffset + 1} < $length"
            }
        }
    }
}
