package me.him188.ani.app.ui.subject.components.comment

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.produceState
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import org.jetbrains.compose.resources.DrawableResource

/**
 * [panelTitle] 将承担所有的 state 刷新工作，也就是此 state 的唯一标识
 */
@Stable
class EditCommentState(
    val showExpandEditCommentButton: Boolean,
    initialExpandEditComment: Boolean,
    title: StateFlow<String?>,
    stickerProvider: Flow<List<EditCommentSticker>>,
    private val onSend: suspend (target: CommentSendTarget, content: String) -> Unit,
    backgroundScope: CoroutineScope,
) {
    private val logger = logger(this::class)

    private var onSendCompleted: (() -> Unit)? = null
    private val editor = EditCommentTextState("")
    private val previewer = EditCommentPreviewerState(false, backgroundScope)

    private val sendTasker = MonoTasker(backgroundScope)

    /**
     * 评论是否正在提交发送，在 [send] 时设置为 true，当 [onSend] 返回或发生错误时设置为 false
     */

    val panelTitle by title.produceState(null, backgroundScope)
    var currentSendTarget: CommentSendTarget? by mutableStateOf(null)
        private set
    val content get() = editor.textField
    val previewing get() = previewer.previewing
    val previewContent get() = previewer.list
    var editExpanded: Boolean by mutableStateOf(initialExpandEditComment)
    var stickerPanelOpened: Boolean by mutableStateOf(false)
        private set
    val sending: Boolean get() = sendTasker.isRunning
    val stickers by stickerProvider
        .stateIn(backgroundScope, SharingStarted.Lazily, listOf())
        .produceState(emptyList(), backgroundScope)

    /**
     * 连续开关为同一个评论的编辑框将保存编辑内容和编辑框状态
     */
    fun startEdit(newTarget: CommentSendTarget) {
        if (newTarget != currentSendTarget) {
            editor.override(TextFieldValue(""))
        }
        currentSendTarget = newTarget
        previewer.closePreview()
        editExpanded = false
    }

    fun toggleStickerPanelState(desired: Boolean? = null) {
        stickerPanelOpened = desired ?: !stickerPanelOpened
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

    suspend fun send() {
        val target = currentSendTarget
        val content = editor.textField.text

        editExpanded = false

        sendTasker.launch(Dispatchers.Default) {
            if (target != null) {
                onSend(target, content)
            } else {
                logger.warn("current send target is null.")
            }
        }
        sendTasker.join()

        editor.override(TextFieldValue(""))
        onSendCompleted?.let { it() }
    }

    /**
     * Invoke after [onSend] completes, this will be in context of caller of [send].
     */
    fun invokeOnSendComplete(block: () -> Unit) {
        onSendCompleted = block
    }
}

@Immutable
data class EditCommentSticker(
    val id: Int,
    val drawableRes: DrawableResource?,
)

/**
 * 评论发送的对象，在 [EditCommentState.onSend] 需要提供。
 */
@Immutable
sealed interface CommentSendTarget {
    /**
     * 剧集评论
     */
    data class Episode(val subjectId: Int, val episodeId: Int) : CommentSendTarget

    /**
     * 番剧吐槽箱
     */
    data class Subject(val subjectId: Int) : CommentSendTarget

    /**
     * 番剧长评
     */
    data class SubjectLong(val subjectId: Int) : CommentSendTarget

    /**
     *  回复某个人的评论
     */
    data class Reply(val commentId: Int) : CommentSendTarget
}

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
    initialText: String
) {
    var textField by mutableStateOf(TextFieldValue(initialText))
        private set

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
        val current = textField
        val currentText = current.text
        val selectionLeft = current.selection.start

        val newText = buildString {
            append(currentText.take(selectionLeft))
            append(value)
            append(currentText.drop(selectionLeft))
        }

        textField = current.copy(
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
        require(secondSliceIndex in value.indices) {
            "secondSliceIndex is out of bound. value length = ${value.length}, secondSliceIndex = $secondSliceIndex"
        }
        val current = textField
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

        textField = current.copy(
            annotatedString = AnnotatedString(newText),
            selection = TextRange(
                selection.start + secondSliceIndex,
                selection.start + secondSliceIndex + selection.length,
            ),
        )
    }

    fun override(value: TextFieldValue) {
        textField = value
    }
}