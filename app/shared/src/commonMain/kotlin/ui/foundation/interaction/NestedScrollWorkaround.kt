package me.him188.ani.app.ui.foundation.interaction

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.platform.LocalDensity
import me.him188.ani.app.ui.foundation.effects.onPointerEventMultiplatform

// https://github.com/JetBrains/compose-multiplatform/issues/4975
fun Modifier.nestedScrollWorkaround(
    lazyListState: LazyListState,
    nestedScrollConnection: NestedScrollConnection
): Modifier {
    return composed {
        val density = LocalDensity.current
        onPointerEventMultiplatform(PointerEventType.Scroll, pass = PointerEventPass.Final) {
            val event = it.changes.getOrNull(0) ?: return@onPointerEventMultiplatform
            val scrollDelta = event.scrollDelta
            if (scrollDelta != Offset.Unspecified && scrollDelta != Offset.Zero) {
                if (!lazyListState.canScrollBackward) {
                    nestedScrollConnection.onPostScroll(
                        Offset.Zero,
                        -scrollDelta * 2f * density.density,
                        NestedScrollSource.Wheel,
                    )
                }
            }
        }
    }
}