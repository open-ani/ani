package me.him188.ani.app.data.media

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.shareIn
import me.him188.ani.app.data.media.MediaCacheManager.Companion.LOCAL_FS_MEDIA_SOURCE_ID
import me.him188.ani.app.data.models.MediaSourceProxyPreferences
import me.him188.ani.app.data.models.ProxyAuthorization
import me.him188.ani.app.data.repositories.PreferencesRepository
import me.him188.ani.app.platform.getAniUserAgent
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.utils.ktor.ClientProxyConfig
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.ServiceLoader

interface MediaSourceManager { // available by inject
    /**
     * 跟随设置更新的 [MediaSource] 列表. 已考虑代理和开关.
     */
    val enabledSources: SharedFlow<List<MediaSource>>

    /**
     * 全部的 [MediaSource], 包括那些设置里关闭的, 包括本地的.
     */
    val allIds: List<String>

    /**
     * 全部的 [MediaSource], 包括那些设置里关闭的, 但不包括本地的.
     */
    val allIdsExceptLocal: List<String>

    fun isLocal(mediaSourceId: String): Boolean {
        return mediaSourceId == LOCAL_FS_MEDIA_SOURCE_ID
    }
}

class MediaSourceManagerImpl(
    additionalSources: () -> List<MediaSource>, // local sources
) : MediaSourceManager, KoinComponent {
    private val preferencesRepository: PreferencesRepository by inject()

    private val defaultMediaPreference = preferencesRepository.defaultMediaPreference.flow
    private val proxyConfig = preferencesRepository.proxyPreferences.flow


    private val scope = CoroutineScope(CoroutineExceptionHandler { _, throwable ->
        // log error
        logger.error(throwable) { "DownloadProviderManager scope error" }
    })
    private val factories = ServiceLoader.load(MediaSourceFactory::class.java).toList()

    private val additionalSources by lazy { additionalSources() }
    override val enabledSources =
        combine(proxyConfig, defaultMediaPreference) { proxyPreferences, defaultMediaPreference ->
            // 一定要 additionalSources 在前面, local sources 需要优先使用
            val enabledSources = defaultMediaPreference.fallbackMediaSourceIds
            this.additionalSources
                .filter { enabledSources == null || it.mediaSourceId in enabledSources }
                .plus(
                    factories
                        .filter { enabledSources == null || it.mediaSourceId in enabledSources }
                        .map { factory ->
                            factory.create(proxyPreferences.get(factory.mediaSourceId))
                        }
                )
        }.shareIn(scope, replay = 1, started = SharingStarted.Lazily)

    override val allIds: List<String> by lazy {
        factories
            .map { it.mediaSourceId }
            .plus(this.additionalSources.map { it.mediaSourceId })
    }
    override val allIdsExceptLocal: List<String> by lazy {
        allIds.filter { !isLocal(it) }
    }

    private fun MediaSourceFactory.create(pref: MediaSourceProxyPreferences): MediaSource {
        return create(
            MediaSourceConfig(
                proxy = pref.toClientProxyConfig(),
                userAgent = getAniUserAgent(),
            )
        )
    }

    private companion object {
        private val logger = logger<MediaSourceManager>()
    }
}

private fun MediaSourceProxyPreferences.toClientProxyConfig(): ClientProxyConfig? {
    return if (enabled) {
        config.run {
            ClientProxyConfig(
                url = url,
                authorization = authorization?.toHeader(),
            )
        }
    } else {
        null
    }
}

fun ProxyAuthorization.toHeader(): String = "Basic ${"$username:$password"}"
