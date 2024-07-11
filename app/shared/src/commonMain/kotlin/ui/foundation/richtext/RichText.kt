package me.him188.ani.app.ui.foundation.richtext

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.ClickableText
import org.jetbrains.compose.resources.painterResource

@Composable
fun RichText(
    elements: List<UIRichElement>,
    modifier: Modifier
) {
    if (elements.isEmpty()) return

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
    ) {
        elements.toLayout { }
    }
}

@Composable
fun List<UIRichElement>.toLayout(
    onClickUrl: (String) -> Unit
) = forEach { e ->
    when (e) {
        is UIRichElement.AnnotatedText -> RichTextDefaults.AnnotatedText(
            slice = e.slice,
            modifier = Modifier,
            onClick = { it.url?.let(onClickUrl) },
        )

        is UIRichElement.Image -> RichTextDefaults.Image(
            element = e,
            modifier = Modifier,
            onClick = { e.jumpUrl?.let(onClickUrl) },
        )

        is UIRichElement.Quote -> {

        }
    }
}

@Stable
object RichTextDefaults {
    val StickerSize: Int = 24

    @Composable
    fun AnnotatedText(
        slice: List<UIRichElement.Annotated>,
        modifier: Modifier = Modifier,
        onClick: (UIRichElement.Annotated) -> Unit
    ) {
        val context = LocalPlatformContext.current
        val inlineStickerMap: MutableMap<String, InlineTextContent> = remember { mutableStateMapOf() }
        val stickerSizeSp = with(LocalDensity.current) { StickerSize.dp.toSp() }
        val bodyLarge = MaterialTheme.typography.bodyLarge.fontSize.value

        val currentOnClick by rememberUpdatedState(onClick)
        val contentColor = LocalContentColor.current

        val content = buildAnnotatedString {
            var currentLength = 0

            slice.forEachIndexed { index, e ->
                val elementLength: Int

                when (e) {
                    is UIRichElement.Annotated.Text -> {
                        elementLength = e.content.length
                        append(e.content)

                        addStyle(
                            style = SpanStyle(
                                color = contentColor,
                                fontSize = if (e.size.toFloat() != bodyLarge) e.size.sp else 15.5.sp,
                                fontWeight = if (e.bold) FontWeight.Bold else null,
                                fontStyle = if (e.italic) FontStyle.Italic else null,
                                textDecoration = if (!e.underline && !e.strikethrough) null
                                else TextDecoration.combine(
                                    buildList {
                                        if (e.underline) add(TextDecoration.Underline)
                                        if (e.strikethrough) add(TextDecoration.LineThrough)
                                    },
                                ),
                                background = if (e.code) MaterialTheme.colorScheme.surfaceContainer else Color.Unspecified,
                                fontFamily = if (e.code) FontFamily.Monospace else null,
                            ),
                            start = currentLength,
                            end = currentLength + elementLength,
                        )
                    }

                    is UIRichElement.Annotated.Sticker -> {
                        val inlineContentId by remember { mutableStateOf(e.id) }
                        val correspondingText = inlineContentId
                        elementLength = correspondingText.length

                        appendInlineContent(inlineContentId, correspondingText)
                        inlineStickerMap[inlineContentId] = InlineTextContent(
                            placeholder = Placeholder(
                                stickerSizeSp,
                                stickerSizeSp,
                                PlaceholderVerticalAlign.AboveBaseline,
                            ),
                            children = { id ->
                                val sticker = slice
                                    .asSequence()
                                    .filterIsInstance<UIRichElement.Annotated.Sticker>()
                                    .find { it.id == id }

                                if (sticker?.resource != null) {
                                    androidx.compose.foundation.Image(
                                        painter = painterResource(sticker.resource),
                                        contentDescription = null,
                                        modifier = Modifier.size(StickerSize.dp),
                                    )
                                }
                            },
                        )
                    }
                }

                if (e.url != null) {
                    addStringAnnotation(
                        tag = "url",
                        annotation = index.toString(),
                        start = currentLength,
                        end = currentLength + elementLength,
                    )
                    if (e is UIRichElement.Annotated.Text) {
                        addStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline,
                            ),
                            start = currentLength,
                            end = currentLength + elementLength,
                        )
                    }
                }
                currentLength += elementLength
            }
        }

        ClickableText(
            text = content,
            modifier = modifier,
            inlineContent = inlineStickerMap,
            onClick = {
                val urlAnnotations = content.getStringAnnotations("url", it, it).firstOrNull()
                if (urlAnnotations != null) {
                    val index = urlAnnotations.item.toIntOrNull() ?: return@ClickableText
                    slice.getOrNull(index)?.let(currentOnClick)
                }
            },
        )
    }

    @Composable
    fun Image(
        element: UIRichElement.Image,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
    ) {
        val context = LocalPlatformContext.current
        val currentOnClick by rememberUpdatedState(onClick)
        var state by remember { mutableStateOf(0) } // 0: loading, 1: success, 2: failed

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(element.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = modifier
                .padding(8.dp)
                .animateContentSize()
                .run { if (state == 0) size(64.dp) else this }
                .placeholder(state == 0)
                .clip(RoundedCornerShape(8.dp))
                .then(Modifier.clickable { currentOnClick() }),
            contentScale = ContentScale.Fit,
            onLoading = { state = 1 },
            onSuccess = { state = 2 },
            onError = { state = 3 },
        )
    }
}