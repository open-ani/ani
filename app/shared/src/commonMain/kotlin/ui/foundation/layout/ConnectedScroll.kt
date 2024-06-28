package me.him188.ani.app.ui.foundation.layout

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.Velocity
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * 提供 [ConnectedScrollState.nestedScrollConnection], 将其添加到 [Modifier.nestedScroll] 中,
 * 即可让 [Modifier.connectedScrollContainer] 优先处理滚动事件.
 */
@Composable
fun rememberConnectedScrollState(
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
): ConnectedScrollState {
    return remember(flingBehavior) {
        ConnectedScrollState(flingBehavior)
    }
}

@Stable
class ConnectedScrollState(
    val flingBehavior: FlingBehavior,
) {
    /**
     * 仅在第一个 measurement pass 后更新
     */
    var scrollableHeight by mutableIntStateOf(0)
        internal set

    // 范围为 -scrollableHeight ~ 0
    var scrollableOffset by mutableFloatStateOf(0f)
        internal set

    val scrollScope = object : ScrollScope {
        override fun scrollBy(pixels: Float): Float {
            val diff = pixels.coerceAtMost(-scrollableOffset)
            scrollableOffset += diff
            return diff
        }
    }

    val isScrolledTop by derivedStateOf {
        if (scrollableHeight == 0) { // not yet measured
            return@derivedStateOf false
        }
        scrollableOffset.toInt() == -scrollableHeight
    }

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            // 手指往下, available.y > 0

            if (scrollableOffset == 0f && available.y > 0) {
                return Offset.Zero
            }

            if (available.y < 0) {
                // 手指往上, 首先让 header 隐藏
                //
                //                   y
                // |---------------| 0
                // |    TopAppBar  |
                // |  图片    标题  |  -scrollableHeight
                // |               |
                // |    收藏数据    |  scrollableOffset
                // |     TAB       |
                // |  LazyColumn   |
                // |---------------|

                val diff = available.y.coerceAtLeast(-scrollableHeight - scrollableOffset)
                scrollableOffset += diff
                return Offset(0f, diff)
            }
            return Offset.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            if (available.y > 0) { // 手指往下
                with(flingBehavior) {
                    scrollScope.performFling(available.y) // 让 headers 也跟着往下
                }
            }
            return super.onPostFling(consumed, available)
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            if (available.y > 0) {
                // 手指往下, 让 header 显示
                // scrollableOffset 是负的
                val diff = available.y.coerceAtMost(-scrollableOffset)
                scrollableOffset += diff
                return consumed + Offset(0f, diff)
            }
            return super.onPostScroll(consumed, available, source)
        }
    }
}

/**
 * 当 [ConnectedScrollState.nestedScrollConnection] 滚动时, 调整此 composable 的位置.
 */
fun Modifier.connectedScrollContainer(state: ConnectedScrollState): Modifier {
    return layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(
            placeable.width,
            placeable.height - state.scrollableOffset.roundToInt().absoluteValue,
        ) {
            placeable.place(0, y = state.scrollableOffset.roundToInt())
        }
    }
}

/**
 * 将该 composable 的高度作为可滚动的高度.
 */
fun Modifier.connectedScrollTarget(state: ConnectedScrollState): Modifier {
    return onPlaced { state.scrollableHeight = it.size.height }
}


/**
 * 同时应用 [connectedScrollContainer] 和 [connectedScrollTarget]
 */
fun Modifier.connectedScroll(state: ConnectedScrollState): Modifier {
    return connectedScrollContainer(state).connectedScrollTarget(state)
}