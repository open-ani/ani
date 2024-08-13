package me.him188.ani.app.ui.subject.components.comment

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.produceState
import org.jetbrains.compose.resources.DrawableResource
import kotlin.coroutines.CoroutineContext

@Stable
class EditCommentState(
    val showExpandEditCommentButton: Boolean,
    initialExpandEditComment: Boolean,
    val panelTitle: State<String?>,
    stickerProvider: Flow<List<EditCommentSticker>>,
    private val onSend: suspend (target: CommentSendTarget, content: String) -> Unit,
    backgroundScope: CoroutineScope,
) {
    private val editor = EditCommentTextState("")
    private val previewer = EditCommentPreviewerState(false, backgroundScope)

    private val sendTasker = MonoTasker(backgroundScope)

    /**
     * 评论是否正在提交发送，在 [send] 时设置为 true，当 [onSend] 返回或发生错误时设置为 false
     */

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

    suspend fun send(context: CoroutineContext = Dispatchers.Default) {
        val target = currentSendTarget
        val content = editor.textField.text

        editExpanded = false

        sendTasker.launch(context) {
            checkNotNull(target)
            onSend(target, content)
        }
        sendTasker.join()

        editor.override(TextFieldValue(""))
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