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
 * By closing the returned [SeekableInput], you also close this [RandomAccessFile].
 * Conversely, by closing this [RandomAccessFile], you also close the returned [SeekableInput],
 * though it is not recommended to close the [RandomAccessFile] directly.
 *
 * The file is not open until first read.
 */
public fun File.asSeekableInput(): SeekableInput = BufferedFileInput(this)

internal class BufferedFileInput(
    private val file: File,
) : SeekableInput {
    private var _reader: BufferedInputStream? = null
    internal var streamCounter: Int = 0

    @kotlin.jvm.Throws(IOException::class)
    private fun createInputStream(): BufferedInputStream {
        ++streamCounter
        return BufferedInputStream(FileInputStream(file), DEFAULT_BUFFER_SIZE * 16)
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

    private fun getReaderOrCreate() = _reader ?: createInputStream().also {
        _reader = it
    }

    override fun seek(offset: Long) {
        require(offset >= 0) { "offset must be non-negative, but was $offset" }
        checkClosed()
        val lastOffset = this.offset
        if (offset == lastOffset) {
            return
        }
        if (offset < lastOffset) {
            // Seeking back
            _reader?.close()
            _reader = createInputStream().also {
                it.skip(offset)
            }
        } else {
            // Seeking forward
            getReaderOrCreate().skip(offset - lastOffset)
        }
        self.offset = offset
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        require(offset >= 0) { "offset must be non-negative, but was $offset" }
        require(length >= 0) { "length must be non-negative, but was $length" }
        require(offset + length <= buffer.size) { "offset + length must be less than or equal to buffer size, but was ${offset + length} > ${buffer.size}" }
        checkClosed()
        val reader = getReaderOrCreate()
        return reader.read(buffer, offset, length).also {
            self.offset += it
        }
    }

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
