package me.him188.ani.utils.io

import org.jetbrains.annotations.TestOnly
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import kotlin.math.min


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
public fun File.toSeekableInput(): SeekableInput = BufferedFileInput(RandomAccessFile(this, "r"))

@JvmInline
public value class OffsetRange private constructor(
    private val packed: Long,
) {
    public constructor(start: Int, end: Int) : this(start.toLong() or (end.toLong() shl 32))

    public val start: Int get() = packed.toInt()
    public val end: Int get() = (packed ushr 32).toInt()
}

internal class BufferedFileInput(
    private val file: RandomAccessFile,
) : BufferedInput() {
    override val fileLength: Long get() = file.length()

    override fun fillBuffer() {
        val buf = this.buf
        val fileLength = this.fileLength
        val pos = this.position

        val readStart = (pos - BUFFER_PER_DIRECTION).coerceAtLeast(0)
        val readEnd = (pos + BUFFER_PER_DIRECTION).coerceAtMost(fileLength)

        val readLength = (readEnd - readStart).coerceToInt()

        val file = this.file
        file.seek(readStart)
        file.readFully(buf, 0, readLength)

        this.bufferedOffsetStart = readStart
        this.bufferedOffsetEndExcl = readEnd
    }

    override fun toString(): String {
        return "BufferedFileInput(file=$file, position=$position, bytesRemaining=$bytesRemaining)"
    }
}

public abstract class BufferedInput : SeekableInput {
    protected companion object {
        public const val BUFFER_PER_DIRECTION: Int = 8192

        public fun Long.coerceToInt(): Int {
            if (this > Int.MAX_VALUE) return Int.MAX_VALUE
            return this.toInt()
        }

        public fun Long.checkToInt(): Int {
            if (this > Int.MAX_VALUE) error("value is too large to fit in Int: $this")
            return this.toInt()
        }
    }

    protected abstract val fileLength: Long

    /**
     * @see buf 包含的数据的起始位置
     */
    protected var bufferedOffsetStart: Long = -1L

    /**
     * @see buf 包含的数据的结束位置 (exclusive)
     */
    protected var bufferedOffsetEndExcl: Long = -1L

    @get:TestOnly
    public val bufferedOffsetRange: LongRange get() = bufferedOffsetStart..<bufferedOffsetEndExcl

    protected var buf: ByteArray = ByteArray(BUFFER_PER_DIRECTION * 2)

    /**
     * 当前 user 读到的位置
     */
    final override var position: Long = 0

    final override val bytesRemaining: Long get() = fileLength - position

    final override fun seek(position: Long) {
        require(position >= 0) { "offset must be non-negative, but was $position" }

        checkClosed()

        val lastPos = this.position
        if (position == lastPos) {
            // seek 到当前位置
            return
        }

        this.position = position
    }

    /**
     * 当此函数返回时, 至少 [position] 位置的数据已经在 [buf] 中
     */
    protected abstract fun fillBuffer()

    final override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        require(offset >= 0) { "offset must be non-negative, but was $offset" }
        require(length >= 0) { "length must be non-negative, but was $length" }
        require(offset + length <= buffer.size) { "offset + length must be less than or equal to buffer size, but was ${offset + length} > ${buffer.size}" }
        checkClosed()

        if (this.position >= fileLength) return -1

        var read = readFromBuffer(length, buffer, offset)
        if (read != -1) return read // 已经从 buffer 读了

        fillBuffer()
        read = readFromBuffer(length, buffer, offset)
        check(read != -1) { "fillBuffer did not fill for $position" }

        return read
    }

    /**
     * Returns length read, or `-1` if does not fit in buffer
     */
    private fun readFromBuffer(length: Int, buffer: ByteArray, offset: Int): Int {
        val bufStart = bufferedOffsetStart
        val bufEnd = bufferedOffsetEndExcl
        val pos = this.position

        if (bufStart != -1L) {
            check(bufEnd != -1L)
            // 有 buffer

            if (pos in bufStart..<bufEnd) {
                // 在 buffer 范围内
                val sizeToRead = min(length, (bufEnd - pos).coerceToInt())
                    .coerceAtMost((fileLength - pos).coerceToInt())

                val offsetInBuf = pos - bufStart
                this.buf.copyInto(
                    buffer,
                    destinationOffset = offset,
                    startIndex = offsetInBuf.checkToInt(),
                    endIndex = (offsetInBuf + sizeToRead).checkToInt(),
                )
                this.position += sizeToRead
                return sizeToRead
            } else {
                // 已经超出范围
            }
        } else {
            // 无 buffer, 先填充 buffer
        }
        return -1
    }

    @Volatile
    protected var closed: Boolean = false
    private fun checkClosed() {
        if (closed) throw IllegalStateException("This SeekableInput is closed")
    }

    override fun close() {
        if (closed) return
        synchronized(this) {
            if (closed) return
            closed = true
        }
    }

    override fun toString(): String {
        return "BufferedInput(position=$position, bytesRemaining=$bytesRemaining)"
    }
}
