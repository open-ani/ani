package me.him188.ani.app.platform

import android.view.WindowManager
import android.media.AudioManager as SystemAudioManager

actual fun getComponentAccessorsImpl(context: Context): PlatformComponentAccessors =
    AndroidPlatformComponentAccessors(context)

private class AndroidPlatformComponentAccessors(
    context: Context
) : PlatformComponentAccessors {
    override val audioManager: AudioManager by lazy {
        AndroidAudioManager(
            context.getSystemService(Context.AUDIO_SERVICE) as SystemAudioManager,
        )
    }
    override val brightnessManager: BrightnessManager? by lazy {
        AndroidBrightnessManager(context)
    }
}

private class AndroidBrightnessManager(
    private val context: Context,
) : BrightnessManager {
    override fun getBrightness(): Float {
        val activity = context.findActivity() ?: return -1f
        val window = activity.window ?: return -1f
        val current = window.attributes.screenBrightness

        if (current == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
            // no override, use system settings
            return android.provider.Settings.System.getInt(
                context.contentResolver,
                android.provider.Settings.System.SCREEN_BRIGHTNESS,
            ).toFloat() / 255
        }

        return current

//        return android.provider.Settings.System.getInt(
//            context.contentResolver,
//            android.provider.Settings.System.SCREEN_BRIGHTNESS
//        ).toFloat() / 255
    }

    override fun setBrightness(level: Float) {
        val activity = context.findActivity() ?: return
        val window = activity.window ?: return
        window.attributes.screenBrightness = level
        window.attributes = window.attributes // update

        // system settings also requires permission
//        android.provider.Settings.System.putInt(
//            context.contentResolver,
//            android.provider.Settings.System.SCREEN_BRIGHTNESS,
//            (level * 255).toInt().coerceIn(minimumValue = 0, maximumValue = 255)
//        )
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
            0,
        )
    }
}