package me.him188.ani.utils.io

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
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
public fun File.asSeekableInput(): SeekableInput = BufferedFileInput(this)

internal class BufferedFileInput(
    private val file: File,
) : SeekableInput {
    private val fileLength = file.length()
    private var _reader: BufferedInputStream? = null
    internal var streamCounter: Int = 0

    @kotlin.jvm.Throws(IOException::class)
    private fun createInputStream(maxBuffer: Long): BufferedInputStream {
        ++streamCounter
        return BufferedInputStream(  // TODO: 实际上自己写一个 BufferedInputStream, 在填充 buffer 时检查 piece 是否下载完成
            FileInputStream(file),
            (DEFAULT_BUFFER_SIZE * 16).coerceAtMost(maxBuffer.coerceToInt())
        )
    }

    private fun Long.coerceToInt(): Int {
        if (this > Int.MAX_VALUE) return Int.MAX_VALUE
        return this.toInt()
    }

    override var offset: Long = 0
    override val bytesRemaining: Long
        get() {
            val reader = _reader
            @Suppress("IfThenToElvis") // don't box ints
            return if (reader != null) {
                reader.available().toUInt().toLong()
            } else {
                file.length() - offset // this is much slower than reader.available()
            }
        }

    private inline val self get() = this

    private fun getReaderOrCreate(maxBuffer: Long) = _reader ?: createInputStream(maxBuffer).also {
        _reader = it
    }

    override fun seek(offset: Long, maxBuffer: Long) {
        require(offset >= 0) { "offset must be non-negative, but was $offset" }
        require(offset <= fileLength) { "offset must be less than or equal to file length, but was $offset > $fileLength" }
        require(maxBuffer >= 1) { "maxBuffer must be >= 1, but was $maxBuffer" }

        checkClosed()
        val lastOffset = this.offset
        if (offset == lastOffset) {
            return
        }
        if (offset < lastOffset) {
            // Seeking back
            _reader?.close()
            _reader = createInputStream(maxBuffer).also {
                it.skip(offset)
            }
        } else {
            // Seeking forward
            getReaderOrCreate(maxBuffer).skip(offset - lastOffset)
        }
        self.offset = offset
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        require(offset >= 0) { "offset must be non-negative, but was $offset" }
        require(length >= 0) { "length must be non-negative, but was $length" }
        require(offset + length <= buffer.size) { "offset + length must be less than or equal to buffer size, but was ${offset + length} > ${buffer.size}" }
        checkClosed()
        val reader = getReaderOrCreate(Long.MAX_VALUE)
        return reader.read(buffer, offset, length).also {
            self.offset += it
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
        _reader?.close()
    }

    override fun toString(): String {
        return "RandomAccessFileAsSeekableInput(${file}, offset=$offset)"
    }
}
