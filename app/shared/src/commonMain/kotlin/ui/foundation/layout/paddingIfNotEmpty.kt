@file:Suppress("NOTHING_TO_INLINE")

package me.him188.ani.app.ui.foundation.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


/**
 * 如果所应用到的 composable 的高度和宽度都大于 `0`, 则给它添加 padding, 否则不添加.
 */
fun Modifier.paddingIfNotEmpty(
    paddingValues: PaddingValues,
): Modifier = layout { measurable, constraints ->
    val start = paddingValues.calculateStartPadding(layoutDirection)
    val top = paddingValues.calculateTopPadding()
    val end = paddingValues.calculateEndPadding(layoutDirection)
    val bottom = paddingValues.calculateBottomPadding()
    val horizontalReservedPx = (start + end).roundToPx()
    val verticalReservedPx = (top + bottom).roundToPx()
    constraints.copy(
        minWidth = (constraints.minWidth - horizontalReservedPx).coerceAtLeast(0),
        maxWidth = (constraints.maxWidth - horizontalReservedPx).coerceAtLeast(0),
        minHeight = (constraints.minHeight - verticalReservedPx).coerceAtLeast(0),
        maxHeight = (constraints.maxHeight - verticalReservedPx).coerceAtLeast(0),
    )
    val placeable = measurable.measure(constraints)
    if (placeable.height > 0 && placeable.width > 0) {
        layout(placeable.width + horizontalReservedPx, placeable.height + verticalReservedPx) {
            placeable.placeRelative(start.roundToPx(), top.roundToPx())
        }
    } else {
        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}

inline fun Modifier.paddingIfNotEmpty(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp
): Modifier = paddingIfNotEmpty(PaddingValues(start, top, end, bottom))

inline fun Modifier.paddingIfNotEmpty(
    all: Dp
): Modifier = paddingIfNotEmpty(PaddingValues(all))

inline fun Modifier.paddingIfNotEmpty(
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp
): Modifier = paddingIfNotEmpty(PaddingValues(horizontal, vertical))
