package me.him188.ani.app.data.source

object BangumiCommentSticker {
    infix operator fun get(id: Int): String? {
        return resolveStickerAsset(id)
    }

    private fun resolveStickerAsset(id: Int): String {
        return if (id < 24) {
            val type = if (id == 11 || id == 23) "gif" else "png"
            "stickers/bgm/${id.toString().padStart(2, '0')}.${type}"
        } else {
            "stickers/tv/${(id - 23).toString().padStart(2, '0')}.gif"
        }
    }
}