@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package me.him188.ani.app.ui.foundation.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldScope
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.layout.animateBounds
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import me.him188.ani.app.ui.foundation.animation.EmphasizedEasing

// 把过渡动画改为 fade 而不是带有回弹的 spring
@ExperimentalMaterial3AdaptiveApi
@Composable
fun ThreePaneScaffoldScope.AnimatedPane1(
    modifier: Modifier = Modifier,
    content: (@Composable AnimatedVisibilityScope.() -> Unit),
) {
    val keepShowing =
        scaffoldStateTransition.currentState[role] != PaneAdaptedValue.Hidden &&
                scaffoldStateTransition.targetState[role] != PaneAdaptedValue.Hidden
    val animateFraction = { scaffoldStateTransitionFraction }
    scaffoldStateTransition.AnimatedVisibility(
        visible = { value: ThreePaneScaffoldValue -> value[role] != PaneAdaptedValue.Hidden },
        modifier =
        modifier
            .animateBounds(
                animateFraction = animateFraction,
                positionAnimationSpec = tween(500, easing = EmphasizedEasing), // changed: custom animation spec
                sizeAnimationSpec = tween(500, easing = EmphasizedEasing), // changed: custom animation spec
                lookaheadScope = this,
                enabled = keepShowing,
            )
            .then(if (keepShowing) Modifier else Modifier.clipToBounds()),
        enter = fadeIn(), // changed 原生的动画会回弹, 与目前的整个 APP 设计风格相差太多了
        exit = fadeOut(), // changed
    ) {
        this.content()
    }
}
