package me.him188.ani.utils.io

import kotlinx.io.IOException
import java.io.File
import java.io.RandomAccessFile


/**
 * Adapts this [RandomAccessFile] to a [SeekableInput].
 *
 * File reads are buffered.
 *
 * **The file length must not change** while it is created as a [SeekableInput], otherwise the behavior is undefined - it is not checked.
 *
 * By closing the returned [SeekableInput], you also close this [RandomAccessFile].
 * Conversely, by closing this [RandomAccessFile], you also close the returned [SeekableInput],
 * though it is not recommended to close the [RandomAccessFile] directly.
 *
 * The file is not open until first read.
 */
@Throws(IOException::class)
public fun File.toSeekableInput(
    bufferSize: Int = BufferedInput.DEFAULT_BUFFER_PER_DIRECTION,
    onFillBuffer: (() -> Unit)? = null,
): SeekableInput = BufferedFileInput(
    RandomAccessFile(this, "r"),
    bufferSize,
    onFillBuffer,
)

internal open class BufferedFileInput(
    private val file: RandomAccessFile,
    private val bufferSize: Int = DEFAULT_BUFFER_PER_DIRECTION,
    private val onFillBuffer: (() -> Unit)? = null,
) : BufferedInput(bufferSize) {
    override val size: Long get() = file.length()

    override fun fillBuffer() {
        onFillBuffer?.invoke()

        val fileLength = this.size
        val pos = this.position

        val readStart = (pos - bufferSize).coerceAtLeast(0)
        val readEnd = (pos + bufferSize).coerceAtMost(fileLength)

        fillBufferRange(readStart, readEnd)
    }

    override fun readFileToBuffer(fileOffset: Long, bufferOffset: Int, length: Int): Int {
        val file = this.file
        file.seek(fileOffset)
        file.readFully(buf, bufferOffset, length)
        return length

//        var read = bufferOffset
//        while (read <= bufferOffset + length) {
//            read += file.read(buf, read, length - read)
//        }
//        return read
    }

    override fun toString(): String {
        return "BufferedFileInput(file=$file, position=$position, bytesRemaining=$bytesRemaining)"
    }

    override fun close() {
        super.close()
        file.close()
    }
}
