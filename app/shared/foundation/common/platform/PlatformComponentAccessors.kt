package me.him188.ani.app.platform

interface PlatformComponentAccessors {
    val audioManager: AudioManager?
}

fun getComponentAccessors(
    context: Context
): PlatformComponentAccessors = getComponentAccessorsImpl(context)

expect fun getComponentAccessorsImpl(
    context: Context
): PlatformComponentAccessors