package me.him188.ani.app.torrent.io

import kotlinx.coroutines.runBlocking
import kotlinx.io.IOException
import me.him188.ani.app.torrent.api.files.PieceState
import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.torrent.api.pieces.awaitFinished
import me.him188.ani.app.torrent.api.pieces.lastIndex
import me.him188.ani.app.torrent.api.pieces.startIndex
import me.him188.ani.utils.io.BufferedInput
import me.him188.ani.utils.io.SeekableInput
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.io.toFile
import me.him188.ani.utils.platform.annotations.Range
import java.io.RandomAccessFile

@Suppress("FunctionName")
@Throws(IOException::class)
actual fun TorrentInput(
    file: SystemPath,
    pieces: List<Piece>, // must support random access
    logicalStartOffset: Long, // 默认为第一个 piece 开头
    onWait: suspend (Piece) -> Unit,
    bufferSize: Int,
    size: Long,
): SeekableInput =
    TorrentInput(RandomAccessFile(file.toFile(), "r"), pieces, logicalStartOffset, onWait, bufferSize, size)


/**
 * A [SeekableInput] that reads from a torrent save file.
 *
 * It takes the advantage of the fact that the torrent save file is a concatenation of all pieces,
 * and awaits [Piece]s to be finished when they are sought and read.
 *
 * 即使 [pieces] 的起始不为 0, [SeekableInput.position] 也是从 0 开始.
 */
// tests: me.him188.ani.app.torrent.io.OffsetTorrentInputTest and me.him188.ani.app.torrent.io.TorrentInputTest
class TorrentInput(
    /**
     * The torrent save file.
     */
    private val file: RandomAccessFile,
    /**
     * The corresponding pieces of the [file], must contain all bytes in the [file].
     *
     * 不需要排序.
     */
    private val pieces: List<Piece>, // must support random access
    /**
     * 逻辑上的偏移量, 也就是当 [seek] `k` 时, 实际上是在 `logicalStartOffset + k` 处.
     *
     * 这里的 "逻辑上" 的第一个 piece 指的是包含文件的第一个 byte 的 piece.
     */
    private val logicalStartOffset: Long = pieces.minOf { it.offset }, // 默认为第一个 piece 开头
    private val onWait: suspend (Piece) -> Unit = { },
    /**
     * 每个方向 (前/后) 的最大的 buffer 大小.
     *
     * 每次读取时会等待当前的 piece 完成后读取, 还会同时检查前后相邻 piece 是否也完成了, 如果完成就会把它们也读进来, 减少 IO 次数.
     * 因此 [bufferSize] 指定的是最大大小. 不会因为过大的 [bufferSize] 而导致等待更多 piece 完成.
     */
    private val bufferSize: Int = DEFAULT_BUFFER_PER_DIRECTION,
    override val size: Long = file.length()
) : BufferedInput(bufferSize) {

    // exclusive
    private val logicalLastOffset = logicalStartOffset + size - 1

    init {
        require(pieces is RandomAccess) {
            "pieces must support random access otherwise the performance will be bad"
        }

        val pieceSum = pieces.maxOf { it.offset + it.size } - logicalStartOffset
        check(pieceSum >= size) {
            "file length ${file.length()} is larger than pieces' range $pieceSum"
        }
        check(findPieceIndex(0) != -1) {
            "logicalStartOffset $logicalStartOffset is not in any piece"
        }
        check(findPieceIndex(file.length() - 1) != -1) {
            "last file pos is not in any piece, maybe because pieces range is too small than file length"
        }
    }

    override fun fillBuffer() {
        val fileLength = this.size
        val pos = this.position


        // 保证当前位置的 piece 已完成
        val index = findPieceIndexOrFail(pos)
        val piece = pieces[index]
        if (piece.state.value != PieceState.FINISHED) {
            runBlocking {
                onWait(piece)
                piece.awaitFinished()
            }
        }

        // 当前 piece 已经完成, 可以读取
        // 检查一下前后相邻 piece 是不是也已经完成了, 如果完成了就顺便读进来, 减少 IO 次数
        val maxBackward = computeMaxBufferSizeBackward(pos, bufferSize.toLong(), piece = piece)
        val maxForward = computeMaxBufferSizeForward(pos, bufferSize.toLong(), piece = piece)

        val readStart = (pos - maxBackward).coerceAtLeast(0)
        val readEnd = (pos + maxForward).coerceAtMost(fileLength)

        fillBufferRange(readStart, readEnd)
    }

    override fun readFileToBuffer(fileOffset: Long, bufferOffset: Int, length: Int): Int {
        val file = this.file
        file.seek(fileOffset)
        file.readFully(buf, bufferOffset, length)
        return length
    }

    /**
     * 计算从 [viewOffset] 开始, 可以继续读取而不会读到未下载完成的 piece 的最大字节数, cap 到 [cap].
     * 会包含 [viewOffset].
     */
    @Suppress("SameParameterValue")
    internal fun computeMaxBufferSizeForward(
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
            // coerceAtMost(logicalLastOffset) is essential to skip garbage
            val length = curr.lastIndex.coerceAtMost(logicalLastOffset) - currOffset + 1
            accSize += length

            if (accSize >= cap) return cap

            val next = pieces.getOrNull(curr.pieceIndex + 1) ?: return accSize
            currOffset = curr.lastIndex.coerceAtMost(logicalLastOffset) + 1
            curr = next
        }
    }

    /**
     * 从 [viewOffset] 开始, 可以往回读取而不会读到未下载完成的 piece 的最大字节数, cap 到 [cap].
     *
     * 不包含 [viewOffset] 自己.
     */
    @Suppress("SameParameterValue")
    internal fun computeMaxBufferSizeBackward(
        viewOffset: Long,
        cap: Long,
        piece: Piece = pieces[findPieceIndex(viewOffset)] // you can pass if you already have it. not checked though.
    ): Long {
        require(cap > 0) { "cap must be positive, but was $cap" }
        require(viewOffset >= 0) { "viewOffset must be non-negative, but was $viewOffset" }

        // view : 1000..2000
        // pieces: 1000..1015, 1016..1031, 1032..1047
        // logicalStartOffset: 1008

        // viewOffset: 18
        // logicalOffset: 1008 + 18 = 1026
        // 1026 - 1016 = 10

        var curr = piece
        var currOffset = logicalStartOffset + viewOffset
        var accSize = 0L
        while (true) {
            if (curr.state.value != PieceState.FINISHED) return accSize
            val length = currOffset - curr.startIndex.coerceAtLeast(logicalStartOffset)
            accSize += length

            if (accSize >= cap) return cap

            val next = pieces.getOrNull(curr.pieceIndex - 1) ?: return accSize
            currOffset = curr.startIndex.coerceAtLeast(logicalStartOffset)
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

    // 一般来说十几次比较就可以找到
    internal fun findPieceIndex(viewOffset: Long): @Range(from = -1L, to = Long.MAX_VALUE) Int {
        require(viewOffset >= 0) { "viewOffset must be non-negative, but was $viewOffset" }

        val logicalOffset = logicalStartOffset + viewOffset

        return pieces.binarySearch {
            when {
                it.startIndex > logicalOffset -> 1
                it.lastIndex < logicalOffset -> -1
                else -> 0
            }
        }
    }

    override fun close() {
        super.close()
        file.close()
    }
}