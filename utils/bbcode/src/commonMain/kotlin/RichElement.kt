package me.him188.ani.utils.bbcode


sealed interface RichElement {
    /**
     * 该元素的可点击跳转链接，若为 null 则表示不可跳转
     */
    val jumpUrl: String?

    data class Text(
        val value: String,
        val size: Int = DEFAULT_SIZE,
        /**
         * Can be:
         * - RGBA: "#FFFFFFFF"
         * - RGB: "#FFFFFF"
         * - name: "red"
         *
         * 如果无法解析则显示为默认颜色
         */
        val color: String? = null,

        val italic: Boolean = false,
        val underline: Boolean = false,
        val strikethrough: Boolean = false,
        val bold: Boolean = false,

        val mask: Boolean = false,
        val code: Boolean = false,

        override val jumpUrl: String? = null
    ) : RichElement {
        companion object {
            const val DEFAULT_SIZE = 16
        }
    }

    data class Image(
        val imageUrl: String,
        override val jumpUrl: String? = null
    ) : RichElement

    data class Quote(
        val contents: RichText,
        override val jumpUrl: String? = null
    ) : RichElement

    data class BangumiSticker(
        val id: Int,
        override val jumpUrl: String? = null
    ) : RichElement

    data class Kanmoji(
        val id: String, // "(=A=)", 详情见文法
        override val jumpUrl: String? = null
    ) : RichElement
}