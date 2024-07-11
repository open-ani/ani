package me.him188.ani.utils.bbcode

import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.assertIs

abstract class BBCodeParserTestHelper {
    inline fun <reified T> List<RichElement>.at(index: Int): T {
        val value = this[index]
        assertIs<T>(value)
        return value
    }

    fun assertText(
        element: RichElement.Text,
        jumpUrl: String? = null,
        value: String,
        size: Int = RichElement.Text.DEFAULT_SIZE,
        color: String? = null,
        italic: Boolean = false,
        underline: Boolean = false,
        strikethrough: Boolean = false,
        bold: Boolean = false,
        mask: Boolean = false,
        code: Boolean = false,
    ) {
        assertElement(jumpUrl, element)
        assertEquals(value, element.value)
        assertEquals(size, element.size)
        assertEquals(color, element.color)
        assertEquals(italic, element.italic)
        assertEquals(underline, element.underline)
        assertEquals(strikethrough, element.strikethrough)
        assertEquals(bold, element.bold)
        assertEquals(mask, element.mask)
        assertEquals(code, element.code)
    }

    fun assertImage(
        element: RichElement.Image,
        jumpUrl: String? = null,
        imageUrl: String,
    ) {
        assertElement(jumpUrl, element)
        assertEquals(imageUrl, element.imageUrl)
    }

    class QuoteContext(
        val elements: List<RichElement>,
    )

    inline fun assertQuote(
        element: RichElement.Quote,
        jumpUrl: String? = null,
        block: QuoteContext.() -> Unit,
    ) {
        assertElement(jumpUrl, element)
        block(QuoteContext(element.contents))
    }

    fun assertBangumiSticker(
        element: RichElement.BangumiSticker,
        jumpUrl: String? = null,
        id: Int,
    ) {
        assertElement(jumpUrl, element)
        assertEquals(id, element.id)
    }

    fun assertKanmoji(
        element: RichElement.Kanmoji,
        jumpUrl: String? = null,
        id: String,
    ) {
        assertElement(jumpUrl, element)
        assertEquals(id, element.id)
    }

    fun assertElement(
        jumpUrl: String?,
        element: RichElement
    ) {
        assertEquals(jumpUrl, element.jumpUrl)
    }
}
