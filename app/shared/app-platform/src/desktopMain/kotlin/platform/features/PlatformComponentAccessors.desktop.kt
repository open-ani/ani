package me.him188.ani.app.platform.features

import me.him188.ani.app.platform.Context

actual fun getComponentAccessorsImpl(context: Context): PlatformComponentAccessors = DesktopPlatformComponentAccessors()

private class DesktopPlatformComponentAccessors : PlatformComponentAccessors {
    override val audioManager: AudioManager?
        get() = null
}
