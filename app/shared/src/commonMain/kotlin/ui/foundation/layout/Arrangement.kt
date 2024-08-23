package me.him188.ani.app.ui.foundation.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.roundToInt

/**
 * 将子元素均匀分布在主轴上, 不考虑它们的大小.
 *
 * 例如一个 Row:
 *
 * ```kotlin
 * Row(
 *    horizontalArrangement = Arrangement.Evenly,
 * ) {
 *     Text("a")
 *     Text("b")
 * }
 * ```
 *
 * 将会变为:
 *
 * ```
 * ------------------------------
 * |a            |b              |
 * ------------------------------
 * ```
 */
@Stable
val Arrangement.Evenly: Arrangement.HorizontalOrVertical
    get() = instance

private val instance = object : Arrangement.HorizontalOrVertical {
    override fun Density.arrange(
        totalSize: Int,
        sizes: IntArray,
        layoutDirection: LayoutDirection,
        outPositions: IntArray
    ) {
        val size = totalSize.toFloat() / sizes.size
        var position = 0f
        for (i in sizes.indices) {
            outPositions[i] = position.roundToInt()
            position += size
        }
    }

    override fun Density.arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) {
        val size = totalSize.toFloat() / sizes.size
        var position = 0f
        for (i in sizes.indices) {
            outPositions[i] = position.roundToInt()
            position += size
        }
    }

}