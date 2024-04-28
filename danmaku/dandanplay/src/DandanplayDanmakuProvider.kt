package me.him188.ani.danmaku.dandanplay

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import me.him188.ani.danmaku.api.AbstractDanmakuProvider
import me.him188.ani.danmaku.api.DanmakuEpisode
import me.him188.ani.danmaku.api.DanmakuMatchers
import me.him188.ani.danmaku.api.DanmakuProviderConfig
import me.him188.ani.danmaku.api.DanmakuProviderFactory
import me.him188.ani.danmaku.api.DanmakuSearchRequest
import me.him188.ani.danmaku.api.DanmakuSession
import me.him188.ani.danmaku.api.TimeBasedDanmakuSession
import me.him188.ani.danmaku.dandanplay.data.toDanmakuOrNull
import me.him188.ani.utils.logging.info

class DandanplayDanmakuProvider(
    config: DanmakuProviderConfig,
) : AbstractDanmakuProvider(config) {
    companion object {
        const val ID = "弹弹play"
    }

    class Factory : DanmakuProviderFactory {
        override val id: String get() = ID

        override fun create(config: DanmakuProviderConfig): DandanplayDanmakuProvider =
            DandanplayDanmakuProvider(config)
    }

    override val id: String get() = ID

    private val dandanplayClient = DandanplayClient(client)

    override fun HttpClientConfig<*>.configureClient() {
        install(HttpRequestRetry) {
            maxRetries = 1
            delayMillis { 2000 }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000 // 弹弹服务器请求比较慢
            connectTimeoutMillis = 10_000 // 弹弹服务器请求比较慢
        }
    }

    override suspend fun fetch(
        request: DanmakuSearchRequest,
    ): DanmakuSession? {
        val searchEpisodeResponse = dandanplayClient.searchEpisode(
            subjectName = request.subjectName.trim().substringBeforeLast(" "),
            episodeName = null // 用我们的匹配算法
//            episodeName = "第${(request.episodeEp ?: request.episodeSort).toString().removePrefix("0")}话",
            // 弹弹的是 EP 顺序
            // 弹弹数据库有时候会只有 "第x话" 没有具体标题, 所以不带标题搜索就够了
        )
        logger.info { "Ep search result: ${searchEpisodeResponse}}" }
        val episodes = searchEpisodeResponse.animes.flatMap { it ->
            it.episodes.map { ep ->
                DanmakuEpisode(
                    id = ep.episodeId.toString(),
                    subjectName = it.animeTitle ?: "",
                    episodeName = ep.episodeTitle ?: "",
                )
            }
        }

        val matcher = DanmakuMatchers.mostRelevant(
            request.subjectName,
            "第${(request.episodeEp ?: request.episodeSort).toString().removePrefix("0")}话 " + request.episodeName
        )

        if (episodes.isNotEmpty()) {
            matcher.match(episodes)?.let {
                logger.info { "Matched episode by ep search: ${it.subjectName} - ${it.episodeName}" }
                return createSession(it.id.toLong(), 0)
            }
        }

        val resp = dandanplayClient.matchVideo(
            filename = request.filename,
            fileHash = request.fileHash,
            fileSize = request.fileSize,
            videoDuration = request.videoDuration
        )
        val match = if (resp.isMatched) {
            resp.matches.firstOrNull() ?: return null
        } else {
            matcher.match(resp.matches.map {
                DanmakuEpisode(
                    it.episodeId.toString(),
                    it.animeTitle, it.episodeTitle,
                )
            })?.let { match ->
                resp.matches.first { it.episodeId.toString() == match.id }
            } ?: return null
        }
        logger.info { "Best match by file match: ${match.animeTitle} - ${match.episodeTitle}" }
        val episodeId = match.episodeId
        return createSession(episodeId, (match.shift * 1000L).toLong())
    }

    private suspend fun createSession(
        episodeId: Long,
        shiftMillis: Long
    ): DanmakuSession {
        val list = dandanplayClient.getDanmakuList(episodeId = episodeId)
        logger.info { "$ID Fetched danmaku list: ${list.size}" }
        return TimeBasedDanmakuSession.create(
            list.asSequence().mapNotNull { it.toDanmakuOrNull() },
            shiftMillis = shiftMillis,
        )
    }
}
