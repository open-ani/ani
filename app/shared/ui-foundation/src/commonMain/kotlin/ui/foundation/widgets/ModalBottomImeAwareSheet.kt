package me.him188.ani.app.ui.foundation.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.him188.ani.app.ui.foundation.dialogs.PlatformPopupProperties

@Composable
fun rememberModalBottomImeAwareSheetState(): ModalBottomImeAwareSheetState {
    return remember { ModalBottomImeAwareSheetState() }
}

@Stable
class ModalBottomImeAwareSheetState {
    var dismissed: Boolean by mutableStateOf(false)

    fun close() {
        dismissed = true
    }
}

/**
 * 与 [ModalBottomSheet] 相似, 但不使用 [imePadding] 和任何 [WindowInsets]
 *
 * 默认的 [ModalBottomSheet] 在 Android 平台总是为悬浮窗口添加了 [imePadding],
 * 此实现移除了 [imePadding].
 *
 * @param onDismiss 在关闭动画完成时调用
 */
@Composable
fun ModalBottomImeAwareSheet(
    // TODO: Support window insets
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    state: ModalBottomImeAwareSheetState = rememberModalBottomImeAwareSheetState(),
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = BottomSheetDefaults.Elevation,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // 用于控制动画
    var presentVisibility by rememberSaveable { mutableStateOf(false) }
    val scrimAlpha by animateFloatAsState(
        targetValue = if (presentVisibility) 1f else 0f,
        animationSpec = tween(),
    )
    var sheetHeight by rememberSaveable { mutableStateOf(0) }
    val sheetOffset by remember { derivedStateOf { sheetHeight * (1 - scrimAlpha) } }

    val animateToDismiss: () -> Unit = {
        focusManager.clearFocus(force = true)
        presentVisibility = false
        scope.launch {
            snapshotFlow { scrimAlpha }.collect {
                if (it == 0f && !presentVisibility) onDismiss()
            }
        }
    }
    LaunchedEffect(Unit) {
        presentVisibility = true
        snapshotFlow { state.dismissed }
            .distinctUntilChanged()
            .collect { if (it) animateToDismiss() }
    }

    ModalBottomImeAwareSheetPopup(
        popupPositionProvider = remember {
            object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset {
                    return IntOffset.Zero
                }
            }
        },
        onDismissRequest = animateToDismiss,
        properties = PlatformPopupProperties(
            focusable = true,
            usePlatformDefaultWidth = false,
            excludeFromSystemGesture = false,
            clippingEnabled = false,
        ),
    ) {
        BoxWithConstraints {
            Canvas(
                modifier = Modifier.fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = animateToDismiss,
                    ),
            ) {
                drawRect(color = scrimColor, alpha = scrimAlpha)
            }
            Surface(
                modifier = Modifier
                    .widthIn(max = sheetMaxWidth)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .graphicsLayer { translationY = sheetOffset },
                shape = shape,
                color = containerColor,
                contentColor = contentColor,
                tonalElevation = tonalElevation,
            ) {
                Box(modifier = modifier.onSizeChanged { sheetHeight = it.height }) {
                    content()
                }
            }
        }
    }
}