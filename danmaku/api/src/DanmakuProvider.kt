package me.him188.ani.danmaku.api

import kotlin.time.Duration

/**
 * A [DanmakuProvider] provides a stream of danmaku for a specific episode.
 */
interface DanmakuProvider {
    /**
     * Matches a danmaku stream by the given filtering parameters.
     *
     * Returns `null` if not found.
     *
     * The returned [DanmakuSession] should be closed when it is no longer needed.
     */
    suspend fun startSession(
        filename: String,
        fileHash: String?,
        fileSize: Long,
        videoDuration: Duration,
    ): DanmakuSession?
}

