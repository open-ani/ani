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
    private val bufferSize: Int = DEFAULT_BUFFER_PER_DIRECTION,
) : BufferedInput(bufferSize) {
    override val fileLength: Long get() = file.length()

    override fun fillBuffer() {
        val fileLength = this.fileLength
        val pos = this.position

        val readStart = (pos - bufferSize).coerceAtLeast(0)
        val readEnd = (pos + bufferSize).coerceAtMost(fileLength)

        fillBufferRange(readStart, readEnd)
    }

    override fun readFileToBuffer(fileOffset: Long, bufferOffset: Int, length: Int): Int {
        val file = this.file
        file.seek(fileOffset)
        return file.read(buf, bufferOffset, length)
    }

    override fun toString(): String {
        return "BufferedFileInput(file=$file, position=$position, bytesRemaining=$bytesRemaining)"
    }
}

public abstract class BufferedInput(
    bufferSize: Int,
) : SeekableInput {
    protected companion object {
        public const val DEFAULT_BUFFER_PER_DIRECTION: Int = 8192

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
    protected var bufferedOffsetEndExcl: Long = 0L

    @get:TestOnly
    public val bufferedOffsetRange: LongRange get() = bufferedOffsetStart..<bufferedOffsetEndExcl

    /**
     * 双向缓冲区, 读取一次后, seek 回去也能很快
     */
    protected var buf: ByteArray = ByteArray(bufferSize * 2)

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

    protected fun fillBufferRange(readStart: Long, readEnd: Long) {
        /**
         * 本次读 buffer 需要的长度
         */
        val readLength = (readEnd - readStart).coerceToInt()

        // 尽量先从旧的 buffer 里 "移动"
        val bufferedStart = bufferedOffsetStart
        val bufferedEnd = bufferedOffsetEndExcl
        val read = if (bufferedStart != -1L) {
            // 我们只考虑用旧 buffer 来填充新 buffer 的前面/后面一部分, 来少读取一些文件字节, 不考虑中间包含的情况. 
            // 如果用旧的 buffer 填充新 buffer 的中间一部分, 那么还需要两次文件 IO 才能完成新 buffer, 这可能比一次文件 IO 更慢.
            if (readStart in bufferedStart..<bufferedEnd) {
                // 旧 buffer 的一部分可以用作前面
                /*
                 *                        can be reused
                 *                      |---------------|
                 * File: |--------------------------------------------------------|
                 *          ^           ^               ^              ^
                     bufferedStart   readStart     bufferedEnd        readEnd
                 * 
                 */
                buf.copyInto(
                    buf,
                    destinationOffset = 0,// relative to readStart
                    startIndex = (readStart - bufferedStart).checkToInt(),
                    endIndex = (bufferedEnd - bufferedStart).checkToInt(),
                )
                // 用文件填充 buffer 的后面
                readFileToBuffer(
                    fileOffset = bufferedEnd,
                    bufferOffset = (bufferedEnd - readStart).checkToInt(),
                    length = (readEnd - bufferedEnd).coerceToInt()
                )
            } else if (readEnd in (bufferedStart + 1)..bufferedEnd) {
                // 旧 buffer 的一部分可以用作后面
                /*
                 *                                        can be reused
                 *                                      |--------------|
                 * File: |--------------------------------------------------------------------|
                 *                      ^               ^              ^            ^
                 *                  readStart     bufferedStart     readEnd     bufferedEnd
                 *  
                 */
                buf.copyInto(
                    buf,
                    destinationOffset = (bufferedStart - readStart).checkToInt(), // relative to readStart
                    startIndex = 0,
                    endIndex = (readEnd - bufferedStart).checkToInt(),
                )
                // 用文件填充 buffer 的前面
                readFileToBuffer(
                    fileOffset = readStart,
                    bufferOffset = 0,
                    length = (bufferedStart - readStart).coerceToInt()
                )
            } else {
                // 旧 buffer 不能用
                readFileToBuffer(readStart, 0, readLength)
            }
        } else {
            readFileToBuffer(readStart, 0, readLength)
        }
        this.bufferedOffsetStart = readStart
        this.bufferedOffsetEndExcl = readEnd
    }

    protected abstract fun readFileToBuffer(fileOffset: Long, bufferOffset: Int, length: Int): Int

    override fun prepareBuffer() {
        checkClosed()
        fillBuffer()
    }

    final override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        require(offset >= 0) { "offset must be non-negative, but was $offset" }
        require(length >= 0) { "length must be non-negative, but was $length" }
        require(offset + length <= buffer.size) { "offset + length must be less than or equal to buffer size, but was ${offset + length} > ${buffer.size}" }
        checkClosed()

        if (this.position >= fileLength) return -1
        if (length == 0) return 0

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
