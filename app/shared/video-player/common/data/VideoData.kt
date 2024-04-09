package me.him188.ani.app.videoplayer.data

import me.him188.ani.utils.io.SeekableInput

/**
 * Holds information about a video file.
 */
interface VideoData : AutoCloseable {
    /**
     * Returns the length of the video file in bytes.
     */
    val fileLength: Long

    /**
     * Optional hash of the video file. `null` if not available.
     */
    val hash: String?

    /**
     * Opens a new input stream to the video file.
     * The returned [SeekableInput] needs to be closed when not used anymore.
     *
     * The returned [SeekableInput] must be closed before a new [createInput] can be made.
     * Otherwise, it is undefined behavior.
     */
    suspend fun createInput(): SeekableInput
}