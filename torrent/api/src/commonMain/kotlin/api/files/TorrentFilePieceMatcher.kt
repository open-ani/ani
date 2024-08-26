package me.him188.ani.app.torrent.api.files

import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.torrent.api.pieces.lastIndex
import me.him188.ani.app.torrent.api.pieces.startIndex

// TorrentFilePieceMatcherTest
object TorrentFilePieceMatcher {
    /**
     * @param allPieces all pieces in the torrent
     * @param offset of the file to match
     * @param length of the file to match
     * @return minimum number of pieces that cover the file offset and length,
     * guaranteed to be continuous and sorted
     */
    fun matchPiecesForFile(allPieces: List<Piece>, offset: Long, length: Long): List<Piece> {
        return allPieces.filter { piece ->
            piece.offset >= offset && piece.offset < offset + length
                    || (piece.offset < offset && piece.lastIndex >= offset)
        }.sortedBy { it.offset }.also { pieces ->
            // 检验 pieces 的大小等于文件大小
            if (pieces.isEmpty()) {
                if (length == 0L) {
                    return@also
                }
                throw IllegalStateException("No pieces found for file offset $offset and length $length")
            }

            // Check continuous
            pieces.forEachIndexed { index, piece ->
                if (index == 0) {
                    return@forEachIndexed
                }
                if (piece.offset != pieces[index - 1].lastIndex + 1) {
                    throw IllegalStateException("Pieces offset is not continuous: lastOffset ${pieces[index - 1].lastIndex + 1} -> currently visiting ${piece.offset}")
                }
            }

            check(pieces.last().lastIndex - pieces.first().startIndex + 1 >= length) {
                "Pieces size is less than file size: ${pieces.last().lastIndex - pieces.first().startIndex + 1} < $length"
            }

            check(pieces is RandomAccess)
        }
    }
}
