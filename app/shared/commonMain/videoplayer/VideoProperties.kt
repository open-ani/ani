package me.him188.ani.app.videoplayer

import kotlin.time.Duration

data class VideoProperties internal constructor(
    val title: String?,
    val heightPx: Int,
    val widthPx: Int,
    val videoBitrate: Int,
    val audioBitrate: Int,
    val frameRate: Float,
    val duration: Duration,
)