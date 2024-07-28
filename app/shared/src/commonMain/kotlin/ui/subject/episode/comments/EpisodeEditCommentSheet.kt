package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import kotlinx.coroutines.launch
import me.him188.ani.app.platform.PlatformPopupProperties
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.subject.components.comment.EditComment
import me.him188.ani.app.ui.subject.components.comment.EditCommentSticker
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel
import kotlin.math.max

@Composable
fun EpisodeEditCommentSheet(
    vm: EpisodeViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
    val contentPadding = 16.dp

    var visible by remember { mutableStateOf(false) }
    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = TweenSpec(),
    )
    var sheetHeight by remember { mutableStateOf(0) }
    val sheetOffset by derivedStateOf { sheetHeight * (1 - scrimAlpha) }

    val animateToDismiss: () -> Unit = {
        focusManager.clearFocus(force = true)
        visible = false
        scope.launch {
            snapshotFlow { scrimAlpha }.collect {
                if (it == 0f && !visible) onDismiss()
            }
        }
    }
    LaunchedEffect(Unit) {
        visible = true
    }

    val statusBarPadding = WindowInsets.statusBars.getTop(density)
    val imePadding = WindowInsets.ime.getBottom(density)
    val navigationBarPadding = WindowInsets.navigationBars.getBottom(density)
    var imePresentHeight by remember { mutableStateOf(0) }
    LaunchedEffect(imePadding) {
        imePresentHeight = max(imePresentHeight, imePadding - navigationBarPadding)
    }

    var stickerPanelOpened by remember { mutableStateOf(false) }

    // Popup on android always clip its composeView to visible 
    Popup(
        popupPositionProvider = object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                return IntOffset(0, statusBarPadding)
            }
        },
        onDismissRequest = animateToDismiss,
        properties = PlatformPopupProperties(
            focusable = true,
            usePlatformDefaultWidth = false,
            usePlatformInsets = false,
            clippingEnabled = false,
        ),
    ) {
        BoxWithConstraints(Modifier.windowInsetsPadding(BottomSheetDefaults.windowInsets)) {
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
                    .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .graphicsLayer { translationY = sheetOffset },
                shape = BottomSheetDefaults.ExpandedShape,
                color = BottomSheetDefaults.ContainerColor,
                contentColor = contentColorFor(BottomSheetDefaults.ContainerColor),
                tonalElevation = BottomSheetDefaults.Elevation,
            ) {
                Layout(
                    modifier = Modifier.ifThen(!stickerPanelOpened) { imePadding() },
                    content = {
                        Box {
                            EditComment(
                                content = "",
                                title = "评论: ${vm.episodeDetailsState.subjectTitle}",
                                modifier = modifier.padding(top = contentPadding).padding(contentPadding),
                                onContentChange = { },
                                stickers = generateSequence(1) { it + 1 }
                                    .take(64)
                                    .map { EditCommentSticker(it, null) }
                                    .toList(),
                                stickerPanelHeight = with(density) { imePresentHeight.toDp() },
                                onStickerPanelStateChanged = { stickerPanelOpened = it },
                                controlSoftwareKeyboard = true,
                                focusRequester = focusRequester,
                                onSend = { animateToDismiss() },
                            )
                        }
                    },
                ) { measurable, constraint ->
                    val placeable = measurable.single().measure(constraint.copy(minWidth = 0, minHeight = 0))
                    sheetHeight = placeable.height
                    layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
                }
            }
        }
    }
}