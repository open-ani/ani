package me.him188.ani.app.ui.foundation.widgets

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.him188.ani.app.tools.MonoTasker

@Stable
inline val ProgressIndicatorHeight get() = 4.dp

enum class Mode {
    Indefinite,
    Definite,
}

private val ScaleAnimation: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMedium)
private val ProgressAnimation: AnimationSpec<Float> = tween(1500)

@Stable
class FastLinearProgressState(
    uiScope: CoroutineScope,
) {
    private val tasker = MonoTasker(uiScope)
    private var targetVisible by mutableStateOf(false)

    internal var mode: Mode by mutableStateOf(Mode.Indefinite)
    internal var scale: Float by mutableFloatStateOf(0f)
        private set
    internal var progress: Float by mutableFloatStateOf(0f)
        private set

    fun setVisible(visible: Boolean) {
        if (targetVisible == visible) return
        targetVisible = visible

        if (visible) {
            this.mode = Mode.Definite
            tasker.launch {
                delay(100) // 超级快的动作, 不展示进度条
                launch {
                    animate(
                        scale, 1f,
                        animationSpec = ScaleAnimation,
                    ) { value, _ ->
                        scale = value
                    }
                }

                animate(
                    0f, 1f,
                    animationSpec = ProgressAnimation,
                ) { value, _ ->
                    progress = value
                }

                if (targetVisible) {
                    mode = Mode.Indefinite
                } else {
                    // 其他线程已经修改, 期望提前结束
                }
            }
        } else {
            tasker.launchNext {
                animate(
                    scale, 0f,
                    animationSpec = ScaleAnimation,
                ) { value, _ ->
                    scale = value
                }
                mode = Mode.Definite // 恢复状态, 避免下次显示时会有一点点
            }
        }
    }

    suspend fun awaitInvisible() {
        withContext(Dispatchers.Main.immediate) {
            if (!targetVisible) return@withContext // fast path
            snapshotFlow { targetVisible }.filter { !it }.first()
        }
    }
}


/**
 * 总是展示 1.5 秒动画, 适用于那些加载很快的小动作.
 *
 * 如果超过 1.5 秒后 [visible] 仍然为 `true`, 进度条将会转为 indefinite 模式.
 */
@Composable
fun FastLinearProgressIndicator(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val state = remember(scope) { FastLinearProgressState(scope) }
    SideEffect {
        state.setVisible(visible)
    }
    return FastLinearProgressIndicator(state, modifier)
}

/**
 * 总是展示 1.5 秒动画, 适用于那些加载很快的小动作.
 */
@Composable
fun FastLinearProgressIndicator(
    state: FastLinearProgressState,
    modifier: Modifier = Modifier,
) {
    val progressModifier = Modifier.fillMaxWidth().graphicsLayer { scaleY = state.scale }
    val trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
    val cap = StrokeCap.Round

    Crossfade(state.mode, modifier = modifier.height(4.dp).fillMaxWidth()) { mode ->
        if (mode == Mode.Definite) {
            LinearProgressIndicator(
                { state.progress },
                progressModifier,
                trackColor = trackColor,
                strokeCap = cap,
            )
        } else {
            LinearProgressIndicator(
                progressModifier,
                trackColor = trackColor,
                strokeCap = cap,
            )
        }
    }
}
//
//@Composable
//fun AnimatedLinearProgressIndicator(
//    visible: Boolean,
//    modifier: Modifier = Modifier,
//    progress: (() -> Float)? = null,
//) {
//    val scale by animateFloatAsState(
//        if (visible) 1f else 0f,
//        spring(stiffness = Spring.StiffnessMedium),
//    )
//    val progressModifier = Modifier.fillMaxWidth().graphicsLayer { scaleY = scale }
//    val trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
//    val cap = StrokeCap.Round
//    Crossfade(progress, modifier = modifier.height(4.dp).fillMaxWidth()) { pro ->
//        if (pro == null) {
//            var indefinite by remember(visible) { mutableStateOf(false) }
//            LaunchedEffect(true) {
//                delay(1500) // 一秒后转为无限模式
//                indefinite = true
//            }
//            if (indefinite) {
//                LinearProgressIndicator(
//                    progressModifier,
//                    trackColor = trackColor,
//                    strokeCap = cap,
//                )
//            } else {
//                val scope = rememberCoroutineScope()
//                var prog by remember {
//                    mutableFloatStateOf(0f)
//                }
//                SideEffect {
//                    scope.launch {
//                        animate(
//                            0f, 1f,
//                            animationSpec = tween(1500)
//                        ) { value, _ ->
//                            prog = value
//                        }
//                    }
//                }
//
//                LinearProgressIndicator(
//                    { prog },
//                    progressModifier,
//                    trackColor = trackColor,
//                    strokeCap = cap,
//                )
//            }
//        } else {
//            LinearProgressIndicator(
//                pro,
//                progressModifier,
//                trackColor = trackColor,
//                strokeCap = cap,
//            )
//        }
//    }
//}
