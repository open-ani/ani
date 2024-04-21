package me.him188.ani.danmaku.ani.client

import me.him188.ani.danmaku.api.AbstractDanmakuProvider
import me.him188.ani.danmaku.api.DanmakuProvider
import me.him188.ani.danmaku.api.DanmakuProviderConfig
import me.him188.ani.danmaku.api.DanmakuProviderFactory
import me.him188.ani.danmaku.api.DanmakuSearchRequest
import me.him188.ani.danmaku.api.DanmakuSession


class AniDanmakuProvider(
    config: DanmakuProviderConfig,
) : AbstractDanmakuProvider(config) {
    companion object {
        const val ID = "ani"
    }

    class Factory : DanmakuProviderFactory {
        override val id: String get() = ID

        override fun create(config: DanmakuProviderConfig): DanmakuProvider =
            AniDanmakuProvider(config)
    }

    override val id: String get() = ID

    override suspend fun fetch(request: DanmakuSearchRequest): DanmakuSession? = null
}
