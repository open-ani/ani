package me.him188.ani.app.videoplayer.torrent

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.him188.ani.app.videoplayer.data.VideoData
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.utils.io.SeekableInput
import me.him188.ani.utils.io.toSeekableInput
import java.io.File
import java.io.IOException

class FileVideoData(
    private val file: File,
) : VideoData {
    override val filename: String
        get() = file.name
    override val fileLength: Long by lazy { file.length() }

    private var hashCache: String? = null

    @Throws(IOException::class)
    override fun computeHash(): String {
        var hash = hashCache
        if (hash == null) {
            hash = md5Hash(file)
            hashCache = hash
        }
        return hash
    }

    override val downloadSpeed: StateFlow<FileSize> = MutableStateFlow(FileSize.Unspecified)
    override val uploadRate: Flow<FileSize> = MutableStateFlow(FileSize.Unspecified)

    @Throws(IOException::class)
    override fun createInput(): SeekableInput = file.toSeekableInput()
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
        val buffer = ByteArray(81920)
        var read = it.read(buffer)
        while (read > 0) {
            digest.update(buffer, 0, read)
            read = it.read(buffer)
        }

        digest.digest().toHexString()
    }
}