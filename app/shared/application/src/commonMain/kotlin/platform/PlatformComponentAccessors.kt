package me.him188.ani.app.platform

import me.him188.ani.app.videoplayer.freatures.AudioManager
import me.him188.ani.app.videoplayer.freatures.BrightnessManager

interface PlatformComponentAccessors {
    val audioManager: AudioManager?
    val brightnessManager: BrightnessManager? get() = null
}

fun getComponentAccessors(
    context: Context
): PlatformComponentAccessors = getComponentAccessorsImpl(context)

expect fun getComponentAccessorsImpl(
    context: Context
): PlatformComponentAccessors