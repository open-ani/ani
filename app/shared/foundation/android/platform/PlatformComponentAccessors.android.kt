package me.him188.ani.app.platform

import android.media.AudioManager as SystemAudioManager

actual fun getComponentAccessorsImpl(context: Context): PlatformComponentAccessors =
    AndroidPlatformComponentAccessors(context)

private class AndroidPlatformComponentAccessors(
    private val context: Context
) : PlatformComponentAccessors {
    override val audioManager: AudioManager by lazy {
        AndroidAudioManager(
            context.getSystemService(Context.AUDIO_SERVICE) as SystemAudioManager
        )
    }
}

private class AndroidAudioManager(
    private val delegate: SystemAudioManager,
) : AudioManager {
    private val StreamType.android: Int
        get() {
            return when (this) {
                StreamType.MUSIC -> SystemAudioManager.STREAM_MUSIC
            }
        }

    override fun getVolume(streamType: StreamType): Float {
        return delegate.getStreamVolume(streamType.android).toFloat() / delegate.getStreamMaxVolume(streamType.android)
    }

    override fun setVolume(streamType: StreamType, levelPercentage: Float) {
        val max = delegate.getStreamMaxVolume(streamType.android)
        return delegate.setStreamVolume(
            streamType.android,
            (levelPercentage * max).toInt()
                .coerceIn(minimumValue = 0, maximumValue = max),
            SystemAudioManager.FLAG_SHOW_UI
        )
    }
}