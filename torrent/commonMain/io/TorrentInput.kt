package me.him188.ani.app.torrent.file

import me.him188.ani.app.torrent.api.PieceState
import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.torrent.api.pieces.awaitFinished
import me.him188.ani.app.torrent.api.pieces.lastIndex
import me.him188.ani.app.torrent.api.pieces.startIndex
import me.him188.ani.utils.io.SeekableInput


/**
 * A [SeekableInput] that reads from a torrent save file.
 *
 * It takes the advantage of the fact that the torrent save file is a concatenation of all pieces,
 * and awaits [Piece]s to be finished when they are sought and read.
 *
 * 即使 [pieces] 的起始不为 0, [SeekableInput.offset] 也是从 0 开始.
 */
internal class TorrentInput(
    /**
     * The torrent save file.
     */
    private val file: SeekableInput,
    /**
     * The corresponding pieces of the [io].
     */
    private val pieces: List<Piece>,
    private val onSeek: suspend (Piece) -> Unit = { }
) : SeekableInput {
    private val logicalStartOffset: Long = pieces.minOf { it.offset }
    private val totalLength = pieces.maxOf { it.offset + it.size } - logicalStartOffset

    override var offset: Long = 0 // view
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
            onSeek(piece)
            piece.awaitFinished()
        }
        file.seek(offset)
        val offsetInPiece = logicalStartOffset + offset - piece.offset
        return piece.size - offsetInPiece
    }

    internal fun findPiece(viewOffset: Long): Int {
        val logicalOffset = logicalStartOffset + viewOffset
        return pieces.indexOfFirst { it.startIndex <= logicalOffset && logicalOffset <= it.lastIndex }
    }

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