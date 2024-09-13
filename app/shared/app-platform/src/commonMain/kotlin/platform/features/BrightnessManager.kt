package me.him188.ani.app.platform.features

interface BrightnessManager {
    /**
     * @return 0..1
     */
    fun getBrightness(): Float

    fun setBrightness(level: Float)
}