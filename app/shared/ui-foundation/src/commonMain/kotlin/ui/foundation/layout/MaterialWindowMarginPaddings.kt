package me.him188.ani.app.ui.foundation.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass

/**
 * 每个窗口 (页面) 距离窗口边缘的间距.
 *
 * 根据当前设备大小决定.
 */
fun Modifier.materialWindowMarginPadding(): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "materialWindowMarginPadding"
    },
) {
    // https://m3.material.io/foundations/layout/applying-layout
    val values = when (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> {
            // https://m3.material.io/foundations/layout/applying-layout/compact#5a83ddd7-137f-4657-ba2d-eb08cac065e7
            MaterialWindowMarginPaddings.COMPACT
        }

        // medium, expanded, large 都是 24
        else -> {
            MaterialWindowMarginPaddings.EXPANDED
        }
    }
    padding(values)
}


private object MaterialWindowMarginPaddings {
    // single instance
    val COMPACT = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
    val EXPANDED = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp)
}
