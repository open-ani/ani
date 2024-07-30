package me.him188.ani.danmaku.api

object DanmakuSanitizer {
    fun sanitize(danmaku: Danmaku): Danmaku = danmaku.run {
        if (text.indexOf("\n") == -1) return@run this

        copy(
            text = text
                .replace("\n\r", " ")
                .replace("\r\n", " ")
                .replace("\n", " ")
                .trim(),
        )
    }
}
