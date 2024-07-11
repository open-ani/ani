package me.him188.ani.app.ui.subject.episode.comments.bbcode

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.him188.ani.app.data.source.AssetLoader
import me.him188.ani.app.data.source.BangumiCommentSticker
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.richtext.UIRichElement
import me.him188.ani.utils.bbcode.BBCode
import me.him188.ani.utils.bbcode.RichElement
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BBCodeViewViewModel : AbstractViewModel(), KoinComponent {
    private val assetLoader by inject<AssetLoader>()

    private val _elements: MutableStateFlow<List<UIRichElement>> = MutableStateFlow(listOf())
    val elements: StateFlow<List<UIRichElement>> get() = _elements

    fun parseBBCode(code: String) {
        backgroundScope.launch {
            _elements.value = listOf()
            val richText = BBCode.parse(code)
            _elements.value = richText.elements.toUIRichElements()
        }
    }

    private fun List<RichElement>.toUIRichElements(): List<UIRichElement> = buildList {
        val annotated = mutableListOf<UIRichElement.Annotated>()

        this@toUIRichElements.forEach { e ->
            when (e) {
                is RichElement.Text -> annotated.add(
                    UIRichElement.Annotated.Text(
                        content = e.value,
                        size = e.size,
                        italic = e.italic,
                        underline = e.underline,
                        strikethrough = e.strikethrough,
                        bold = e.bold,
                        mask = e.mask,
                        code = e.code,
                        url = e.jumpUrl,
                    ),
                )

                is RichElement.BangumiSticker -> annotated.add(
                    UIRichElement.Annotated.Sticker(
                        id = "(bgm${e.id})",
                        uri = assetLoader.loadAsUri(BangumiCommentSticker[e.id] ?: "")?.toString(),
                        url = e.jumpUrl,
                    ),
                )

                is RichElement.Kanmoji -> annotated.add(
                    UIRichElement.Annotated.Sticker(
                        id = e.id,
                        uri = null, // TODO: path
                        url = e.jumpUrl,
                    ),
                )

                is RichElement.Quote -> {
                    if (annotated.isNotEmpty()) {
                        add(UIRichElement.AnnotatedText(annotated.toList()))
                        annotated.clear()
                    }
                    add(UIRichElement.Quote(e.contents.toUIRichElements()))
                }

                is RichElement.Image -> {
                    if (annotated.isNotEmpty()) {
                        add(UIRichElement.AnnotatedText(annotated.toList()))
                        annotated.clear()
                    }
                    add(UIRichElement.Image(e.imageUrl, e.jumpUrl))
                }
            }
        }

        if (annotated.isNotEmpty()) {
            add(UIRichElement.AnnotatedText(annotated.toList()))
            annotated.clear()
        }
    }
}