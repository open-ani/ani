package me.him188.ani.app.ui.subject.components.comment

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.produceState
import me.him188.ani.app.ui.foundation.richtext.RichText
import me.him188.ani.app.ui.foundation.text.ProvideContentColor
import me.him188.ani.app.ui.foundation.theme.slightlyWeaken
import kotlin.math.max

/**
 * 评论编辑.
 *
 * @see EditCommentScaffold
 */
@Composable
fun EditComment(
    content: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    sending: Boolean = false,
    onContentChange: (String) -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onSend: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    var editExpanded by rememberSaveable { mutableStateOf(false) }
    val previewer = rememberEditCommentPreviewer()
    val editor = rememberEditCommentTextValue(content)

    EditCommentScaffold(
        title = {
            if (title != null) EditCommentDefaults.Title(title)
        },
        actionRow = {
            EditCommentDefaults.ActionRow(
                previewing = previewer.previewing,
                sending = sending,
                onClickBold = { editor.wrapSelectionWith("[b][/b]", 3) },
                onClickItalic = { editor.wrapSelectionWith("[i][/i]", 3) },
                onClickUnderlined = { editor.wrapSelectionWith("[u][/u]", 3) },
                onClickStrikethrough = { editor.wrapSelectionWith("[s][/s]", 3) },
                onClickMask = { editor.wrapSelectionWith("[mask][/mask]", 6) },
                onClickImage = { editor.wrapSelectionWith("[img][/img]", 5) },
                onClickUrl = { editor.wrapSelectionWith("[url=][/url]", 5) },
                onClickEmoji = { },
                onPreview = {
                    if (previewer.previewing) {
                        previewer.closePreview()
                    } else {
                        previewer.submitPreview(editor.textField.text)
                    }
                },
                onSend = {
                    focusManager.clearFocus()
                    onSend()
                },
            )
        },
        modifier = modifier,
        expanded = editExpanded,
        onClickExpanded = { editExpanded = it },
    ) {
        Crossfade(
            targetState = previewer.previewing,
            modifier = Modifier.composed {
                if (editExpanded) {
                    fillMaxHeight().animateContentSize().weight(1.0f)
                } else {
                    animateContentSize()
                }
            },
        ) { previewing ->
            val contentPadding = remember { PaddingValues(horizontal = 12.dp, vertical = 12.dp) }

            ProvideContentColor(MaterialTheme.colorScheme.onSurface) {
                if (previewing) {
                    val richText by previewer.list.collectAsState()
                    EditCommentDefaults.Preview(
                        content = richText,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = contentPadding,
                    )
                } else {
                    EditCommentDefaults.EditText(
                        value = editor.textField,
                        maxLine = if (editExpanded) null else 3,
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .fillMaxWidth()
                            .ifThen(editExpanded) { fillMaxHeight() },
                        contentPadding = contentPadding,
                        onValueChange = { editor.override(it) },
                    )
                    LaunchedEffect(true) {
                        focusRequester.requestFocus()
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberEditCommentPreviewer(
    initialPreviewing: Boolean = false
): EditCommentPreviewerState {
    val scope = rememberCoroutineScope()
    return remember(scope) {
        EditCommentPreviewerState(initialPreviewing, scope)
    }
}

@Composable
private fun rememberEditCommentTextValue(
    initialValue: String = ""
): EditCommentTextState {
    val scope = rememberCoroutineScope()
    return remember(scope) {
        EditCommentTextState(initialValue, scope)
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
                title()
            }
            if (expanded != null) {
                EditCommentDefaults.ActionButton(
                    imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    onClick = { onClickExpanded(!expanded) },
                )
            }

        }

        ProvideContentColor(contentColor) {
            Column {
                content()
            }
        }
        actionRow()

    }
}

private class EditCommentPreviewerState(
    initialPreviewing: Boolean,
    coroutineScope: CoroutineScope
) {
    private val richText = MutableStateFlow("")
    private val _previewing = MutableStateFlow(initialPreviewing)

    val previewing: Boolean by _previewing.produceState(false, coroutineScope)
    val list = richText
        .combine(_previewing) { text, preview ->
            if (!preview || text.isEmpty()) return@combine UIRichText(emptyList())
            with(CommentMapperContext) { parseBBCode(text) }
        }
        .stateIn(coroutineScope, SharingStarted.Lazily, UIRichText(emptyList()))

    fun submitPreview(value: String) {
        richText.value = value
        _previewing.value = true
    }

    fun closePreview() {
        _previewing.value = false
    }
}

private class EditCommentTextState(
    initialText: String,
    coroutineScope: CoroutineScope
) {
    private val textFlow = MutableStateFlow(TextFieldValue(initialText))

    val textField by textFlow.produceState(TextFieldValue(""), coroutineScope)

    /**
     * 在当前位置插入文本，清除 selection 状态
     *
     * @param value 插入的文本
     * @param cursorOffset 相较于插入前的 [TextFieldValue.selection] 偏移
     */
    fun insertTextAt(
        value: String,
        cursorOffset: Int = value.length
    ) {
        val current = textFlow.value
        val currentText = current.text
        val selectionLeft = current.selection.start

        val newText = buildString {
            append(currentText.take(selectionLeft))
            append(value)
            append(currentText.drop(selectionLeft))
        }

        textFlow.value = current.copy(
            annotatedString = AnnotatedString(newText),
            selection = TextRange((selectionLeft + cursorOffset).coerceIn(0..newText.lastIndex)),
        )
    }

    /**
     * 将当前选择的文本以字符包裹，若未选择文本则相当于 insert
     *
     * @param value 插入的文本
     * @param secondSliceIndex 将 [value] 按此索引一分为二，前半段不包含此索引
     */
    fun wrapSelectionWith(
        value: String,
        secondSliceIndex: Int
    ) {
        require(secondSliceIndex in 0..value.lastIndex) {
            "secondSliceIndex is out of bound. value length = ${value.length}, secondSliceIndex = $secondSliceIndex"
        }
        val current = textFlow.value
        if (current.selection.length == 0) {
            insertTextAt(value, secondSliceIndex)
            return
        }

        val secondSliceIndex = secondSliceIndex.coerceIn(0, max(0, value.length - 1))
        val currentText = current.text
        val selection = current.selection

        val newText = buildString {
            append(currentText.take(selection.start))
            append(value.take(secondSliceIndex))
            append(currentText.substring(selection.start, selection.end))
            append(value.substring(secondSliceIndex, value.length))
            append(currentText.substring(selection.end, currentText.length))
        }

        textFlow.value = current.copy(
            annotatedString = AnnotatedString(newText),
            selection = TextRange(
                selection.start + secondSliceIndex,
                selection.start + secondSliceIndex + selection.length,
            ),
        )
    }

    fun override(value: TextFieldValue) {
        textFlow.value = value
    }
}

object EditCommentDefaults {
    @Composable
    fun Title(text: String) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }

    @Composable
    fun EditText(
        value: TextFieldValue,
        onValueChange: (TextFieldValue) -> Unit,
        enabled: Boolean = true,
        hint: String? = null,
        maxLine: Int? = null,
        modifier: Modifier = Modifier,
        contentPadding: PaddingValues = PaddingValues(0.dp),
        interactionSource: InteractionSource = remember { MutableInteractionSource() },
    ) {
        BasicTextField(
            value = value,
            textStyle = MaterialTheme.typography.bodyMedium.merge(
                fontSize = 15.5.sp,
                color = LocalContentColor.current.slightlyWeaken(),
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = modifier,
            maxLines = maxLine ?: Int.MAX_VALUE,
            onValueChange = onValueChange,
            decorationBox = { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = value.text,
                    enabled = enabled,
                    innerTextField = innerTextField,
                    interactionSource = interactionSource,
                    singleLine = false,
                    visualTransformation = VisualTransformation.None,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                    ),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = contentPadding,
                )
            },
        )
    }

    @Composable
    fun Preview(
        content: UIRichText,
        modifier: Modifier = Modifier,
        contentPadding: PaddingValues = PaddingValues(0.dp)
    ) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            RichText(
                elements = content.elements,
                modifier = Modifier.padding(contentPadding),
            )
        }
    }

    @Composable
    fun ActionButton(imageVector: ImageVector, onClick: () -> Unit) {
        IconButton(
            modifier = Modifier.size(48.dp),
            onClick = onClick,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
        }
    }

    @Composable
    fun ActionRow(
        sending: Boolean = false,
        previewing: Boolean = false,

        onClickBold: () -> Unit,
        onClickItalic: () -> Unit,
        onClickUnderlined: () -> Unit,
        onClickStrikethrough: () -> Unit,
        onClickMask: () -> Unit,
        onClickImage: () -> Unit,
        onClickUrl: () -> Unit,
        onClickEmoji: () -> Unit,
        onSend: () -> Unit,
        onPreview: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FlowRow(modifier = Modifier.fillMaxWidth()) {
                ActionButton(Icons.Outlined.SentimentSatisfied, onClickEmoji)
                ActionButton(Icons.Outlined.FormatBold, onClickBold)
                ActionButton(Icons.Outlined.FormatItalic, onClickItalic)
                ActionButton(Icons.Outlined.FormatUnderlined, onClickUnderlined)
                ActionButton(Icons.Outlined.FormatStrikethrough, onClickStrikethrough)
                ActionButton(Icons.Outlined.VisibilityOff, onClickMask)
                ActionButton(Icons.Outlined.Image, onClickImage)
                ActionButton(Icons.Outlined.Link, onClickUrl)
            }
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextButton(
                    onClick = onPreview,
                    modifier = Modifier.padding(end = 4.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    Text(text = if (previewing) "编辑" else "预览")
                }
                OutlinedButton(
                    onClick = onSend,
                    enabled = !sending,
                    modifier = Modifier.padding(start = 4.dp).animateContentSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    Crossfade(targetState = sending) {
                        if (!it) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "发送")
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Send,
                                    modifier = Modifier.padding(start = 4.dp).size(18.dp),
                                    contentDescription = null,
                                )
                            }
                        } else {
                            CircularProgressIndicator(
                                Modifier.size(20.dp),
                                color = LocalContentColor.current,
                            )
                        }
                    }
                }
            }
        }
    }
}