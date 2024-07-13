package me.him188.ani.app.ui.foundation.richtext

import androidx.compose.ui.graphics.Color
import me.him188.ani.app.data.source.BangumiCommentSticker
import me.him188.ani.app.tools.HtmlColor
import me.him188.ani.utils.bbcode.RichElement
import me.him188.ani.utils.bbcode.RichText
import org.jetbrains.compose.resources.DrawableResource

interface UIRichElement {
    sealed interface Annotated {
        val url: String?

        data class Text(
            val content: String,
            val size: Int = RichTextDefaults.FontSize,
            val color: Color = Color.Unspecified,

            val italic: Boolean = false,
            val underline: Boolean = false,
            val strikethrough: Boolean = false,
            val bold: Boolean = false,

            val mask: Boolean = false,
            val code: Boolean = false,

            override val url: String? = null
        ) : Annotated

        data class Sticker(
            val id: String,
            val resource: DrawableResource?,
            override val url: String? = null
        ) : Annotated
    }

    data class AnnotatedText(val slice: List<Annotated>, val maxLine: Int? = null) : UIRichElement

    data class Quote(val content: List<UIRichElement>) : UIRichElement

    data class Image(val imageUrl: String, val jumpUrl: String?) : UIRichElement
}

fun RichText.toUIRichElements(): List<UIRichElement> = buildList {
    val annotated = mutableListOf<UIRichElement.Annotated>()

    elements.forEach { e ->
        when (e) {
            is RichElement.Text -> {
                if (e.value.trim().isNotEmpty()) {
                    annotated.add(
                        UIRichElement.Annotated.Text(
                            content = e.value,
                            size = e.size,
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