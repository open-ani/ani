package me.him188.ani.app.ui.foundation.richtext.impl

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.him188.ani.app.data.source.BangumiCommentSticker
import me.him188.ani.app.tools.HtmlColor
import me.him188.ani.app.ui.foundation.richtext.RichTextDefaults
import me.him188.ani.app.ui.foundation.richtext.UIRichElement
import me.him188.ani.utils.bbcode.BBCode
import me.him188.ani.utils.bbcode.RichElement
import me.him188.ani.utils.bbcode.RichText
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn

@Composable
fun rememberBBCodeRichTextState(
    initialText: String, 
    defaultTextSize: TextUnit = LocalTextStyle.current.fontSize,
): BBCodeRichTextState {
    val scope = rememberCoroutineScope()
    return remember(scope) { 
        BBCodeRichTextState(initialText, defaultTextSize, scope)
    }
}

@Stable
class BBCodeRichTextState(
    initialText: String, 
    defaultTextSize: TextUnit,
    scope: CoroutineScope
) {
    private val logger = logger(this::class)
    
    private val textFlow = MutableStateFlow(initialText)
    var elements: List<UIRichElement> by mutableStateOf(listOf())
        private set
    
    init {
        scope.launch { 
            textFlow.collectLatest { code ->
                val richText = try { BBCode.parse(code) } catch (ex: Exception) {
                    logger.warn(ex) { "failed to parse bbcode \"$code\"" }
                    return@collectLatest
                }
                elements = richText.toUIRichElements(defaultTextSize.value)
            }
        }
    }
    
    fun setText(text: String) {
        textFlow.value = text
    }
}

// TODO: move to BBCodeRichTextState
fun RichText.toUIRichElements(overrideTextSize: Float? = null): List<UIRichElement> = buildList {
    val annotated = mutableListOf<UIRichElement.Annotated>()

    elements.forEach { e ->
        when (e) {
            is RichElement.Text -> {
                if (e.value.trim().isNotEmpty()) {
                    annotated.add(
                        UIRichElement.Annotated.Text(
                            content = e.value,
                            size = overrideTextSize ?: e.size.toFloat(),
                            color = HtmlColor.parse(e.color),
                            italic = e.italic,
                            underline = e.underline,
                            strikethrough = e.strikethrough,
                            bold = e.bold,
                            mask = e.mask,
                            code = e.code,
                            url = e.jumpUrl,
                        ),
                    )
                }
            }

            is RichElement.BangumiSticker -> annotated.add(
                UIRichElement.Annotated.Sticker(
                    id = "(bgm${e.id})",
                    resource = BangumiCommentSticker[e.id],
                    url = e.jumpUrl,
                ),
            )

            is RichElement.Kanmoji -> annotated.add(
                UIRichElement.Annotated.Sticker(
                    id = e.id,
                    resource = null, // TODO: path
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

fun RichText.toUIBriefText(): UIRichElement.AnnotatedText {
    var plainText = String()
    val annotated = mutableListOf<UIRichElement.Annotated>()

    elements.forEach { e ->
        when (e) {
            is RichElement.Text -> plainText += e.value.replace('\n', ' ')
            is RichElement.Image -> plainText += "[图片]"
            is RichElement.Kanmoji -> plainText += e.id
            is RichElement.Quote -> plainText += "[引用]"
            is RichElement.BangumiSticker -> {
                if (plainText.isNotEmpty()) {
                    annotated.add(UIRichElement.Annotated.Text(plainText, RichTextDefaults.FontSize))
                    plainText = ""
                }
                annotated.add(
                    UIRichElement.Annotated.Sticker(
                        id = "(bgm${e.id})",
                        resource = BangumiCommentSticker[e.id],
                        url = e.jumpUrl,
                    ),
                )
            }
        }
    }

    if (plainText.isNotEmpty()) {
        annotated.add(UIRichElement.Annotated.Text(plainText, RichTextDefaults.FontSize))
        plainText = ""
    }

    return UIRichElement.AnnotatedText(annotated)
}