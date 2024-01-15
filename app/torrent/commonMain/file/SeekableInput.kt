package me.him188.ani.app.torrent.file

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.RandomAccessFile

public interface SeekableInput {
    public suspend fun seek(offset: Long)
    public suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int
}

public fun RandomAccessFile.asSeekableInput(): SeekableInput = object : SeekableInput {
    override suspend fun seek(offset: Long) {
        withContext(Dispatchers.IO) { this@asSeekableInput.seek(offset) }
    }

    override suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        return withContext(Dispatchers.IO) { this@asSeekableInput.read(buffer, offset, length) }
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