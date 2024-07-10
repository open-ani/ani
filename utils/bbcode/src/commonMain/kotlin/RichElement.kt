package me.him188.ani.utils.bbcode


sealed interface RichElement {
    /**
     * 该元素的可点击跳转链接，若为 null 则表示不可跳转
     */
    val jumpUrl: String?

    data class Text(
        val value: String,
        val size: Int,

        val italic: Boolean,
        val underline: Boolean,
        val removeline: Boolean,
        val bold: Boolean,

        val mask: Boolean,

        override val jumpUrl: String? = null
    ) : RichElement

    data class Image(
        val imageUrl: String,
        override val jumpUrl: String? = null
    ) : RichElement

    data class Quote(
        val text: Text,
        override val jumpUrl: String? = null
    ) : RichElement

    data class Face(
        val id: Int,
        override val jumpUrl: String? = null
    ) : RichElement
}