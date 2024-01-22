package me.him188.ani.app.torrent.file

import me.him188.ani.app.torrent.PieceState
import me.him188.ani.app.torrent.model.Piece
import me.him188.ani.app.torrent.model.awaitFinished
import me.him188.ani.app.torrent.model.lastIndex
import me.him188.ani.app.torrent.model.startIndex


/**
 * A [SeekableInput] that reads from a torrent save file.
 *
 * It takes the advantage of the fact that the torrent save file is a concatenation of all pieces,
 * and awaits [Piece]s to be finished when they are sought and read.
 */
internal class TorrentInput(
    /**
     * The torrent save file.
     */
    private val file: SeekableInput,
    /**
     * The corresponding pieces of the [file].
     */
    private val pieces: List<Piece>,
) : SeekableInput {
    private val totalLength = pieces.maxOf { it.offset + it.size }

    override var offset: Long = 0
    override val bytesRemaining: Long get() = (totalLength - offset).coerceAtLeast(0)

    override suspend fun seek(offset: Long) {
        seekImpl(offset)
    }

    /**
     * Returns max bytes available for read without suspending to wait for more piece to be downloaded.
     */
    private suspend fun seekImpl(offset: Long): Long {
        this.offset = offset
        val index = findPiece(offset)
        if (index == -1) {
            throw IllegalStateException("offset $offset is not in any piece")
        }
        val piece = pieces[index]
        if (piece.state.value != PieceState.FINISHED) {
            piece.awaitFinished()
        }
        file.seek(offset)
        val pieceOffset = offset - piece.offset
        return piece.size - pieceOffset
    }

    internal fun findPiece(offset: Long) = pieces.indexOfFirst { it.startIndex <= offset && offset <= it.lastIndex }

    override suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val pieceAvailableSize = seekImpl(this.offset)
        val maxRead = length.toUInt().toLong().coerceAtMost(pieceAvailableSize)
        return file.read(buffer, offset, maxRead.toInt()).also {
            this@TorrentInput.offset += it
        }
    }

    override fun close() {
        this.file.close()
    }
}