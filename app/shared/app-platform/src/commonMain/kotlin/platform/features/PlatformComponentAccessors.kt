package me.him188.ani.app.platform.features

interface PlatformComponentAccessors {
    val audioManager: AudioManager?
    val brightnessManager: BrightnessManager? get() = null
}
