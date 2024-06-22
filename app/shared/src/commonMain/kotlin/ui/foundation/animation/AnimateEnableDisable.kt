package me.him188.ani.app.ui.foundation.animation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.debugInspectorInfo

fun Modifier.animateEnable(enabled: Boolean): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "animateEnable"
        properties["enabled"] = enabled
    },
) {
    // Animate alpha value based on the enabled state
    val alpha by animateFloatAsState(targetValue = if (enabled) 1f else 0.38f)
    this.alpha(alpha)
}
