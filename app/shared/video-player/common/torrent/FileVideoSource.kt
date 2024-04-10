package me.him188.ani.app.videoplayer.torrent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.app.videoplayer.data.VideoData
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.utils.io.SeekableInput
import me.him188.ani.utils.io.asSeekableInput
import java.io.File
import java.io.RandomAccessFile

class FileVideoData(
    private val file: File,
) : VideoData {
    override val fileLength: Long by lazy { file.length() }
    override val hash: String by lazy { md5Hash(file) }

    override suspend fun createInput(): SeekableInput = withContext(Dispatchers.IO) {
        RandomAccessFile(file, "r").asSeekableInput()
    }

    override fun close() {
        // no-op
    }
}

class FileVideoSource(
    private val file: File,
) : VideoSource<FileVideoData> {
    init {
        require(file.exists()) { "File does not exist: $file" }
    }

    override val uri: String
        get() = "file://${file.absolutePath}"

    override suspend fun open(): FileVideoData = FileVideoData(file)

    override fun toString(): String = "FileVideoSource(uri=$uri)"
}

@OptIn(ExperimentalStdlibApi::class)
private fun md5Hash(file: File): String {
    return file.inputStream().use {
        val digest = java.security.MessageDigest.getInstance("MD5")
        val buffer = ByteArray(8192)
        var read = it.read(buffer)
        while (read > 0) {
            digest.update(buffer, 0, read)
            read = it.read(buffer)
        }

        digest.digest().toHexString()
    }
}