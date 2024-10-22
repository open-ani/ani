/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.torrent.api.files

import me.him188.ani.app.torrent.api.pieces.PieceList

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
        TODO("matchPiecesForFile")
//        return allPieces
//            .asSequence()
//            .filter { piece ->
//                piece.dataOffset >= offset && piece.dataOffset < offset + length
//                        || (piece.dataOffset < offset && piece.dataLastOffset >= offset)
//            }
//            .toMutableList()
//            .apply { sortBy { it.dataOffset } }
//            .also { pieces ->
//                // 检验 pieces 的大小等于文件大小
//                if (pieces.isEmpty()) {
//                    if (length == 0L) {
//                        return@also
//                    }
//                    throw IllegalStateException("No pieces found for file offset $offset and length $length")
//                }
//
//                // Check continuous
//                pieces.forEachIndexed { index, piece ->
//                    if (index == 0) {
//                        return@forEachIndexed
//                    }
//                    if (piece.dataOffset != pieces[index - 1].dataLastOffset + 1) {
//                        throw IllegalStateException("Pieces offset is not continuous: lastOffset ${pieces[index - 1].dataLastOffset + 1} -> currently visiting ${piece.dataOffset}")
//                    }
//                }
//
//                check(pieces.last().dataLastOffset - pieces.first().dataStartOffset + 1 >= length) {
//                    "Pieces size is less than file size: ${pieces.last().dataLastOffset - pieces.first().dataStartOffset + 1} < $length"
//                }
//
//                check(pieces is RandomAccess)
//            }
    }
}
