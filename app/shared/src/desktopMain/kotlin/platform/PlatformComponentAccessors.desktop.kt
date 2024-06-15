package me.him188.ani.app.platform

actual fun getComponentAccessorsImpl(context: Context): PlatformComponentAccessors = DesktopPlatformComponentAccessors()

private class DesktopPlatformComponentAccessors : PlatformComponentAccessors {
    override val audioManager: AudioManager?
        get() = null
}