package me.him188.ani.utils.platform.annotations

/**
 * Jetbrains Annotations range
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.TYPE)
expect annotation class Range(
    /**
     * @return minimal allowed value (inclusive)
     */
    val from: Long,
    /**
     * @return maximal allowed value (inclusive)
     */
    val to: Long
)
