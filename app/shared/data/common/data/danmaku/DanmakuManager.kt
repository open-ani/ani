package me.him188.ani.app.data.danmaku

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import me.him188.ani.app.data.repositories.PreferencesRepository
import me.him188.ani.app.platform.getAniUserAgent
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.danmaku.ani.client.AniDanmakuProvider
import me.him188.ani.danmaku.ani.client.AniDanmakuSender
import me.him188.ani.danmaku.ani.client.AniDanmakuSenderImpl
import me.him188.ani.danmaku.ani.client.SendDanmakuException
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuProvider
import me.him188.ani.danmaku.api.DanmakuProviderConfig
import me.him188.ani.danmaku.api.DanmakuSearchRequest
import me.him188.ani.danmaku.api.DanmakuSession
import me.him188.ani.danmaku.api.emptyDanmakuSession
import me.him188.ani.danmaku.dandanplay.DandanplayDanmakuProvider
import me.him188.ani.danmaku.protocol.DanmakuInfo
import me.him188.ani.utils.coroutines.mapAutoClose
import me.him188.ani.utils.coroutines.mapAutoCloseCollection
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

/**
 * 管理多个弹幕源 [DanmakuProvider]
 */
interface DanmakuManager {
    val selfId: Flow<String?>

    suspend fun fetch(
        request: DanmakuSearchRequest,
    ): DanmakuSession

    @Throws(SendDanmakuException::class)
    suspend fun post(episodeId: Int, danmaku: DanmakuInfo): Danmaku
}

object DanmakuProviderLoader {
    fun load(
        config: (id: String) -> DanmakuProviderConfig,
    ): List<DanmakuProvider> {
        // 待 https://youtrack.jetbrains.com/issue/KT-65362/Cannot-resolve-declarations-from-a-dependency-when-there-are-multiple-JVM-only-project-dependencies-in-a-JVM-Android-MPP
        // 解决后, 才能切换使用 ServiceLoader, 否则 resources META-INF/services 会冲突
//        val factories = ServiceLoader.load(DanmakuProviderFactory::class.java).toList()
        val factories = listOf(
            DandanplayDanmakuProvider.Factory(),
            AniDanmakuProvider.Factory()
        )
        return factories
            .map { factory -> factory.create(config(factory.id)) }
    }
}

class DanmakuManagerImpl(
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : DanmakuManager, KoinComponent, HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    private val preferencesRepository: PreferencesRepository by inject()
    private val sessionManager: SessionManager by inject()

    private val config = preferencesRepository.danmakuSettings.flow.map { config ->
        DanmakuProviderConfig(
            userAgent = getAniUserAgent(),
            useGlobal = config.useGlobal,
        )
    }

    /**
     * @see DanmakuProviderLoader
     */
    private val providers: Flow<List<DanmakuProvider>> = config.mapAutoCloseCollection { config ->
        DanmakuProviderLoader.load { config }
    }.shareInBackground(started = SharingStarted.Lazily)

    private val sender: Flow<AniDanmakuSender> = config.mapAutoClose { config ->
        AniDanmakuSenderImpl(
            config,
            sessionManager.session.map { it?.accessToken },
            backgroundScope.coroutineContext
        )
    }.shareInBackground(started = SharingStarted.Lazily)

    private companion object {
        val logger = logger<DanmakuManagerImpl>()
    }

    override val selfId: Flow<String?> = sender.flatMapLatest { it.selfId }

    override suspend fun fetch(
        request: DanmakuSearchRequest,
    ): CombinedDanmakuSession {
        return CombinedDanmakuSession(
            providers.first().map { provider ->
                runCatching {
                    provider.fetch(request = request)
                }.onFailure {
                    logger.error(it) { "Failed to fetch danmaku from provider '${provider.id}'" }
                }.getOrNull() ?: emptyDanmakuSession()
            }
        )
    }

    override suspend fun post(episodeId: Int, danmaku: DanmakuInfo): Danmaku {
        return sender.first().send(episodeId, danmaku)
    }
}

class CombinedDanmakuSession(
    private val sessions: List<DanmakuSession>,
) : DanmakuSession {
    override val totalCount: Int?
        get() = if (sessions.all { it.totalCount == null }) null
        else sessions.sumOf { it.totalCount ?: 0 }

    override fun at(progress: Flow<Duration>): Flow<Danmaku> {
        return sessions.map { it.at(progress) }.merge()
    }
}