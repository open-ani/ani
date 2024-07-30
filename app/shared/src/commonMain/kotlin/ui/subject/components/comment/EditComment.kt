package me.him188.ani.app.ui.subject.components.comment

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Indication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.him188.ani.app.ui.foundation.IconButton
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.interaction.isImeVisible
import me.him188.ani.app.ui.foundation.produceState
import me.him188.ani.app.ui.foundation.richtext.RichText
import me.him188.ani.app.ui.foundation.text.ProvideContentColor
import me.him188.ani.app.ui.foundation.theme.looming
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Stable
class EditCommentState(
    val showExpandEditCommentButton: Boolean,
    initialExpandEditComment: Boolean,
    val panelTitle: String? = null,
    private val stickerProvider: suspend () -> List<EditCommentSticker>,
    private val onSend: suspend (String) -> Unit,
    private val backgroundScope: CoroutineScope,
) {
    private var currentSendTarget = 0

    private var onSendCompleted: (() -> Unit)? = null
    private val editor = EditCommentTextState("", backgroundScope)
    private val previewer = EditCommentPreviewerState(false, backgroundScope)
    private val _editExpanded: MutableState<Boolean> = mutableStateOf(initialExpandEditComment)

    /**
     * 评论是否正在提交发送，在 [send] 时设置为 true，当 [onSend] 返回或发生错误时设置为 false
     */
    private val _sending: MutableState<Boolean> = mutableStateOf(false)

    val content get() = editor.textField
    val previewing get() = previewer.previewing
    val previewContent get() = previewer.list
    val editExpanded: Boolean by _editExpanded
    val sending: Boolean by _sending
    val stickers = flow { emit(stickerProvider()) }
        .stateIn(backgroundScope, SharingStarted.Lazily, listOf())

    /**
     * 连续开关为同一个评论的编辑框将保存编辑内容和编辑框状态
     */
    fun handleNewEdit(id: Int) {
        if (id != currentSendTarget) {
            editor.override(TextFieldValue(""))
        }
        currentSendTarget = id
    }

    fun setEditExpanded(value: Boolean) {
        _editExpanded.value = value
    }

    fun setContent(value: TextFieldValue) {
        editor.override(value)
    }

    /**
     * @see EditCommentTextState.wrapSelectionWith
     */
    fun wrapSelectionWith(value: String, secondSliceIndex: Int) {
        editor.wrapSelectionWith(value, secondSliceIndex)
    }

    /**
     * @see EditCommentTextState.insertTextAt
     */
    fun insertTextAt(value: String, cursorOffset: Int = value.length) {
        editor.insertTextAt(value, cursorOffset)
    }

    /**
     * @see EditCommentPreviewerState.closePreview
     * @see EditCommentPreviewerState.submitPreview
     */
    fun togglePreview() {
        if (previewing) {
            previewer.closePreview()
        } else {
            previewer.submitPreview(editor.textField.text)
        }
    }

    fun send() {
        val value = editor.textField.text

        _editExpanded.value = false
        _sending.value = true

        backgroundScope.launch {
            onSend(value)
            withContext(Dispatchers.Main) {
                _sending.value = false
            }
        }.invokeOnCompletion {
            editor.override(TextFieldValue(""))
            if (_sending.value) _sending.value = false
            onSendCompleted?.let { it() }
        }
    }

    fun invokeOnSendComplete(block: () -> Unit) {
        onSendCompleted = block
    }
}

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
    onStickerPanelStateChanged: (Boolean) -> Unit = { },
) {
    val keyboard = if (!controlSoftwareKeyboard) null else LocalSoftwareKeyboardController.current
    
    val textFieldInteractionSource = remember { MutableInteractionSource() }
    var stickerPanelExpanded by rememberSaveable { mutableStateOf(false) }

    val requiredStickerPanelHeight =
        remember(stickerPanelHeight) { max(EditCommentDefaults.MinStickerHeight.dp, stickerPanelHeight) }

    val imeVisible = isImeVisible()
    var previousImeVisible by remember { mutableStateOf(false) }
    LaunchedEffect(imeVisible) {
        if (!previousImeVisible && imeVisible) {
            stickerPanelExpanded = false
            onStickerPanelStateChanged(false)
        }
        previousImeVisible = imeVisible
    }

    EditCommentScaffold(
        modifier = modifier,
        title = {
            if (state.panelTitle != null) EditCommentDefaults.Title(state.panelTitle)
        },
        actionRow = {
            EditCommentDefaults.ActionRow(
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
                    stickerPanelExpanded = !stickerPanelExpanded
                    if (stickerPanelExpanded) keyboard?.hide()
                    onStickerPanelStateChanged(stickerPanelExpanded)
                },
                onPreview = {
                    keyboard?.hide()
                    if (stickerPanelExpanded) stickerPanelExpanded = false
                    state.togglePreview()
                },
                onSend = {
                    keyboard?.hide()
                    state.send()
                },
            )
            if (stickerPanelExpanded) {
                val stickers by state.stickers.collectAsStateWithLifecycle()
                
                EditCommentDefaults.StickerSelector(
                    list = stickers,
                    modifier = Modifier.fillMaxWidth().height(requiredStickerPanelHeight),
                    onClickItem = { stickerId ->
                        val inserted = "(bgm$stickerId)"
                        state.insertTextAt(inserted, inserted.length)
                    },
                )
            }
        },
        expanded = if (!state.showExpandEditCommentButton) null else state.editExpanded,
        onClickExpanded = { state.setEditExpanded(it) },
    ) {
        Crossfade(
            targetState = state.previewing,
            modifier = Modifier.ifThen(state.editExpanded) { weight(1.0f) },
        ) { previewing ->
            val contentPadding = remember { PaddingValues(horizontal = 12.dp, vertical = 12.dp) }

            ProvideContentColor(MaterialTheme.colorScheme.onSurface) {
                if (previewing) {
                    val richText by state.previewContent.collectAsState()
                    EditCommentDefaults.Preview(
                        content = richText,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = contentPadding,
                    )
                } else {
                    EditCommentDefaults.EditText(
                        value = state.content,
                        maxLine = if (state.editExpanded) null else 3,
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .fillMaxWidth()
                            .ifThen(state.editExpanded) { fillMaxHeight() }
                            .animateContentSize(),
                        contentPadding = contentPadding,
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

@Composable
private fun rememberEditCommentPreviewer(
    initialPreviewing: Boolean = false
): EditCommentPreviewerState {
    val scope = rememberCoroutineScope()
    return remember(scope) {
        EditCommentPreviewerState(initialPreviewing, scope)
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

data class EditCommentSticker(
    val id: Int,
    val drawableRes: DrawableResource?,
)

class EditCommentPreviewerState(
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

class EditCommentTextState(
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
            selection = TextRange((selectionLeft + cursorOffset).coerceIn(0..newText.lastIndex + 1)),
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
    @Suppress("ConstPropertyName")
    private const val ActionButtonSize: Int = 48

    @Suppress("ConstPropertyName")
    private const val ActionRowPrimaryAction: String = "primaryAction"

    @Suppress("ConstPropertyName")
    const val MinStickerHeight: Int = 192
    
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
                color = LocalContentColor.current.looming(),
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
    fun ActionButton(
        imageVector: ImageVector,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        iconSize: Dp = 22.dp,
        indication: Indication? = rememberRipple(
            bounded = false,
            radius = 20.dp, /* IconButtonTokens.StateLayerSize / 2 */
        )
    ) {
        me.him188.ani.app.ui.foundation.IconButton(
            modifier = modifier,
            enabled = enabled,
            onClick = onClick,
            indication = indication,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
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
        // maybe we can extract the layout
        val size = ActionButtonSize.dp
        var actionExpanded by rememberSaveable { mutableStateOf(false) }
        val collapsedActionAnim by animateDpAsState(if (actionExpanded) size else 0.dp)
        val reversedActionAnim by derivedStateOf { size - collapsedActionAnim }

        Layout(
            modifier = modifier.then(Modifier.fillMaxWidth().animateContentSize()),
            measurePolicy = { measurables, rawConstraints ->
                val constraints = rawConstraints.copy(minWidth = 0)
                val containerWidth = constraints.maxWidth
                val primaryAction = measurables.find { it.layoutId == ActionRowPrimaryAction }
                val textActions = measurables.filter { it.layoutId != ActionRowPrimaryAction }

                var currentAccumulatedWidth = 0
                val currentList = mutableListOf<Placeable>()

                val textPlaceables = textActions.foldIndexed(mutableListOf<List<Placeable>>()) { i, acc, curr ->
                    val placeable = curr.measure(constraints)

                    // 当前一行显示不下了，需要放到下一行
                    if (currentAccumulatedWidth + placeable.width > containerWidth) {
                        acc.add(currentList.toList())
                        currentList.clear()
                        currentAccumulatedWidth = 0
                    }

                    currentList.add(placeable)
                    currentAccumulatedWidth += placeable.width

                    if (i == textActions.lastIndex && currentList.isNotEmpty()) {
                        acc.add(currentList.toList())
                    }

                    acc
                }

                val primaryActionPlaceable = primaryAction?.measure(constraints)
                val primaryActionWidth = primaryActionPlaceable?.width ?: 0
                val primaryActionHeight = primaryActionPlaceable?.height ?: 0

                val shouldAppendNewLine =
                    primaryActionWidth + (textPlaceables.lastOrNull()?.sumOf { it.width } ?: 0) > containerWidth

                layout(
                    width = constraints.maxWidth,
                    height = textPlaceables.sumOf { it.maxOf { p -> p.height } } +
                            if (shouldAppendNewLine) primaryActionHeight else 0,
                ) {
                    var currentX = 0
                    var currentY = 0
                    var lastLineMaxHeight = 0
                    textPlaceables.forEachIndexed { i, line ->
                        val lineMaxHeight = maxOf(
                            line.maxOf { it.height },
                            if (!shouldAppendNewLine) primaryActionHeight else 0,
                        )

                        line.forEach { p ->
                            // center vertically
                            p.placeRelative(currentX, currentY + (lineMaxHeight - p.height) / 2)
                            currentX += p.width
                        }

                        currentX = 0
                        currentY += lineMaxHeight
                        if (i == textPlaceables.lastIndex) {
                            lastLineMaxHeight = lineMaxHeight
                        }
                    }
                    val adjustedYOffset = if (shouldAppendNewLine) 0 else {
                        -(lastLineMaxHeight + primaryActionHeight) / 2
                    }
                    primaryActionPlaceable?.placeRelative(
                        x = containerWidth - primaryActionWidth,
                        y = currentY + adjustedYOffset,
                    )
                }
            },
            content = {
                ActionButton(Icons.Outlined.SentimentSatisfied, onClickEmoji, Modifier.size(size), !sending)
                if (actionExpanded) {
                    ActionButton(
                        Icons.Outlined.FormatBold,
                        onClickBold,
                        Modifier.size(height = size, width = collapsedActionAnim),
                        !sending,
                    )
                    ActionButton(
                        Icons.Outlined.FormatItalic,
                        onClickItalic,
                        Modifier.size(height = size, width = collapsedActionAnim),
                        !sending,
                    )
                    ActionButton(
                        Icons.Outlined.FormatUnderlined,
                        onClickUnderlined,
                        Modifier.size(height = size, width = collapsedActionAnim),
                        !sending,
                    )
                    ActionButton(
                        Icons.Outlined.FormatStrikethrough,
                        onClickStrikethrough,
                        Modifier.size(height = size, width = collapsedActionAnim),
                        !sending,
                    )
                }
                ActionButton(Icons.Outlined.VisibilityOff, onClickMask, Modifier.size(size), !sending)
                ActionButton(Icons.Outlined.Image, onClickImage, Modifier.size(size), !sending)
                if (actionExpanded) {
                    ActionButton(
                        Icons.Outlined.Link,
                        onClickUrl,
                        Modifier.size(height = size, width = collapsedActionAnim),
                        !sending,
                    )
                }
                // 最后一个按钮不要有 ripple effect，看起来比较奇怪
                ActionButton(
                    imageVector = Icons.Outlined.MoreHoriz,
                    enabled = !sending,
                    onClick = { actionExpanded = true },
                    modifier = Modifier.size(height = size, width = reversedActionAnim),
                    indication = null,
                )

                Row(modifier = Modifier.layoutId(ActionRowPrimaryAction)) {
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
            },
        )
    }

    @Composable
    fun StickerSelector(
        list: List<EditCommentSticker>,
        onClickItem: (id: Int) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val previewing = LocalIsPreviewing.current

        LazyColumn(modifier = modifier) {
            item {
                FlowRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Start,
                ) {
                    list.forEach { sticker ->
                        IconButton(onClick = { onClickItem(sticker.id) }) {
                            if (previewing || sticker.drawableRes == null) {
                                Icon(Icons.Outlined.SentimentSatisfied, contentDescription = null)
                            } else {
                                Icon(painterResource(sticker.drawableRes), contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}