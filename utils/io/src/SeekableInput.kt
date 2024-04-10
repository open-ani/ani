package me.him188.ani.utils.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.RandomAccessFile

/**
 * A **asynchronous** source of bytes from which you can seek to a position and read sequentially.
 */
public interface SeekableInput : AutoCloseable {
    /**
     * The current position in bytes from the start of the input source.
     *
     * Does not throw even if the input source is closed.
     */
    public val offset: Long

    /**
     * The number of bytes remaining from the current position to the end of the input source.
     *
     * Does not throw even if the input source is closed.
     */
    public val bytesRemaining: Long

    /**
     * Seeks to the given offset in bytes from the start of the input source.
     *
     * This function suspends until the target byte at [seek] position is available to read.
     *
     * When this function returns, it moves [the current position][SeekableInput.offset] to [offset].
     *
     * @param offset absolute offset in bytes from the start of the input source.
     *
     * @throws IllegalStateException if the input source is closed.
     */
    public suspend fun seek(offset: Long)

    /**
     * Reads up to [length] bytes from the input source into [buffer] starting at [offset].
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
     */
    public suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int

    /**
     * Closes this [SeekableInput], and **also** closes the underlying source.
     *
     * Does nothing if this [SeekableInput] is already closed.
     */
    @Throws(IOException::class)
    public override fun close()
}

/**
 * Adapts this [RandomAccessFile] to a [SeekableInput].
 *
 * By closing the returned [SeekableInput], you also close this [RandomAccessFile].
 * Conversely, by closing this [RandomAccessFile], you also close the returned [SeekableInput],
 * though it is not recommended to close the [RandomAccessFile] directly.
 */
@Throws(IOException::class)
public fun RandomAccessFile.asSeekableInput(): SeekableInput = object : SeekableInput {
    override val offset: Long = this@asSeekableInput.filePointer
    override val bytesRemaining: Long = this@asSeekableInput.length() - offset

    override suspend fun seek(offset: Long) {
        withContext(Dispatchers.IO) { this@asSeekableInput.seek(offset) }
    }

    override suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        return withContext(Dispatchers.IO) { this@asSeekableInput.read(buffer, offset, length) }
    }

    override fun close() {
        this@asSeekableInput.close()
    }

    override fun toString(): String {
        return "RandomAccessFileAsSeekableInput(${this@asSeekableInput})"
    }
}

/**
 * Reads max [maxLength] bytes from this [SeekableInput], and advances the current position by the number of bytes read.
 *
 * Returns the bytes read.
 *
 * See [SeekableInput.read] for more details about the behaviour of asynchronous reading.
 */
public suspend fun SeekableInput.readBytes(maxLength: Int = 4096): ByteArray {
    val buffer = ByteArray(maxLength)
    val actualLength = read(buffer, 0, maxLength)
    return if (actualLength != buffer.size) {
        buffer.copyOf(newSize = actualLength)
    } else {
        buffer
    }
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