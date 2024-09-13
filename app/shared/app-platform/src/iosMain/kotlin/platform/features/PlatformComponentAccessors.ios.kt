package me.him188.ani.app.platform.features

import me.him188.ani.app.platform.Context

actual fun getComponentAccessorsImpl(context: Context): PlatformComponentAccessors {
    return IosPlatformComponentAccessors
}

private object IosPlatformComponentAccessors : PlatformComponentAccessors {
    override val audioManager: AudioManager?
        get() = null
}
