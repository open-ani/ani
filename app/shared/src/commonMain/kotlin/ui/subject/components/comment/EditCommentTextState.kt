package me.him188.ani.app.ui.subject.components.comment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

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