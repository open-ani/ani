package me.him188.ani.app.platform

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