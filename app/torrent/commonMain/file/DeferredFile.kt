package me.him188.ani.app.torrent.file

import me.him188.ani.app.torrent.PieceState
import me.him188.ani.app.torrent.model.Piece
import me.him188.ani.app.torrent.model.awaitFinished
import me.him188.ani.app.torrent.model.lastIndex
import me.him188.ani.app.torrent.model.startIndex
import me.him188.ani.utils.logging.logger

public interface DeferredFile {
    public val offset: Long
    public val bytesRemaining: Long

    public suspend fun seek(offset: Long)

    public suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int
}

public suspend fun DeferredFile.readBytes(maxLength: Int = 4096): ByteArray {
    val buffer = ByteArray(maxLength)
    val actualLength = read(buffer, 0, maxLength)
    return if (actualLength != buffer.size) {
        buffer.copyOf(newSize = actualLength)
    } else {
        buffer
    }
}

internal class TorrentDeferredFileImpl(
    private val file: SeekableInput,
    private val pieces: List<Piece>,
) : DeferredFile {
    private companion object {
        private val logger = logger(TorrentDeferredFileImpl::class)
    }

    private val totalLength = pieces.maxOf { it.offset + it.size }

    override var offset: Long = 0
    override val bytesRemaining: Long get() = (totalLength - offset).coerceAtLeast(0)

    override suspend fun seek(offset: Long) {
        seekImpl(offset)
    }

    // Returns max read
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
            this@TorrentDeferredFileImpl.offset += it
        }
    }
}