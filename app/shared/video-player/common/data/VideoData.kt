package me.him188.ani.app.videoplayer.data

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.utils.io.SeekableInput
import java.io.IOException

/**
 * Holds information about a video file.
 */
@Stable
interface VideoData : AutoCloseable {
    val filename: String

    /**
     * Returns the length of the video file in bytes.
     */
    val fileLength: Long

    /**
     * The download speed in bytes per second.
     *
     * If this video data is not being downloaded, i.e. it is a local file,
     * the flow emits [FileSize.Unspecified].
     */
    val downloadSpeed: Flow<FileSize>

    /**
     * The upload speed in bytes per second.
     *
     * If this video data is not being uploaded, i.e. it is a local file,
     * the flow emits [FileSize.Unspecified].
     */
    val uploadRate: Flow<FileSize>

    /**
     * Optional hash of the video file. `null` if not available.
     */
    @Throws(IOException::class)
    fun computeHash(): String?

    /**
     * Opens a new input stream to the video file.
     * The returned [SeekableInput] needs to be closed when not used anymore.
     *
     * The returned [SeekableInput] must be closed before a new [createInput] can be made.
     * Otherwise, it is undefined behavior.
     */
    fun createInput(): SeekableInput

    override fun close()
}