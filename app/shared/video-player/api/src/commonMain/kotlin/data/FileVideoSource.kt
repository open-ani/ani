package me.him188.ani.app.videoplayer.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.io.IOException
import me.him188.ani.datasources.api.MediaExtraFiles
import me.him188.ani.utils.coroutines.runInterruptible
import me.him188.ani.utils.io.DigestAlgorithm
import me.him188.ani.utils.io.SeekableInput
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.io.absolutePath
import me.him188.ani.utils.io.bufferedSource
import me.him188.ani.utils.io.exists
import me.him188.ani.utils.io.length
import me.him188.ani.utils.io.name
import me.him188.ani.utils.io.readAndDigest
import me.him188.ani.utils.io.toSeekableInput

class FileVideoData(
    val file: SystemPath,
) : VideoData {
    override val filename: String
        get() = file.name
    override val fileLength: Long by lazy { file.length() }

    private var hashCache: String? = null

    @OptIn(ExperimentalStdlibApi::class)
    @Throws(IOException::class)
    override fun computeHash(): String {
        var hash = hashCache
        if (hash == null) {
            hash = file.bufferedSource().use { it.readAndDigest(DigestAlgorithm.MD5).toHexString() }
            hashCache = hash
        }
        return hash
    }

    override val networkStats: Flow<VideoData.Stats> = MutableStateFlow(VideoData.Stats.Unspecified)

    override suspend fun createInput(): SeekableInput = runInterruptible { file.toSeekableInput() }
    override suspend fun close() {
        // no-op
    }
}

class FileVideoSource(
    private val file: SystemPath,
    override val extraFiles: MediaExtraFiles,
) : VideoSource<FileVideoData> {
    init {
        require(file.exists()) { "File does not exist: $file" }
    }

    override val uri: String
        get() = "file://${file.absolutePath}"

    override suspend fun open(): FileVideoData = FileVideoData(file)

    override fun toString(): String = "FileVideoSource(uri=$uri)"
}
