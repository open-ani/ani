package me.him188.ani.app.platform

interface BrightnessManager {
    /**
     * @return 0..1
     */
    fun getBrightness(): Float

    fun setBrightness(level: Float)
}