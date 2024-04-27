package me.him188.ani.app.torrent.file

import kotlinx.coroutines.runBlocking
import me.him188.ani.app.torrent.api.PieceState
import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.torrent.api.pieces.awaitFinished
import me.him188.ani.app.torrent.api.pieces.lastIndex
import me.him188.ani.app.torrent.api.pieces.startIndex
import me.him188.ani.utils.io.SeekableInput
import org.jetbrains.annotations.Range
import java.io.IOException


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
    private val onWait: suspend (Piece) -> Unit = { }
) : SeekableInput {
    private val logicalStartOffset: Long = pieces.minOf { it.offset }
    private val totalLength = pieces.maxOf { it.offset + it.size } - logicalStartOffset

    override var offset: Long = 0 // view
    override val bytesRemaining: Long get() = (totalLength - offset).coerceAtLeast(0)

    @Throws(IOException::class)
    override fun seek(offset: Long, maxBuffer: Long) {
        checkClosed()
        seekImpl(offset, maxBuffer)
    }

    /**
     * Returns max bytes available for read without suspending to wait for more piece to be downloaded.
     */
    @Throws(IOException::class)
    private fun seekImpl(offset: Long, maxBuffer: Long): Long {
        require(offset >= 0) { "offset must be non-negative, but was $offset" }
        require(maxBuffer >= 1) { "maxBuffer must be >= 1, but was $maxBuffer" }

        this.offset = offset
        val index = findPieceIndexOrFail(offset)
        val piece = pieces[index]
        if (piece.state.value != PieceState.FINISHED) {
            runBlocking {
                onWait(piece)
                piece.awaitFinished()
            }
        }

        file.seek(
            offset,
            maxBuffer = computeMaxBufferSize(
                offset,
                cap = (8192 * 16L)
                    .coerceAtMost(maxBuffer),
                piece
            ).coerceAtLeast(1)
        )
        val offsetInPiece = logicalStartOffset + offset - piece.offset
        return piece.size - offsetInPiece
    }

    /**
     * 计算从 [viewOffset] 开始,
     */
    @Suppress("SameParameterValue")
    internal fun computeMaxBufferSize(
        viewOffset: Long,
        cap: Long,
        piece: Piece = pieces[findPieceIndex(viewOffset)] // you can pass if you already have it. not checked though.
    ): Long {
        require(cap > 0) { "cap must be positive, but was $cap" }
        require(viewOffset >= 0) { "viewOffset must be non-negative, but was $viewOffset" }

        var curr = piece
        var currOffset = logicalStartOffset + viewOffset
        var accSize = 0L
        while (true) {
            if (curr.state.value != PieceState.FINISHED) return accSize
            val length = curr.lastIndex - currOffset + 1
            accSize += length

            if (accSize >= cap) return cap

            val next = pieces.getOrNull(curr.pieceIndex + 1) ?: return accSize
            currOffset = curr.lastIndex + 1
            curr = next
        }
    }

    /**
     * @throws IllegalArgumentException
     */
    private fun findPieceIndexOrFail(viewOffset: Long): @Range(from = 0L, to = Long.MAX_VALUE) Int {
        val index = findPieceIndex(viewOffset)
        if (index == -1) {
            throw IllegalArgumentException("offset $viewOffset is not in any piece")
        }
        return index.also {
            check(it >= 0) { "findPieceIndex returned a negative index: $it" }
        }
    }

    internal fun findPieceIndex(viewOffset: Long): @Range(from = -1L, to = Long.MAX_VALUE) Int {
        require(viewOffset >= 0) { "viewOffset must be non-negative, but was $viewOffset" }
        val logicalOffset = logicalStartOffset + viewOffset
        return pieces.indexOfFirst { it.startIndex <= logicalOffset && logicalOffset <= it.lastIndex }
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        require(offset >= 0) { "offset must be non-negative, but was $offset" }
        require(length >= 0) { "length must be non-negative, but was $length" }
        checkClosed()

        val pieceAvailableSize = seekImpl(this.offset, maxBuffer = Long.MAX_VALUE)
        val maxRead = length.toUInt().toLong().coerceAtMost(pieceAvailableSize)
        return file.read(buffer, offset, maxRead.toInt()).also {
            this@TorrentInput.offset += it
        }
    }

    @Volatile
    private var closed = false
    private fun checkClosed() {
        if (closed) throw IllegalStateException("This SeekableInput is closed")
    }

    override fun close() {
        if (closed) return
        synchronized(this) {
            if (closed) return
            closed = true
        }
        this.file.close()
    }
}