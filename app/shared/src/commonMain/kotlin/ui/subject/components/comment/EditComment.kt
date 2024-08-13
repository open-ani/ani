package me.him188.ani.app.ui.subject.components.comment

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import kotlinx.coroutines.launch
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.interaction.isImeVisible
import me.him188.ani.app.ui.foundation.text.ProvideContentColor

/**
 * 评论编辑.
 *
 * @see EditCommentScaffold
 */
@Composable
fun EditComment(
    state: EditCommentState,
    modifier: Modifier = Modifier,
    controlSoftwareKeyboard: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() },
    stickerPanelHeight: Dp = EditCommentDefaults.MinStickerHeight.dp,
    onSendComplete: () -> Unit = { },
) {
    val scope = rememberCoroutineScope()
    val keyboard = if (!controlSoftwareKeyboard) null else LocalSoftwareKeyboardController.current
    val textFieldInteractionSource = remember { MutableInteractionSource() }

    val requiredStickerPanelHeight =
        remember(stickerPanelHeight) { max(EditCommentDefaults.MinStickerHeight.dp, stickerPanelHeight) }

    val imeVisible = isImeVisible()
    var previousImeVisible by remember { mutableStateOf(false) }
    LaunchedEffect(imeVisible) {
        if (!previousImeVisible && imeVisible) {
            state.toggleStickerPanelState(false)
        }
        previousImeVisible = imeVisible
    }

    EditCommentScaffold(
        modifier = modifier,
        title = {
            state.panelTitle?.let { EditCommentDefaults.Title(it) }
        },
        actionRow = {
            EditCommentDefaults.ActionRow(
                sendTarget = state.currentSendTarget,
                previewing = state.previewing,
                sending = state.sending,
                onClickBold = { state.wrapSelectionWith("[b][/b]", 3) },
                onClickItalic = { state.wrapSelectionWith("[i][/i]", 3) },
                onClickUnderlined = { state.wrapSelectionWith("[u][/u]", 3) },
                onClickStrikethrough = { state.wrapSelectionWith("[s][/s]", 3) },
                onClickMask = { state.wrapSelectionWith("[mask][/mask]", 6) },
                onClickImage = { state.wrapSelectionWith("[img][/img]", 5) },
                onClickUrl = { state.wrapSelectionWith("[url=][/url]", 5) },
                onClickEmoji = {
                    state.toggleStickerPanelState()
                    if (state.stickerPanelOpened) keyboard?.hide()
                },
                onPreview = {
                    keyboard?.hide()
                    state.toggleStickerPanelState(false)
                    state.togglePreview()
                },
                onSend = {
                    keyboard?.hide()
                    scope.launch {
                        state.send()
                        onSendComplete()
                    }
                },
            )
            if (state.stickerPanelOpened) {
                EditCommentDefaults.StickerSelector(
                    list = state.stickers,
                    modifier = Modifier.fillMaxWidth().height(requiredStickerPanelHeight),
                    onClickItem = { stickerId ->
                        val inserted = "(bgm$stickerId)"
                        state.insertTextAt(inserted, inserted.length)
                    },
                )
            }
        },
        expanded = if (!state.showExpandEditCommentButton) null else state.editExpanded,
        onClickExpanded = { state.editExpanded = it },
    ) {

        Crossfade(
            targetState = state.previewing,
            modifier = Modifier.weight(1.0f, fill = false),
        ) { previewing ->
            ProvideContentColor(MaterialTheme.colorScheme.onSurface) {
                if (previewing) {
                    val richText by state.previewContent.collectAsState()
                    EditCommentDefaults.Preview(
                        content = richText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .ifThen(state.editExpanded) { fillMaxHeight() }
                            .animateContentSize(),
                        contentPadding = OutlinedTextFieldDefaults.contentPadding(),
                    )
                } else {
                    EditCommentDefaults.EditText(
                        value = state.content,
                        maxLines = if (state.editExpanded) Int.MAX_VALUE else 3,
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .fillMaxWidth()
                            .ifThen(state.editExpanded) { fillMaxHeight() }
                            .animateContentSize(),
                        onValueChange = { state.setContent(it) },
                        interactionSource = textFieldInteractionSource,
                    )
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                }
            }
        }
    }
}

/**
 * 评论编辑 Scaffold
 *
 * @param actionRow 操作按钮, 进行富文本编辑和评论发送. see [EditCommentDefaults.ActionRow].
 * @param expanded 展开按钮状态, 为 `null` 时不显示按钮.
 * @param onClickExpanded 点击展开按钮时触发该点击事件.
 * @param title 评论编辑标题, 一般显示 正在为哪个对象发送评论. see [EditCommentDefaults.Title].
 * @param content 评论编辑框. see [EditCommentDefaults.EditText].
 */
@Composable
fun EditCommentScaffold(
    actionRow: @Composable ColumnScope.() -> Unit,
    onClickExpanded: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean? = null,
    title: (@Composable () -> Unit)? = null,
    contentColor: Color = Color.Unspecified,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (title != null) {
                Box(Modifier.weight(1.0f)) {
                    title()
                }
            }
            if (expanded != null) {
                EditCommentDefaults.ActionButton(
                    imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    enabled = true,
                    onClick = { onClickExpanded(!expanded) },
                )
            }

        }

        ProvideContentColor(contentColor) {
            content()
        }
        Column {
            actionRow()
        }

    }
}