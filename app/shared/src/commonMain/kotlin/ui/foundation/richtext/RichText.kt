package me.him188.ani.app.ui.foundation.richtext

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.ClickableText
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.utils.logging.logger
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
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
    private val logger = logger<RichTextDefaults>()

    private class AnnotatedTextViewModel : AbstractViewModel() {
        private val _maskConnection: MutableStateFlow<Map<Int, Int>> = MutableStateFlow(mapOf())
        val maskConnection: StateFlow<Map<Int, Int>> get() = _maskConnection

        private val _maskState: MutableStateFlow<Map<Int, Boolean>> = MutableStateFlow(mapOf())
        val maskState: StateFlow<Map<Int, Boolean>> get() = _maskState

        fun parseMask(slice: List<UIRichElement.Annotated>) {
            var currentMaskIndex = 0

            val state = mutableMapOf<Int, Boolean>()
            val connection = mutableMapOf<Int, Int>()

            slice.forEachIndexed { index, e ->
                val maskIndex = currentMaskIndex
                if (e is UIRichElement.Annotated.Text && e.mask) {
                    if (slice.getOrNull(index - 1).let {
                            !(it is UIRichElement.Annotated.Text && it.mask)
                        }) {
                        // 上一个 text 没有遮罩，增加遮罩块索引
                        // 如果上一个 text 也有遮罩，则与上一个遮罩状态合并
                        currentMaskIndex += 1
                    }
                    state[maskIndex] = true
                    connection[index] = maskIndex
                }
            }

            _maskConnection.value = connection
            _maskState.value = state
        }

        fun updateMaskConnection(block: MutableMap<Int, Int>.() -> Unit) {
            _maskConnection.value = mutableMapOf<Int, Int>()
                .apply { putAll(_maskConnection.value) }
                .also(block)
        }

        fun updateMaskState(block: MutableMap<Int, Boolean>.() -> Unit) {
            _maskState.value = mutableMapOf<Int, Boolean>()
                .apply { putAll(_maskState.value) }
                .also(block)
        }
    }
    
    @Composable
    fun AnnotatedText(
        slice: List<UIRichElement.Annotated>,
        modifier: Modifier = Modifier,
        onClick: (UIRichElement.Annotated) -> Unit
    ) {
        val inlineStickerMap: MutableMap<String, InlineTextContent> = remember { mutableStateMapOf() }
        val stickerSizeSp = with(LocalDensity.current) { StickerSize.dp.toSp() }
        val bodyLarge = MaterialTheme.typography.bodyLarge.fontSize.value
        val colorScheme = MaterialTheme.colorScheme

        val currentOnClick by rememberUpdatedState(onClick)
        val contentColor = LocalContentColor.current

        val vm = rememberViewModel(keys = listOf(slice)) { AnnotatedTextViewModel() }

        val maskConnection by vm.maskConnection.collectAsStateWithLifecycle()
        val maskState by vm.maskState.collectAsStateWithLifecycle()

        LaunchedEffect(slice) {
            vm.parseMask(slice)
        }
        
        val content = buildAnnotatedString {
            var currentLength = 0

            slice.forEachIndexed { index, e ->
                val elementLength: Int

                when (e) {
                    is UIRichElement.Annotated.Text -> {
                        elementLength = e.content.length
                        append(e.content)

                        val maskIndex = maskConnection[index]
                        if (e.mask) {
                            // 为这个 text 片段添加遮罩 annotation，用于处理点击遮罩的事件
                            addStringAnnotation(
                                tag = "mask",
                                annotation = index.toString(),
                                start = currentLength,
                                end = currentLength + elementLength,
                            )
                        }

                        val background by animateColorAsState(
                            if (maskState[maskIndex] == true) {
                                colorScheme.secondaryContainer
                            } else {
                                if (e.code) colorScheme.surfaceContainer else Color.Unspecified
                            },
                        )
                        val textColor by animateColorAsState(
                            if (maskState[maskIndex] == true) colorScheme.secondaryContainer else contentColor,
                        )

                        addStyle(
                            style = SpanStyle(
                                color = textColor,
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
                                background = background,
                                fontFamily = if (e.code) FontFamily.Monospace else null,
                            ),
                            start = currentLength,
                            end = currentLength + elementLength,
                        )
                    }

                    is UIRichElement.Annotated.Sticker -> {
                        val inlineContentId = e.id
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
                                color = colorScheme.primary,
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
            onClick = { textPos ->
                val annotations = content.getStringAnnotations(textPos, textPos)

                // 先检查是不是 mask
                val maskAnno = annotations.firstOrNull { it.tag == "mask" }
                if (maskAnno != null) {
                    logger.info("mask anno: $maskAnno")
                    logger.info("mask connection: ${maskConnection.toMap().entries.joinToString(", ")}")
                    logger.info("mask state: ${maskState.toMap().entries.joinToString(", ")}")
                    // 若 annotation item 不是 slice index，视作无效的 annotation，没必要继续梳理
                    val sliceIndex = maskAnno.item.toIntOrNull() ?: return@ClickableText
                    val maskIndex = maskConnection[sliceIndex]

                    // 去掉 mask，不继续处理
                    // 例如 mask 了一个 url，第一次点击去掉 mask，第二次跳转
                    if (maskIndex != null && maskState[maskIndex] == true) {
                        vm.updateMaskState { set(maskIndex, false) }
                        return@ClickableText
                    }
                }

                // 检查有没有 url 跳转
                val urlAnno = annotations.firstOrNull { it.tag == "url" }
                if (urlAnno != null) {
                    val sliceIndex = urlAnno.item.toIntOrNull() ?: return@ClickableText
                    slice.getOrNull(sliceIndex)?.let(currentOnClick)
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