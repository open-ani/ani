package me.him188.ani.app.data.media

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
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
     * List of download providers configured using user preferences.
     * Automatically reloaded when preferences change.
     */
    val sources: SharedFlow<List<MediaSource>>
    val ids: List<String>
}

class MediaSourceManagerImpl(
    additionalSources: () -> List<MediaSource>, // local sources
) : MediaSourceManager, KoinComponent {
    private val preferencesRepository: PreferencesRepository by inject()
    private val config = preferencesRepository.proxyPreferences.flow


    private val scope = CoroutineScope(CoroutineExceptionHandler { _, throwable ->
        // log error
        logger.error(throwable) { "DownloadProviderManager scope error" }
    })
    private val factories = ServiceLoader.load(MediaSourceFactory::class.java).toList()

    private val additionalSources by lazy { additionalSources() }
    override val sources = config.map { proxyPreferences ->
        // 一定要 additionalSources 在前面, local sources 需要优先使用
        this.additionalSources + factories
            .map { factory ->
                factory.create(proxyPreferences.get(factory.id))
            }
    }.shareIn(scope, replay = 1, started = SharingStarted.Lazily)

    override val ids: List<String> = factories
        .map { it.id }
        .toList()

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
