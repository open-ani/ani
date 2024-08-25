package me.him188.ani.app.tools

import me.him188.ani.app.tools.Progress.Companion.Unspecified
import kotlin.jvm.JvmInline

/**
 * 表示一个范围为 `0f..1f`, 且可能为未知 [Unspecified] 的进度数值.
 *
 * @see Float.toProgress
 */
@JvmInline
value class Progress private constructor(
    private val rawValue: Float,
) {
    init {
        require(rawValue in 0f..1f || rawValue == -1f) { "Progress must be in range 0f..1f, but was $rawValue" }
    }

    val isUnspecified: Boolean get() = rawValue == -1f

    /**
     * [Unspecified] 视为未完成
     */
    val isFinished get() = rawValue == 1f

    /**
     * 会 box, 建议使用其他函数
     */
    fun getOrNull(): Float? = if (isUnspecified) null else rawValue

    fun getOrDefault(otherwise: Float): Float = if (isUnspecified) otherwise else rawValue

    companion object {
        /**
         * 表示未知
         */
        val Unspecified = Progress(-1f)
        val Zero = Progress(0f)

        fun fromZeroToOne(value: Float): Progress {
            return Progress(value.coerceIn(0f, 1f))
        }
    }
}

fun Progress.getOrMinusOne(): Float = getOrDefault(-1f)
fun Progress.getOrZero(): Float = getOrDefault(0f)

/**
 * 如果是 [Unspecified], 则返回 `0f`.
 * 返回范围 `0f..100f` 的数值.
 */
fun Progress.toPercentageOrZero(): Float = getOrZero() * 100f

/**
 * 将范围为 `0f..1f` 的数值转换为 [Progress].
 * @see Unspecified
 */
fun Float.toProgress(): Progress = Progress.fromZeroToOne(this)
fun Float?.toProgress(): Progress = this?.toProgress() ?: Unspecified
