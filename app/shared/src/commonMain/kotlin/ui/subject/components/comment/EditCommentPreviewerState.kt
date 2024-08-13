package me.him188.ani.app.ui.subject.components.comment

import androidx.compose.runtime.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import me.him188.ani.app.data.source.CommentMapperContext
import me.him188.ani.app.ui.foundation.produceState

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
        .stateIn(coroutineScope, SharingStarted.Lazily, null)

    fun submitPreview(value: String) {
        richText.value = value
        _previewing.value = true
    }

    fun closePreview() {
        _previewing.value = false
    }
}