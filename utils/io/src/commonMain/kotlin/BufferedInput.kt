package me.him188.ani.utils.io

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import me.him188.ani.utils.platform.annotations.TestOnly
import kotlin.concurrent.Volatile
import kotlin.math.min

/**
 * 实现双向缓冲和复用算法
 */
abstract class BufferedInput(
    bufferSize: Int,
) : SeekableInput {
    companion object {
        const val DEFAULT_BUFFER_PER_DIRECTION: Int = 8192 * 16

        protected fun Long.coerceToInt(): Int {
            if (this > Int.MAX_VALUE) return Int.MAX_VALUE
            return this.toInt()
        }

        protected fun Long.checkToInt(): Int {
            if (this > Int.MAX_VALUE) error("value is too large to fit in Int: $this")
            return this.toInt()
        }
    }

    /**
     * @see buf 包含的数据的起始位置
     */
    protected var bufferedOffsetStart: Long = -1L

    /**
     * @see buf 包含的数据的结束位置 (exclusive)
     */
    protected var bufferedOffsetEndExcl: Long = 0L

    /**
     * view offsets
     */
    @TestOnly
    val bufferedOffsetRange: LongRange get() = bufferedOffsetStart..<bufferedOffsetEndExcl

    /**
     * 双向缓冲区, 读取一次后, seek 回去也能很快
     */
    protected val buf: ByteArray = ByteArray(bufferSize * 2)

    /**
     * 当前 user 读到的位置
     */
    final override var position: Long = 0
        protected set(value) {
            require(value >= 0) { "position must be non-negative, but was $value" }
            field = value
        }

    final override val bytesRemaining: Long get() = (this.size - position).coerceAtLeast(0)

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

    /**
     * @param readEnd exclusive
     */
    protected fun fillBufferRange(readStart: Long, readEnd: Long) {
        /**
         * 本次读 buffer 需要的长度
         */
        val readLength = (readEnd - readStart).coerceToInt()

        // 尽量先从旧的 buffer 里 "移动"
        val bufferedStart = bufferedOffsetStart
        val bufferedEnd = bufferedOffsetEndExcl
        if (bufferedStart != -1L) {
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
                readFileToBufferChecked(
                    fileOffset = bufferedEnd,
                    bufferOffset = (bufferedEnd - readStart).checkToInt(),
                    length = (readEnd - bufferedEnd).coerceToInt(),
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
                readFileToBufferChecked(
                    fileOffset = readStart,
                    bufferOffset = 0,
                    length = (bufferedStart - readStart).coerceToInt(),
                )
            } else {
                // 旧 buffer 不能用
                readFileToBufferChecked(readStart, 0, readLength)
            }
        } else {
            readFileToBufferChecked(readStart, 0, readLength)
        }
        this.bufferedOffsetStart = readStart
        this.bufferedOffsetEndExcl = readEnd
    }

    protected abstract fun readFileToBuffer(fileOffset: Long, bufferOffset: Int, length: Int): Int

    private fun readFileToBufferChecked(fileOffset: Long, bufferOffset: Int, length: Int) {
        val read = readFileToBuffer(fileOffset, bufferOffset, length)
        check(read == length) { "readFileToBufferExact: Expected to read $length bytes, but read $read bytes, fileOffset=$fileOffset" }
    }

    override fun prepareBuffer() {
        checkClosed()
        fillBuffer()
    }

    final override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        require(offset >= 0) { "offset must be non-negative, but was $offset" }
        require(length >= 0) { "length must be non-negative, but was $length" }
        require(offset + length <= buffer.size) { "offset + length must be less than or equal to buffer size, but was ${offset + length} > ${buffer.size}" }
        checkClosed()

        if (this.position >= this.size) return -1
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
                    .coerceAtMost((this.size - pos).coerceToInt())

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

    private val lock = SynchronizedObject()

    override fun close() {
        if (closed) return
        synchronized(lock) {
            if (closed) return
            closed = true
        }
    }

    override fun toString(): String {
        return "BufferedInput(position=$position, bytesRemaining=$bytesRemaining)"
    }
}
