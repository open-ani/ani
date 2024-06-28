@file:Suppress("NOTHING_TO_INLINE")

package me.him188.ani.app.data.subject

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import me.him188.ani.app.data.subject.PackedDate.Companion.Invalid
import java.util.Calendar
import java.util.TimeZone
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * 一个日期, 支持年月日. 支持表示无效状态 [Invalid].
 */
@Immutable
@JvmInline
@Serializable
value class PackedDate @PublishedApi internal constructor(
    /**
     * 高 16 位为年, 中 8 位为月, 低 8 位为日
     */
    @JvmField
    @PublishedApi
    internal val packed: Int
) : Comparable<PackedDate> {
    inline val isValid: Boolean get() = packed != Int.MAX_VALUE
    inline val isInvalid: Boolean get() = packed == Int.MAX_VALUE

    /**
     * 获取年份, `0..9999`. 当无效 [Invalid] 时返回 0.
     */
    inline val year: Int get() = if (isValid) DatePacker.unpack1(packed) else 0

    /**
     * 获取原始月份信息, `1..12`. 当无效 [Invalid] 时返回 0.
     */
    inline val month: Int get() = if (isValid) DatePacker.unpack2(packed) else 0

    /**
     * 获取日期, `0..31`. 当无效 [Invalid] 时返回 0.
     */
    inline val day: Int get() = if (isValid) DatePacker.unpack3(packed) else 0

    companion object {
        /**
         * 表示一个无效时间.
         */
        @JvmStatic
        val Invalid = PackedDate(Int.MAX_VALUE)


        /**
         * @param date `2024-05-18`. 允许的日期范围为 `0000-01-01` 到 `9999-12-31`. 仅检查时间格式, 不检查时间合法性.
         * 因此 2 月 31 日也被视为是正确的.
         * @return 当 [date] 格式不正确时返回 [Invalid]
         */
        fun parseFromDate(date: String): PackedDate {
            val split = date.split("-")
            if (split.size != 3) return Invalid
            return PackedDate(
                split[0].toIntOrNull() ?: return Invalid,
                split[1].toIntOrNull() ?: return Invalid,
                split[2].toIntOrNull() ?: return Invalid,
            )
        }

        fun now(): PackedDate {
            val currentTimeMillis = System.currentTimeMillis()
            val timeZone = TimeZone.getTimeZone("UTC+8") // bangumi 是固定 UTC+8
            val calendar = Calendar.getInstance(timeZone).apply { timeInMillis = currentTimeMillis }

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is zero-based
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            return PackedDate(year, month, day)
        }
    }

    override fun compareTo(other: PackedDate): Int = packed.compareTo(other.packed) // trivial!

    override fun toString(): String {
        return if (isInvalid) "Invalid" else "$year-$month-$day"
    }
}

/**
 * 获取月份所在季度的第一个月, `1, 4, 7, 10`.
 */
@Stable
inline val PackedDate.seasonMonth: Int
    get() = when (month) {
        12, in 1..2 -> 1
        in 3..5 -> 4
        in 6..8 -> 7
        in 9..11 -> 10
        else -> 0
    }

/**
 * 计算两个日期的间隔. 当任一日期无效时返回 [Duration.INFINITE].
 */
operator fun PackedDate.minus(other: PackedDate): Duration {
    if (this.isInvalid || other.isInvalid) return Duration.INFINITE
    val thisCalendar = Calendar.getInstance().apply {
        clear()
        set(year, month - 1, day)
    }
    val otherCalendar = Calendar.getInstance().apply {
        clear()
        set(other.year, other.month - 1, other.day)
    }
    return (thisCalendar.timeInMillis - otherCalendar.timeInMillis).milliseconds
}

@Stable
inline fun PackedDate(
    year: Int,
    month: Int,
    day: Int,
): PackedDate = if (year in 0..9999 && month in 1..12 && day in 1..31) {
    PackedDate(DatePacker.pack(year, month, day))
} else {
    PackedDate.Invalid // invalid
}

@Suppress("NOTHING_TO_INLINE")
@PublishedApi
internal object DatePacker {
    inline fun pack(
        val1: Int, // short
        val2: Int, // byte
        val3: Int, // byte
    ): Int {
        return val1.shl(16) or val2.shl(8) or val3
    }

    inline fun unpack1(value: Int): Int = value.shr(16).and(0xFFFF)
    inline fun unpack2(value: Int): Int = value.shr(8).and(0xFF)
    inline fun unpack3(value: Int): Int = value.and(0xFF)
}
