@file:OptIn(UnstableApi::class)

package me.him188.ani.app.videoplayer.media

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.him188.ani.app.videoplayer.TorrentVideoSource
import me.him188.ani.app.videoplayer.VideoSource

object DataSourceAdapter {
    suspend fun mediaSourceFactory(video: VideoSource<*>): Flow<ProgressiveMediaSource.Factory> {
        return when (video) {
            is TorrentVideoSource -> video.startStreaming().map {
                ProgressiveMediaSource.Factory { TorrentDataSource(it) }
            }

            else -> error("Unsupported video type: ${video::class}")
        }
    }
}