package me.him188.ani.utils.io

import org.jetbrains.annotations.Range
import java.io.EOFException
import java.io.IOException

/**
 * A **asynchronous** source of bytes from which you can seek to a position and read sequentially.
 *
 * Note: this class is not thread-safe.
 */
public interface SeekableInput : AutoCloseable {
    /**
     * The current position in bytes from the start of the input source.
     *
     * Does not throw even if the input source is closed.
     */
    public val position: @Range(from = 0L, to = Long.MAX_VALUE) Long // get must be fast

    /**
     * The number of bytes remaining from the current position to the end of the input source.
     *
     * Does not throw even if the input source is closed.
     */
    public val bytesRemaining: @Range(from = 0L, to = Long.MAX_VALUE) Long // get must be fast

    public val size: @Range(from = 0L, to = Long.MAX_VALUE) Long

    /**
     * Seeks to the given offset in bytes from the start of the input source.
     *
     * This function suspends until the target byte at [seek] position is available to read.
     *
     * When this function returns, it moves [the current position][SeekableInput.position] to [position].
     *
     * @param position absolute offset in bytes from the start of the input source.
     *
     * @throws IllegalArgumentException if [position] is negative.
     * @throws IllegalStateException if the input source is closed.
     */
    @Throws(IOException::class)
    public fun seek(
        position: @Range(from = 0L, to = Long.MAX_VALUE) Long,
    )

    public fun prepareBuffer() {}

    /**
     * Reads up to [length] bytes from the input source into [buffer] starting at [offset].
     *
     * It is guaranteed that this function returns very quickly if there is at least one byte buffered,
     * and it tries to read at most buffer as possible.
     *
     * This function suspends until at least one byte is available to read,
     * and attempts to return as soon as possible after completing one read.
     * Read below for detailed explanation.
     *
     * This function moves moves the current position by the number of bytes read.
     *
     * ## Behaviour of asynchronous reading
     *
     * This function performs the read as soon as possible when there is at least one byte available,
     * and tries to read as much as possible (caped at [length]).
     *
     * **It is not guaranteed to read [length] bytes even if it does not reach the EOF.**
     *
     * If the length of the returned array is less than [length],
     * it does not necessarily mean that the EOF has been reached.
     * It may be because the underlying source just have that many bytes available **now**,
     * so this function read that available bytes and returns immediately without waiting for more bytes.
     *
     * @return the number of bytes read, or `-1` if the end of the input source has been reached.
     *
     * @throws IllegalStateException if the input source is closed.
     * @throws IllegalArgumentException if [offset] or [length] is negative.
     * @throws IllegalArgumentException if `offset + length` is greater than the size of [buffer].
     * @throws IOException if an I/O error occurs while reading from the input source.
     */
    @Throws(IOException::class)
    public fun read(
        buffer: ByteArray,
        offset: Int = 0,
        length: Int = buffer.size - offset
    ): Int // This must not be suspend because it can be called very frequently

    /**
     * Closes this [SeekableInput], and **also** closes the underlying source.
     *
     * Does nothing if this [SeekableInput] is already closed.
     */
    @Throws(IOException::class)
    public override fun close()
}

/**
 * Reads max [maxLength] bytes from this [SeekableInput], and advances the current position by the number of bytes read.
 *
 * Returns the bytes read.
 *
 * See [SeekableInput.read] for more details about the behaviour of asynchronous reading.
 */
public fun SeekableInput.readBytes(maxLength: Int = 4096): ByteArray {
    val buffer = ByteArray(maxLength)
    val actualLength = read(buffer, 0, maxLength)
    if (actualLength == -1) return ByteArray(0)
    return if (actualLength != buffer.size) {
        buffer.copyOf(newSize = actualLength)
    } else {
        buffer
    }
}

/**
 * 读取所剩的所有字节. 如果文件已经关闭, 会抛出异常 [IllegalStateException]
 */
public fun SeekableInput.readAllBytes(): ByteArray {
    val buffer = ByteArray(bytesRemaining.toInt())
    var offset = 0
    while (true) {
        val read = read(buffer, offset)
        if (read == -1) break
        offset += read
    }
    if (offset == buffer.size) return buffer
    return buffer.copyOf(newSize = offset)
}

/**
 * 读取 [n] 个字节.
 *
 * @throws EOFException when the file is shorter than [n] bytes.
 * @throws IllegalStateException if the input source is closed.
 */
@Throws(EOFException::class)
public fun SeekableInput.readExactBytes(
    n: Int
): ByteArray {
    val buffer = ByteArray(n)
    var offset = 0
    while (offset != buffer.size) {
        val read = read(buffer, offset)
        if (read == -1) break
        offset += read
    }
    if (offset == buffer.size) return buffer
    throw EOFException("Expected $n bytes, but only read $offset bytes")
}


//public fun ByteArray.asSeekableInput(): SeekableInput = object : SeekableInput {
//    private var offset = 0L
//
//    override suspend fun seek(offset: Long) {
//        this.offset = offset
//    }
//
//    override suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int {
//        val maxRead = length.toUInt().toLong().coerceAtMost(size - this.offset)
//        copyInto(buffer, offset, this.offset.toInt(), (this.offset + maxRead).toInt())
//        this.offset += maxRead
//        return maxRead.toInt()
//    }
//}