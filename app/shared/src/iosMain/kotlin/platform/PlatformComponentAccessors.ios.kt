package me.him188.ani.app.platform

actual fun getComponentAccessorsImpl(context: Context): PlatformComponentAccessors {
    return IosPlatformComponentAccessors
}

private object IosPlatformComponentAccessors : PlatformComponentAccessors {
    override val audioManager: AudioManager?
        get() = null
}
