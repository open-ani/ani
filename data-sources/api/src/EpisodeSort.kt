package me.him188.ani.datasources.api

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.EpisodeSort.Normal
import me.him188.ani.datasources.api.EpisodeSort.Special
import java.math.BigDecimal

/**
 * 剧集序号, 例如 "01", "24.5", "OVA".
 *
 * - [Normal] 代表普通正片剧集, 例如 "01", "24.5". 注意, 只有整数和 ".5" 的浮点数会被解析为 Normal 类型.
 * - [Special] 代表任何其他剧集, 统称为特殊剧集, 例如 "OVA", "SP".
 *
 *
 * - Sort: 在系列中的集数, 例如第二季的第一集为 26
 * - Ep: 在当前季度中的集数, 例如第二季的第一集为 01
 */
@Serializable
@Stable
sealed class EpisodeSort : Comparable<EpisodeSort> {
    abstract val number: Float?

    /**
     * "1", "1.5", "SP"
     * Not padded.
     *
     * @see toString
     */
    internal abstract val raw: String

    /**
     * An integer or a `.5` float.
     */
    @Serializable
    class Normal internal constructor(
        override val number: Float, // Luckily ".5" can be precisely represented in IEEE 754
    ) : EpisodeSort() {
        override val raw: String
            get() {
                if (number.toInt().toFloat() == number) return number.toInt().toString()
                return number.toString()
            }

        val isPartial: Boolean get() = number % 1f == 0.5f

        override fun toString(): String {
            if (number.toInt().toFloat() == number) {
                if (number < 10) {
                    return "0${number.toInt()}"
                }
                return number.toInt().toString()
            }
            return number.toString()
        }
    }

    @Serializable
    class Special internal constructor(
        override val raw: String,
    ) : EpisodeSort() {
        override val number: Float? get() = null
        override fun toString(): String = raw
    }

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other === null) return false
        if (other !is EpisodeSort) return false

        val otherFloat = other.number
        val thisFloat = number
        if (otherFloat != null && thisFloat != null) return otherFloat == thisFloat
        if (otherFloat != null || thisFloat != null) return false // one Normal one Special
        return other.raw == raw
    }

    final override fun hashCode(): Int {
        if (number != null) return number.hashCode()
        return raw.hashCode()
    }

    final override fun compareTo(other: EpisodeSort): Int {
        if (this is Normal) {
            if (other is Normal) return number.compareTo(other.number) // Normal and Normal
            return -1 // Normal < Special
        }
        if (other is Normal) return 1 // Special > Normal

        // Special and Special
        return raw.compareTo(other.raw)
    }

    companion object {
//        fun parseRange(range: String): List<EpisodeSort> {
//            
//        }
    }
}

fun EpisodeSort(raw: String): EpisodeSort {
    val float = raw.toFloatOrNull() ?: return Special(raw)
    if (float < 0) return Special(raw)
    return if (float.toInt().toFloat() == float || float % 0.5f == 0f) {
        Normal(float)
    } else {
        Special(raw)
    }
}

fun EpisodeSort(int: Int): EpisodeSort {
    if (int < 0) return Special(int.toString())
    return Normal(int.toFloat())
}

fun EpisodeSort(int: BigDecimal): EpisodeSort {
    if (int < BigDecimal.ZERO) return Special(int.toString())
    return EpisodeSort(int.toString())
}
