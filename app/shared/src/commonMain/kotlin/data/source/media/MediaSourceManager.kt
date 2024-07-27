package me.him188.ani.app.data.source.media

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import me.him188.ani.app.data.models.preference.MediaSourceProxySettings
import me.him188.ani.app.data.models.preference.ProxyAuthorization
import me.him188.ani.app.data.models.preference.ProxyConfig
import me.him188.ani.app.data.repository.MediaSourceInstanceRepository
import me.him188.ani.app.data.repository.MikanIndexCacheRepository
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.data.repository.updateConfig
import me.him188.ani.app.data.source.media.MediaCacheManager.Companion.LOCAL_FS_MEDIA_SOURCE_ID
import me.him188.ani.app.data.source.media.fetch.MediaFetchSession
import me.him188.ani.app.data.source.media.fetch.MediaFetcher
import me.him188.ani.app.data.source.media.fetch.MediaFetcherConfig
import me.him188.ani.app.data.source.media.fetch.MediaSourceMediaFetcher
import me.him188.ani.app.data.source.media.fetch.create
import me.him188.ani.app.data.source.media.instance.MediaSourceInstance
import me.him188.ani.app.data.source.media.instance.MediaSourceSave
import me.him188.ani.app.platform.getAniUserAgent
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.mikan.MikanCNMediaSource
import me.him188.ani.datasources.mikan.MikanMediaSource
import me.him188.ani.utils.coroutines.onReplacement
import me.him188.ani.utils.ktor.ClientProxyConfig
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.platform.Uuid
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.ServiceLoader
import kotlin.coroutines.CoroutineContext

interface MediaSourceManager { // available by inject
    /**
     * 全部 [MediaSourceInstance] 列表.
     */
    val allInstances: Flow<List<MediaSourceInstance>>

    /**
     * 全部的 [MediaSource], 包括那些设置里关闭的, 包括本地的.
     */
    val allFactories: List<MediaSourceFactory>

    /**
     * 全部的 [MediaSource], 包括那些设置里关闭的, 包括本地的.
     */
    val allFactoryIds: List<String>

    /**
     * 全部的 [MediaSource], 包括那些设置里关闭的, 但不包括本地的.
     */
    val allFactoryIdsExceptLocal: List<String>
        get() = allFactoryIds.filter { !isLocal(it) }

    /**
     * 根据启用的 [MediaSourceInstance] 创建的 [MediaSourceMediaFetcher].
     */
    val mediaFetcher: Flow<MediaFetcher>

    fun isLocal(mediaSourceId: String): Boolean {
        return mediaSourceId == LOCAL_FS_MEDIA_SOURCE_ID
    }

    fun instanceConfigFlow(instanceId: String): Flow<MediaSourceConfig>

    suspend fun addInstance(mediaSourceId: String, config: MediaSourceConfig)
    suspend fun updateConfig(instanceId: String, config: MediaSourceConfig)
    suspend fun setEnabled(instanceId: String, enabled: Boolean)
    suspend fun removeInstance(instanceId: String)
}

/**
 * 根据请求创建 [MediaFetchSession].
 * 也会跟随用户设置的启用/禁用数据源 emit 新的 [MediaFetchSession].
 *
 * @param requestLazy 相当于 [lazy]. 只有第一个元素会被使用. 必须至少 emit 一个元素.
 *
 * @see MediaFetchRequest.Companion.create
 */
fun MediaSourceManager.createFetchFetchSessionFlow(requestLazy: Flow<MediaFetchRequest>): Flow<MediaFetchSession> =
    this.mediaFetcher.map { it.newSession(requestLazy) }

class MediaSourceManagerImpl(
    additionalSources: () -> List<MediaSource>, // local sources, calculated only once
    flowCoroutineContext: CoroutineContext = Dispatchers.Default,
) : MediaSourceManager, KoinComponent {
    private val settingsRepository: SettingsRepository by inject()
    private val proxyConfig = settingsRepository.proxySettings.flow
    private val mikanIndexCacheRepository: MikanIndexCacheRepository by inject()
    private val instances: MediaSourceInstanceRepository by inject()

    private val scope = CoroutineScope(
        CoroutineExceptionHandler { _, throwable ->
            // log error
            logger.error(throwable) { "DownloadProviderManager scope error" }
        },
    )
    private val factories: List<MediaSourceFactory> = ServiceLoader.load(MediaSourceFactory::class.java).toList()

    private val additionalSources by lazy {
        additionalSources().map { source ->
            MediaSourceInstance(
                source.mediaSourceId,
                source.mediaSourceId,
                true,
                MediaSourceConfig.Default,
                source,
            )
        }
    }
    override val allInstances =
        combine(instances.flow, proxyConfig.map { it.default }.distinctUntilChanged()) { saves, config ->
            // 一定要 additionalSources 在前面, local sources 需要优先使用
            this.additionalSources + saves.mapNotNull { createInstance(it, config) }
        }.onReplacement { list ->
            list.forEach { it.close() }
        }.flowOn(flowCoroutineContext).shareIn(scope, replay = 1, started = SharingStarted.Lazily)
    override val allFactories: List<MediaSourceFactory> get() = factories

    private fun createInstance(save: MediaSourceSave, config: MediaSourceProxySettings): MediaSourceInstance? {
        val factory = factories.find { it.mediaSourceId == save.mediaSourceId }
        return if (factory == null) {
            logger.error { "MediaSourceFactory not found for ${save.mediaSourceId}" }
            null
        } else {
            MediaSourceInstance(
                save.instanceId,
                save.mediaSourceId,
                save.isEnabled,
                save.config,
                factory.create(config, save.config),
            )
        }
    }

    override val allFactoryIds: List<String> by lazy {
        factories
            .map { it.mediaSourceId }
            .plus(this.additionalSources.map { it.mediaSourceId })
    }

    override val mediaFetcher: Flow<MediaFetcher> = allInstances.map { instances ->
        MediaSourceMediaFetcher(
            configProvider = { MediaFetcherConfig.Default },
            mediaSources = instances,
        )
    }

    override fun instanceConfigFlow(instanceId: String): Flow<MediaSourceConfig> {
        return instances.flow.map { list ->
            list.find { it.instanceId == instanceId }?.config ?: MediaSourceConfig.Default
        }
    }

    override suspend fun addInstance(mediaSourceId: String, config: MediaSourceConfig) {
        val save = MediaSourceSave(
            instanceId = Uuid.randomString(),
            mediaSourceId = mediaSourceId,
            isEnabled = true,
            config = config,
        )
        instances.add(save)
    }

    override suspend fun updateConfig(instanceId: String, config: MediaSourceConfig) {
        instances.updateConfig(instanceId, config)
    }

    override suspend fun setEnabled(instanceId: String, enabled: Boolean) {
        instances.updateSave(instanceId) {
            copy(isEnabled = enabled)
        }
    }

    override suspend fun removeInstance(instanceId: String) {
        instances.remove(instanceId)
    }

    private fun MediaSourceFactory.create(
        globalProxySettings: MediaSourceProxySettings,
        config: MediaSourceConfig,
    ): MediaSource {
        val mediaSourceConfig = config.copy(
            proxy = config.proxy ?: globalProxySettings.toClientProxyConfig(),
            userAgent = getAniUserAgent(),
        )
        return when (this) {
            is MikanMediaSource.Factory -> create(mediaSourceConfig, mikanIndexCacheRepository)
            is MikanCNMediaSource.Factory -> create(mediaSourceConfig, mikanIndexCacheRepository)
            else -> create(mediaSourceConfig)
        }
    }

    private companion object {
        private val logger = logger<MediaSourceManager>()
    }
}

fun MediaSourceProxySettings.toClientProxyConfig(): ClientProxyConfig? {
    return if (enabled) {
        config.run {
            toClientProxyConfig()
        }
    } else {
        null
    }
}

fun ProxyConfig.toClientProxyConfig() = ClientProxyConfig(
    url = url,
    authorization = authorization?.toHeader(),
)

fun ProxyAuthorization.toHeader(): String = "Basic ${"$username:$password"}"
