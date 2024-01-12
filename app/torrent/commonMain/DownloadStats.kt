package me.him188.ani.app.torrent

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

public interface DownloadStats {
    public val totalBytes: Flow<Long>
    public val downloadedBytes: Flow<Long>
    public val downloadRate: Flow<Long> // bytes per second

    /**
     * Range: `0..1`
     */
    public val progress: Flow<Float>

    public val isFinished: Flow<Boolean>

    /**
     * @throws CancellationException if coroutine scope is cancelled
     */
    public suspend fun awaitFinished()
}