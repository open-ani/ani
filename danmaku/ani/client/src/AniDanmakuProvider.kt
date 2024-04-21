package me.him188.ani.danmaku.ani.client

import io.ktor.client.call.body
import io.ktor.client.request.get
import me.him188.ani.danmaku.api.AbstractDanmakuProvider
import me.him188.ani.danmaku.api.DanmakuProvider
import me.him188.ani.danmaku.api.DanmakuProviderConfig
import me.him188.ani.danmaku.api.DanmakuProviderFactory
import me.him188.ani.danmaku.api.DanmakuSearchRequest
import me.him188.ani.danmaku.api.DanmakuSession
import me.him188.ani.danmaku.api.TimeBasedDanmakuSession
import me.him188.ani.danmaku.protocol.DanmakuGetResponse
import me.him188.ani.danmaku.api.Danmaku as ApiDanmaku
import me.him188.ani.danmaku.api.DanmakuLocation as ApiDanmakuLocation
import me.him188.ani.danmaku.protocol.DanmakuLocation as ProtocolDanmakuLocation

object AniBangumiSeverBaseUrls {
    const val GLOBAL = "https://danmaku-global.myani.org"
    const val CN = "https://danmaku-cn.myani.org"

    val list = listOf(GLOBAL, CN)

    fun getBaseUrl(useGlobal: Boolean) = if (useGlobal) GLOBAL else CN
}

class AniDanmakuProvider(
    private val config: DanmakuProviderConfig,
) : AbstractDanmakuProvider(config) {
    private fun getBaseUrl() = AniBangumiSeverBaseUrls.getBaseUrl(config.useGlobal)

    companion object {
        const val ID = "ani"
    }

    class Factory : DanmakuProviderFactory {
        override val id: String get() = ID

        override fun create(config: DanmakuProviderConfig): DanmakuProvider =
            AniDanmakuProvider(config)
    }

    override val id: String get() = ID

    override suspend fun fetch(request: DanmakuSearchRequest): DanmakuSession {
        val resp = client.get("${getBaseUrl()}/v1/danmaku/${request.episodeId}")
        val list = resp.body<DanmakuGetResponse>().danmakuList
        return TimeBasedDanmakuSession.create(list.asSequence().map {
            ApiDanmaku(
                id = it.id,
                providerId = ID,
                playTimeMillis = it.danmakuInfo.playTime,
                senderId = it.senderId,
                location = it.danmakuInfo.location.toApi(),
                text = it.danmakuInfo.text,
                color = it.danmakuInfo.color,
            )
        })
    }
}

fun ProtocolDanmakuLocation.toApi(): ApiDanmakuLocation = when (this) {
    ProtocolDanmakuLocation.TOP -> ApiDanmakuLocation.TOP
    ProtocolDanmakuLocation.BOTTOM -> ApiDanmakuLocation.BOTTOM
    ProtocolDanmakuLocation.NORMAL -> ApiDanmakuLocation.NORMAL
}
