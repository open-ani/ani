@file:Suppress("NOTHING_TO_INLINE")

package me.him188.ani.app.data.subject

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Immutable
@JvmInline
@Serializable
value class PackedDate @PublishedApi internal constructor(
    @JvmField
    @PublishedApi
    internal val packed: Int
) {
    inline val isValid: Boolean get() = packed != Int.MAX_VALUE

    inline val year: Int get() = if (isValid) DatePacker.unpack1(packed) else 0
    inline val rawMonth: Int get() = if (isValid) DatePacker.unpack2(packed) else 0
    inline val day: Int get() = if (isValid) DatePacker.unpack3(packed) else 0

    @Stable
    val coercedMonth: Int
        get() = when (rawMonth) {
            12, in 1..2 -> 1
            in 3..5 -> 4
            in 6..8 -> 7
            in 9..11 -> 10
            else -> 0
        }

    companion object {
        @JvmStatic
        val Invalid = PackedDate(Int.MAX_VALUE)


        /**
         * @param date `2024-05-18`
         */
        fun parseFromDate(date: String): PackedDate {
            val split = date.split("-")
            if (split.size != 3) return Invalid
            return PackedDate(
                split[0].toIntOrNull() ?: return Invalid,
                split[1].toIntOrNull() ?: return Invalid,
                split[2].toIntOrNull() ?: return Invalid
            )
        }
    }
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
