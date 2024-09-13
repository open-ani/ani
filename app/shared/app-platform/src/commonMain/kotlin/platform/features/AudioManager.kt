package me.him188.ani.app.platform.features

import me.him188.ani.app.platform.Context

interface AudioManager {
    /**
     * @return 0..1
     */
    fun getVolume(streamType: StreamType): Float

    fun setVolume(streamType: StreamType, levelPercentage: Float)
}

enum class StreamType {
    MUSIC,
}

fun Context.getComponentAccessors() = getComponentAccessorsImpl(this)

internal expect fun getComponentAccessorsImpl(context: Context): PlatformComponentAccessors
