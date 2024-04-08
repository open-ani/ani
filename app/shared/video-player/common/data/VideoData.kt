package me.him188.ani.app.videoplayer.data

/**
 * Represents a video data like an opened file or input stream.
 */
interface VideoData : AutoCloseable {
    /**
     * Returns the length of the video file in bytes.
     */
    val fileLength: Long

    val hash: String?
}